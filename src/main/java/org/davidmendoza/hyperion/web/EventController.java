/*
 * The MIT License
 *
 * Copyright 2014 J. David Mendoza <jdmendozar@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.davidmendoza.hyperion.web;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.davidmendoza.hyperion.model.Connection;
import org.davidmendoza.hyperion.model.Event;
import org.davidmendoza.hyperion.model.Message;
import org.davidmendoza.hyperion.model.Party;
import org.davidmendoza.hyperion.model.User;
import org.davidmendoza.hyperion.service.EventService;
import org.davidmendoza.hyperion.service.MessageService;
import org.davidmendoza.hyperion.service.PartyService;
import org.davidmendoza.hyperion.service.UserService;
import org.davidmendoza.hyperion.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Controller
@RequestMapping("/event")
public class EventController extends BaseController {

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private PartyService partyService;
    
    @Inject
    private ConnectionRepository connectionRepository;
    

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String showCreate(Model model) {
        log.debug("/event/showCreate");
        Event event = new Event();
        event.setCode(RandomStringUtils.random(6, false, true));
        model.addAttribute("event", event);
        return "event/create";
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public String confirm(@Valid Event event, BindingResult bindingResult, Model model, Principal principal) throws ParseException {
        log.debug("/event/confirm");
        String back = "event/create";
        if (StringUtils.isBlank(event.getId()) && StringUtils.isNotBlank(event.getCode()) && eventService.isNotUniqueCode(event.getCode())) {
            bindingResult.rejectValue("code", "NotUnique.event.code", new Object[] {event.getCode()}, "Code is not unique");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Could not create event {}", bindingResult.getAllErrors());
            return back;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR, event.getHour());
        cal.set(Calendar.MINUTE, event.getMinutes());
        cal.set(Calendar.AM_PM, event.getAmpm().equals("AM") ? Calendar.AM : Calendar.PM);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(event.getStartDate());
        cal2.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        cal2.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));

        event.setDate(cal2.getTime());

        User user = userService.get(principal.getName());
        event.setUser(user);
        event.setStatus(Constants.DRAFT);

        try {
            event = eventService.createOrUpdate(event);
            model.addAttribute("event", event);
        } catch (Exception e) {
            log.error("Could not create event", e);

            model.addAttribute("errorMessage", "event.not.created");
            model.addAttribute("errorMessageAttr", e.getMessage());

            return back;
        }
        
        return "event/confirm";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@Valid Event event, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal, Model model, HttpServletRequest request) {
        log.debug("/event/create");
        String back = "event/create";
        log.debug("Event {}",event);
        log.debug("Request.hour {}", request.getParameter("hour"));
        if (request.getParameterMap().containsKey("cancel")) {
            return back;
        }
        if (bindingResult.hasErrors()) {
            log.warn("Could not create event {}", bindingResult.getAllErrors());
            return back;
        }

        try {
            User user = userService.get(principal.getName());
            event.setUser(user);
            event.setStatus(Constants.PUBLISHED);

            event = eventService.createOrUpdate(event);

            redirectAttributes.addFlashAttribute("event", event);
            redirectAttributes.addFlashAttribute("successMessage", "event.created");
            redirectAttributes.addFlashAttribute("successMessageAttrs", event.getName());

            Message code = messageService.get(Constants.CODE);
            MimeMessage message = mailSender.createMimeMessage();
            InternetAddress[] addresses = {new InternetAddress("iRSVPed <myrsvplease2@gmail.com>")};
            message.addFrom(addresses);
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getUsername());
            helper.setSubject(code.getSubject());
            String content = code.getContent();
            content = content.replaceAll("@@NAME@@", user.getFirstName());
            content = content.replaceAll("@@USERNAME@@", user.getUsername());
            content = content.replaceAll("@@CODE@@", event.getCode());
            content = content.replaceAll("@@EVENT@@", event.getName());

            helper.setText(content, true);
            mailSender.send(message);

            return "redirect:/event/created/" + event.getId();
        } catch (Exception e) {
            log.error("Could not create event", e);

            model.addAttribute("errorMessage", "event.not.created");
            model.addAttribute("errorMessageAttr", e.getMessage());

            return back;
        }
    }

    @RequestMapping(value = "/created/{eventId}", method = RequestMethod.GET)
    public String created(@PathVariable String eventId, @ModelAttribute("event") Event event, RedirectAttributes redirectAttributes, Model model, Principal principal) {
        log.debug("/event/created");
        if (event == null || !StringUtils.isNotBlank(event.getName())) {
            event = eventService.get(eventId);
            model.addAttribute("event", event);
        }

        if (principal != null) {
            User user = userService.get(principal.getName());
            if (user.getId().equals(event.getUser().getId())) {
                model.addAttribute("owner", Boolean.TRUE);
            }
            List<Party> parties = partyService.findAllByEvent(event, user);
            model.addAttribute("parties", parties);
        }

        return "event/created";
    }

    @RequestMapping(value = "/show/{eventId}", method = RequestMethod.GET)
    public String show(@PathVariable String eventId, @ModelAttribute("event") Event event, RedirectAttributes redirectAttributes, Model model, Principal principal) {
        log.debug("/event/show");
        if (event == null || !StringUtils.isNotBlank(event.getName())) {
            event = eventService.get(eventId);
            model.addAttribute("event", event);
        }

        if (principal != null) {
            User user = userService.get(principal.getName());
            if (user.getId().equals(event.getUser().getId())) {
                model.addAttribute("owner", Boolean.TRUE);
            }
            List<Party> parties = partyService.findAllByEvent(event, user);
            model.addAttribute("parties", parties);
        }

        return "event/show";
    }

    @RequestMapping(value = "/delete/{eventId}", method = RequestMethod.GET)
    public String delete(@PathVariable String eventId, RedirectAttributes redirectAttributes, Principal principal) {
        log.debug("/event/delete");
        if (principal != null) {
            try {
                String name = eventService.delete(eventId, principal.getName());
                redirectAttributes.addFlashAttribute("successMessage", "event.deleted");
                redirectAttributes.addFlashAttribute("successMessageAttrs", name);
            } catch (Exception e) {
                log.error("Could not delete event", e);
                redirectAttributes.addFlashAttribute("errorMessage", "event.not.deleted");
                redirectAttributes.addFlashAttribute("errorMessageAttrs", e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "event.not.deleted.cause.not.signed.in");
        }
        return "redirect:/event";
    }

    @RequestMapping(value = "/edit/{eventId}", method = RequestMethod.GET)
    public String edit(@PathVariable String eventId, Model model, Principal principal) {
        log.debug("/event/edit");
        Event event = eventService.get(eventId);
        model.addAttribute("event", event);

        return "event/edit";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String update(@Valid Event event, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal, Model model, HttpServletRequest request) {
        log.debug("/event/update");
        String back = "event/edit";
        if (request.getParameterMap().containsKey("cancel")) {
            return back;
        }
        if (bindingResult.hasErrors()) {
            log.warn("Could not create event {}", bindingResult.getAllErrors());
            return back;
        }

        try {
            User user = userService.get(principal.getName());
            event.setUser(user);
            event.setStatus(Constants.PUBLISHED);

            event = eventService.createOrUpdate(event);

            redirectAttributes.addFlashAttribute("event", event);
            redirectAttributes.addFlashAttribute("successMessage", "event.updated");
            redirectAttributes.addFlashAttribute("successMessageAttrs", event.getName());

            return "redirect:/event/show/" + event.getId();
        } catch (Exception e) {
            log.error("Could not update event", e);

            model.addAttribute("errorMessage", "event.not.updated");
            model.addAttribute("errorMessageAttr", e.getMessage());

            return back;
        }
    }

    @RequestMapping(value = "/rsvp/{eventId}", method = RequestMethod.GET)
    public String rsvp(@PathVariable String eventId, RedirectAttributes redirectAttributes, Principal principal, HttpServletResponse response) {
        log.debug("/event/rsvp");
        if (principal != null) {
            try {
                SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a ZZZ");
                Event event = eventService.get(eventId);
                User user = userService.get(principal.getName());
                List<Party> parties = partyService.findAllByEvent(event, user);
                List<String[]> rows = new ArrayList<>();
                String[] headers = new String[]{"Name", "Email", "Phone",
                    "Seats", "Comments", "Created", "Event"
                };
                rows.add(headers);
                for (Party party : parties) {
                    String[] row = new String[]{party.getName(), party.getEmail(),
                        party.getPhone(), party.getSeats().toString(), party.getComments(),
                        sdf2.format(party.getDateCreated()), party.getEvent().getName()
                    };
                    rows.add(row);
                }
                SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
                response.setContentType("text/csv");
                response.addHeader("Content-Disposition", "attachment; filename=\"" + event.getName() + "-" + sdf3.format(new Date()) + ".csv\"");
                OutputStream out = response.getOutputStream();
                try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
                    writer.writeAll(rows);
                }
                out.flush();
                return null;
            } catch (Exception e) {
                log.error("Could not export rsvp's", e);
                redirectAttributes.addFlashAttribute("errorMessage", "event.rsvps.not.exported");
                redirectAttributes.addFlashAttribute("errorMessageAttrs", e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "event.rsvps.not.exported.cause.not.signed.in");
        }
        return "redirect:/show/" + eventId;
    }

    @RequestMapping(value = {"", "/list"}, method = RequestMethod.GET)
    public String list(Model model,
            Principal principal,
            @RequestParam(required = false) Boolean mine,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order) {

        Map<String, Object> params = new HashMap<>();
        if (max != null) {
            params.put("max", max);
            if (offset != null) {
                params.put("offset", offset);
            }
        } else {
            params.put("max", 10);
            params.put("offset", 0);
        }

        if (StringUtils.isNotBlank(filter)) {
            params.put("filter", filter);
        }

        if (StringUtils.isNotBlank(sort)) {
            params.put("sort", sort);
        } else {
            params.put("sort", "name");
        }

        if (StringUtils.isNotBlank(order)) {
            params.put("order", order);
        } else {
            params.put("order", "asc");
        }
        params.put("order2", params.get("order"));

        if (mine != null) {
            log.debug("Mine selected");
            params.put("mine", Boolean.TRUE);
            params.put("principal", principal.getName());
        }
        
        //Validar rol del usuario actual, ya que solo ADMIN puede ver todos los eventos
        //Los demas usuarios veran solo sus registros
        
        if(hasPrivileges("ROLE_ADMIN")){
            log.debug("Es ADMIN");
            //Puede ver todos los registros
            params.remove("mine");
        }
        else{
            //Es USER, solo ve sus propios registros
            params.put("mine", Boolean.TRUE);
            params.put("principal", principal.getName());
            
        }

        params = eventService.list(params);

        this.paginate(params, model, page);

        model.addAllAttributes(params);

        return "event/list";
    }
    
    @RequestMapping("/shareMyEventOnFB/{eventId}")
    public String shareEventOnFB(@PathVariable String eventId, RedirectAttributes redirectAttributes, Principal principal, HttpServletResponse response) {
        log.debug("/event/shareEventOnFB");
        if (principal != null) {
            log.debug("Looking for social connection for {}", principal.getName());
            Connection conn = userService.getConnection(principal.getName());
            log.debug("Connection was found {}", conn);
            if (conn != null) {
                org.springframework.social.connect.Connection<Facebook> connection = connectionRepository.findPrimaryConnection(Facebook.class);
                log.debug("Connection FB {}", connection);
                Event event = eventService.get(eventId);
                FacebookProfile profile = connection.getApi().userOperations().getUserProfile();
                log.debug("FB Profile {}", profile);
                log.debug("FB Profile {}", profile.getEmail());
                log.debug("FB Profile {}", profile.getFirstName());
                log.debug("FB Profile {}", profile.getRelationshipStatus());
                connection.getApi().commentOperations().addComment("719978398092913", "testing");
                //org.springframework.social.facebook.api.Event ev = new org.springframework.social.facebook.api.Event
                //connection.getApi().eventOperations().createEvent(event.getName(), sdf.format(event.getDate()), sdf.format(event.getDate()));
//                log.debug("Valor devuelto por FB.post {}", fbEvent);
            }
            else{
                log.debug("No connection was found!");
            }
        }
        return "redirect:/show/" + eventId;
    }

}

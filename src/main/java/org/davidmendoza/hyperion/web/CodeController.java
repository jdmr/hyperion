/*
 * The MIT License
 *
 * Copyright 2014 J. David Mendoza <jdmendoza@swau.edu>.
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

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import org.apache.commons.lang.StringUtils;
import org.davidmendoza.hyperion.model.Event;
import org.davidmendoza.hyperion.model.Message;
import org.davidmendoza.hyperion.model.Party;
import org.davidmendoza.hyperion.service.EventService;
import org.davidmendoza.hyperion.service.MessageService;
import org.davidmendoza.hyperion.service.PartyService;
import org.davidmendoza.hyperion.utils.Constants;
import org.davidmendoza.hyperion.utils.NotEnoughSeatsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Controller
@RequestMapping("/code")
public class CodeController extends BaseController {

    @Autowired
    private EventService eventService;
    @Autowired
    private PartyService partyService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private JavaMailSender mailSender;

    @RequestMapping(method = RequestMethod.POST)
    public String showRsvp(@RequestParam String code, Model model) {
        Event event = eventService.getByCode(code);
        if (event == null) {
            return "home/home";
        }

        model.addAttribute("event", event);

        Party party = new Party();
        party.setEvent(event);
        model.addAttribute("party", party);

        return "code/rsvp";
    }

    @RequestMapping(value = "/rsvp", method = RequestMethod.POST)
    public String rsvp(@Valid Party party, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        String back = "code/rsvp";
        if (bindingResult.hasErrors()) {
            log.warn("Could not rsvp party {}", bindingResult.getAllErrors());
            return back;
        }

        try {
            party = partyService.create(party);
            redirectAttributes.addFlashAttribute("party", party);
            return "redirect:/code/thankyou";
        } catch (NotEnoughSeatsException e) {
            log.info("Not enough seats available for event " + party.getEvent().getId(), e);
            bindingResult.reject("seats", e.getMessage());
            return back;
        } catch (Exception e) {
            log.error("Could not create party for event", e);
            model.addAttribute("errorMessage", "party.not.created");
            model.addAttribute("errorMessageAttrs", e.getMessage());
            return back;
        }
    }

    @RequestMapping(value = "/thankyou", method = RequestMethod.GET)
    public String thankyou(@ModelAttribute("party") Party party) {
        if (party != null && StringUtils.isNotBlank(party.getEmail())) {
            try {
                Event event = eventService.get(party.getEvent().getId());
                Message code = messageService.get(Constants.CODE);
                MimeMessage message = mailSender.createMimeMessage();
                InternetAddress[] addresses = {new InternetAddress("iRSVPed <myrsvplease2@gmail.com>")};
                message.addFrom(addresses);
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(party.getEmail());
                helper.setSubject(code.getSubject());
                String content = code.getContent();
                content = content.replaceAll("@@NAME@@", party.getName());
                content = content.replaceAll("@@USERNAME@@", party.getEmail());
                content = content.replaceAll("@@CODE@@", event.getCode());
                content = content.replaceAll("@@EVENT@@", event.getName());

                helper.setText(content, true);
                mailSender.send(message);

            } catch(Exception e) {
                log.error("Could not send thankyou email", e);
            }

        }
        return "code/thankyou";
    }
}

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

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.inject.Inject;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang.StringUtils;
import org.davidmendoza.hyperion.model.Event;
import org.davidmendoza.hyperion.model.Message;
import org.davidmendoza.hyperion.model.User;
import org.davidmendoza.hyperion.service.MessageService;
import org.davidmendoza.hyperion.service.UserService;
import org.davidmendoza.hyperion.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sun.rmi.transport.proxy.HttpReceiveSocket;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Controller
@RequestMapping("/settings")
public class SettingsController extends BaseController {

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private JavaMailSender mailSender;
    
    @Inject
    private ConnectionRepository connectionRepository;
    

    @RequestMapping(value = "/options", method = RequestMethod.GET)
    public String options(Model model) {
        log.debug("/settings/options");
        
        return "settings/options";
    }
    
    @RequestMapping(value = "/socialize", method = RequestMethod.GET)
    public String linkSocial(Model model) {
        log.debug("/settings/socialize");
        return "settings/socialize";
    }

    @RequestMapping(value = "/disocialize", method = RequestMethod.POST)
    public String unlinkSocial(@Valid Event event, BindingResult bindingResult, Model model, Principal principal) throws ParseException {
        log.debug("/settings/disocialize");
        String back = "/profile";
        if (bindingResult.hasErrors()) {
            log.warn("Could not unlink social network record {}", bindingResult.getAllErrors());
            model.addAttribute("errorMessage", "user.social.network.not.cancelled");
            model.addAttribute("errorMessageAttr", bindingResult.getAllErrors());
            return back;
        }
        
        return "settings/disocialize";
    }

    @RequestMapping(value = "/createAccount", method = RequestMethod.POST)
    public String createAccount(@Valid Event event, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal, Model model, HttpServletRequest request) {
        log.debug("/event/create");
        String back = "event/create";
        
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

            event = null;

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

    @RequestMapping(value = "/changePassword")
    public String changePasswd(HttpServletRequest request, String password, String confirmedPassword, RedirectAttributes redirectAttributes, Model model) {
        log.debug("/settings/changePasswd");
        
        log.debug("Password {}", password);
        log.debug("Confirmed Password {}", confirmedPassword);
        
        try {
            if(password == null || password.isEmpty()){
                log.error("El password esta vacio");
                model.addAttribute("errorMessage", "password.is.empty");
                return "settings/options";
            }
            if(confirmedPassword == null || confirmedPassword.isEmpty()){
                log.error("El password confirmado esta vacio");
                model.addAttribute("errorMessage", "confirmedPassword.is.empty");
                return "settings/options";
            }
            
            if(!password.equals(confirmedPassword)){
                log.error("El password y el password confirmado no son iguales");
                model.addAttribute("errorMessage", "password.not.equals.confirmedPassword");
                return "settings/options";
            }
            
            User user = (User) request.getSession().getAttribute(Constants.LOGGED_USER);
            user = userService.get(user.getId());
            
            userService.changePassword(user, password);
            request.getSession().setAttribute(Constants.LOGGED_USER, user);
            
            redirectAttributes.addFlashAttribute("email", password);
            redirectAttributes.addFlashAttribute("successMessage", "email.updated");
            redirectAttributes.addFlashAttribute("successMessageAttrs", password);
        } catch (Exception e) {
            log.error("Error al intentar modificar el password del usuario {}", e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "settings/options";
        }

        return "settings/options";
    }
    
    @RequestMapping(value = "/updateEmail")
    public String updateEmail(HttpServletRequest request, String email, String confirmedEmail, RedirectAttributes redirectAttributes, Model model) {
        log.debug("/settings/updateEmail");
        
        log.debug("email {}", email);
        log.debug("confirmedEmail {}", confirmedEmail);
        
        if(!email.equals(confirmedEmail)){
            log.error("El correo {} no es igual al correo confirmado {}", email, confirmedEmail);
            model.addAttribute("errorMessage", "email.not.equals");
            return "settings/options";
        }
        
        try {
            User user = (User) request.getSession().getAttribute(Constants.LOGGED_USER);
            user = userService.get(user.getId());
            
            //Validar que el correo no este duplicado
            User user2 = userService.get(email);
            if(user2 != null){
                log.error("El correo {} ya existe! Por favor, verifiquelo", email);
                model.addAttribute("errorMessage", "email.exists");
                return "settings/options";
            }
            
            //Para evitar error
            user.setPasswordVerification(user.getPassword());
            //Se asigna nuevo correo
            user.setUsername(email);
            
            userService.update(user);
            request.getSession().setAttribute(Constants.LOGGED_USER, user);
            
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("successMessage", "email.updated");
            redirectAttributes.addFlashAttribute("successMessageAttrs", email);
        } catch (Exception e) {
            log.error("Error al intentar modificar el correo del usuario {}", e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "settings/options";
        }

        return "settings/options";
    }

    @RequestMapping(value = "deleteAccount")
    public String deleteAccount(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
        log.debug("/settings/deleteAccount");
        
        User user = (User) request.getSession().getAttribute(Constants.LOGGED_USER);
        userService.delete(user.getId());
        
        request.getSession().invalidate();

        return "settings/options";
    }

}

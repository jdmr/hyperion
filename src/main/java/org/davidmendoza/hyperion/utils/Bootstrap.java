/*
 * The MIT License
 *
 * Copyright 2014 J. David Mendoza.
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
package org.davidmendoza.hyperion.utils;

import org.davidmendoza.hyperion.dao.MessageDao;
import org.davidmendoza.hyperion.dao.UserDao;
import org.davidmendoza.hyperion.model.Message;
import org.davidmendoza.hyperion.model.Role;
import org.davidmendoza.hyperion.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Component
@Transactional
public class Bootstrap implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    @Autowired
    private UserDao userDao;
    @Autowired
    private MessageDao messageDao;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Making initial check...");
        log.info("Validating Roles");
        Role adminRole = userDao.getRole("ROLE_ADMIN");
        if (adminRole == null) {
            adminRole = new Role("ROLE_ADMIN");
            adminRole = userDao.createRole(adminRole);
        }

        Role userRole = userDao.getRole("ROLE_USER");
        if (userRole == null) {
            userRole = new Role("ROLE_USER");
            userDao.createRole(userRole);
        }

        log.info("Validating Users");
        User admin = userDao.get("admin@irsvped.com");
        if (admin == null) {
            admin = new User("admin@irsvped.com", "admin", "Admin", "User");
            admin.addRole(adminRole);

            userDao.create(admin);
        }
        
        log.info("Validating Messages");
        Message signup = messageDao.get(Constants.SIGN_UP);
        if (signup == null) {
            signup = new Message();
            signup.setName(Constants.SIGN_UP);
            signup.setSubject("Welcome to iRSVPed!");
            StringBuilder content = new StringBuilder();
            content.append("<p>Salut @@NAME@@,</p>");
            content.append("<p>We are excited to help you create your special event.</p>");
            content.append("<p>To access your event's reports and settings you'll require to provide your credentials:</p>");
            content.append("<dl>");
            content.append("<dt>Email</dt>");
            content.append("<dd>@@USERNAME@@</dd>");
            content.append("<dt>Password</dt>");
            content.append("<dd>@@PASSWORD@@</dd>");
            content.append("</dl>");
            content.append("<p>Thanks again for using our service.</p>");
            signup.setContent(content.toString());
            messageDao.create(signup);
        }

        Message code = messageDao.get(Constants.CODE);
        if (code == null) {
            code = new Message();
            code.setName(Constants.CODE);
            code.setSubject("Your RSVP code is available!");
            StringBuilder content = new StringBuilder();
            content.append("<p>Here is your RSVP code for @@EVENT@@, please provide this code to your special guests:</p>");
            content.append("<p>@@CODE@@</p>");
            content.append("<p>Enjoy!</p>");
            code.setContent(content.toString());
            messageDao.create(code);
        }

        log.info("Done. Application is running!");
    }

}

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

package org.davidmendoza.hyperion.service.impl;

import java.util.List;
import org.davidmendoza.hyperion.dao.UserDao;
import org.davidmendoza.hyperion.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService,
                AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

        private static final Logger log = LoggerFactory
                        .getLogger(UserDetailsServiceImpl.class);
        @Autowired
        private UserDao userDao;

        @Override
        public UserDetails loadUserByUsername(String username)
                        throws UsernameNotFoundException {
                log.debug("loadUserByUsername: {}", username);
                User user = userDao.get(username);
                if (user == null) {
                        throw new UsernameNotFoundException("Could not find user "
                                        + username);
                }
                return (UserDetails) user;
        }

        @Override
        public UserDetails loadUserDetails(OpenIDAuthenticationToken token)
                        throws UsernameNotFoundException {
                log.debug("loadUserDetails: {}", token);
                String username = token.getIdentityUrl();
                String email = "";
                User user = userDao.getByOpenId(username);
                log.debug("Found user : {}", user);
                if (user == null) {
                        log.debug("Looking for email attribute");
                        List<OpenIDAttribute> attrs = token.getAttributes();
                        for (OpenIDAttribute attr : attrs) {
                                log.debug("Attr: {}", attr.getName());
                                if (attr.getName().equals("email")) {
                                        email = attr.getValues().get(0);
                                }
                        }
                        log.debug("Looking for email {}", email);
                        user = userDao.get(email);
                        if (user == null) {
                                throw new UsernameNotFoundException(
                                                "Could not find user " + email);
                        }
                        user.setOpenId(username);
                        userDao.update(user);
                }
                log.debug("Returning user: {}", user);
                return (UserDetails) user;
        }
}
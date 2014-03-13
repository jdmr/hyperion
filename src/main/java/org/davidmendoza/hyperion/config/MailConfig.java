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
package org.davidmendoza.hyperion.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Configuration
@Import(PropertyPlaceholderConfig.class)
public class MailConfig {
    
    @Value("${mail.smtp.host}")
    private String mailHost;
    @Value("${mail.smtp.password}")
    private String mailPassword;
    @Value("${mail.smtp.user}")
    private String mailUser;
    @Value("${mail.smtp.port}")
    private String mailPort;
    @Value("${mail.smtp.auth}")
    private String mailAuth;
    @Value("${mail.smtp.starttls.enable}")
    private String mailStarttls;
    @Value("${mail.smtp.socketFactory.class}")
    private String mailSocketFactory;
    @Value("${mail.debug}")
    private String mailDebug;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setHost(mailHost);
        mailSender.setPassword(mailPassword);
        mailSender.setUsername(mailUser);
        mailSender.setPort(new Integer(mailPort));
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", mailAuth);
        properties.put("mail.smtp.starttls.enable", mailStarttls);
        properties.put("mail.smtp.socketFactory.class", mailSocketFactory);
        properties.put("mail.debug", mailDebug);
        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }
}

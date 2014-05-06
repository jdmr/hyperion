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
package org.davidmendoza.hyperion.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.davidmendoza.hyperion.dao.EventDao;
import org.davidmendoza.hyperion.dao.UserDao;
import org.davidmendoza.hyperion.model.Event;
import org.davidmendoza.hyperion.model.Role;
import org.davidmendoza.hyperion.model.User;
import org.davidmendoza.hyperion.service.BaseService;
import org.davidmendoza.hyperion.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Service
@Transactional
public class EventServiceImpl extends BaseService implements EventService {

    @Autowired
    private EventDao eventDao;
    @Autowired
    private UserDao userDao;

    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> list(Map<String, Object> params) {
        return eventDao.list(params);
    }

    @Override
    public Event createOrUpdate(Event event) throws IOException {
        Date date = new Date();
        event.setLastUpdated(date);
        if (event.getImageFile() != null && !event.getImageFile().isEmpty()) {
            event.setImageName(event.getImageFile().getOriginalFilename());
            event.setContentType(event.getImageFile().getContentType());
            event.setImageSize(event.getImageFile().getSize());
            event.setImageData(event.getImageFile().getBytes());
        }
        if (StringUtils.isBlank(event.getId())) {
            event.setDateCreated(date);
            event.setId(UUID.randomUUID().toString());
            event = eventDao.create(event);
        } else {
            event = eventDao.update(event);
        }
        return event;
    }

    @Transactional(readOnly = true)
    @Override
    public Event get(String eventId) {
        return eventDao.get(eventId);
    }

    @Override
    public Event getByCode(String code) {
        return eventDao.getByCode(code);
    }

    @Override
    public String delete(String eventId, String name) {
        User user = userDao.get(name);
        boolean isAdmin = false;
        for (Role role : user.getRoles()) {
            if (role.getAuthority().contains("ROLE_ADMIN")) {
                isAdmin = true;
                break;
            }
        }
        Event event = eventDao.get(eventId);
        if (isAdmin || event.getUser().getId().equals(user.getId())) {
            eventDao.delete(event);
            return event.getName();
        } else {
            throw new RuntimeException("You can't delete an event that doesn't belong to you.");
        }

    }

    @Override
    public boolean isNotUniqueCode(String code) {
        Event event = eventDao.getByCode(code);
        return event != null;
    }

}

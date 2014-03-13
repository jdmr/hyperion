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
package org.davidmendoza.hyperion.dao.impl;

import org.davidmendoza.hyperion.dao.BaseDao;
import org.davidmendoza.hyperion.dao.UserDao;
import org.davidmendoza.hyperion.model.Role;
import org.davidmendoza.hyperion.model.User;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jdmr on 1/28/14.
 */
@Repository
@Transactional
public class UserDaoHibernate extends BaseDao implements UserDao {
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public User get(String username) {
        Query query = currentSession().getNamedQuery("findUserByUsername");
        query.setString("username", username);
        return (User) query.uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public User getByOpenId(String openId) {
        Query query = currentSession().getNamedQuery("findUserByOpenId");
        query.setString("openId", openId);
        return (User) query.uniqueResult();
    }

    @Override
    public User update(User user) {
        currentSession().update(user);
        return user;
    }

    @Override
    public User create(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        currentSession().save(user);
        return user;
    }
    
    @Override
    public Role getRole(String authority) {
        return (Role) currentSession().get(Role.class, authority);
    }

    @Override
    public Role createRole(Role role) {
        currentSession().save(role);
        return role;
    }

}

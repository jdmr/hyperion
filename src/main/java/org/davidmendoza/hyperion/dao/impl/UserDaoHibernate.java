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

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.davidmendoza.hyperion.dao.BaseDao;
import org.davidmendoza.hyperion.dao.UserDao;
import org.davidmendoza.hyperion.model.Connection;
import org.davidmendoza.hyperion.model.Role;
import org.davidmendoza.hyperion.model.User;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
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
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setPasswordVerification(user.getPassword());
        }
        currentSession().save(user);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRole(String authority) {
        return (Role) currentSession().get(Role.class, authority);
    }

    @Override
    public Role createRole(Role role) {
        currentSession().save(role);
        return role;
    }

    @Override
    public Connection getConnection(String username) {
        Query query = currentSession().createQuery("select c from Connection c where c.id.userid = :username");
        query.setString("username", username);
        return (Connection) query.uniqueResult();
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> list(Map<String, Object> params) {
        log.debug("User list");
        Criteria criteria = currentSession().createCriteria(User.class);
        Criteria countCriteria = currentSession().createCriteria(User.class);
        if (params.containsKey("filter")) {
            String filter = (String) params.get("filter");
            Disjunction properties = Restrictions.disjunction();
            properties.add(Restrictions.ilike("username", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("firstName", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("lastName", filter, MatchMode.ANYWHERE));
            criteria.add(properties);
            countCriteria.add(properties);
        }
        if (params.containsKey("max")) {
            criteria.setMaxResults((Integer) params.get("max"));
            if (params.containsKey("offset")) {
                criteria.setFirstResult((Integer) params.get("offset"));
            }
        }
        if (params.containsKey("sort")) {
            if (((String) params.get("order")).equals("desc")) {
                criteria.addOrder(Order.desc((String) params.get("sort")));
                params.put("order", "asc");
            } else {
                criteria.addOrder(Order.asc((String) params.get("sort")));
                params.put("order", "desc");
            }
        }
        params.put("list", criteria.list());
        countCriteria.setProjection(Projections.rowCount());
        params.put("totalItems", countCriteria.list().get(0));
        return params;
    }

    @Override
    public User get(Long userId) {
        return (User) currentSession().get(User.class, userId);
    }

    @Override
    public void delete(User user) {
        Query query = currentSession().createQuery("delete from Event e where e.user.id = :userId");
        query.setLong("userId", user.getId());
        query.executeUpdate();
        currentSession().delete(user);
    }

}

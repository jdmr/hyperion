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
package org.davidmendoza.hyperion.dao.impl;

import java.util.Map;
import org.davidmendoza.hyperion.dao.BaseDao;
import org.davidmendoza.hyperion.dao.EventDao;
import org.davidmendoza.hyperion.model.Event;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Repository
@Transactional
public class EventDaoImpl extends BaseDao implements EventDao {

    @Override
    public Map<String, Object> list(Map<String, Object> params) {
        log.debug("Event list");
        Criteria criteria = currentSession().createCriteria(Event.class);
        Criteria countCriteria = currentSession().createCriteria(Event.class);
        if (params.containsKey("filter")) {
            String filter = (String) params.get("filter");
            criteria.createAlias("user", "user_alias");
            countCriteria.createAlias("user", "user_alias");
            Disjunction properties = Restrictions.disjunction();
            properties.add(Restrictions.ilike("code", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("name", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("description", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("street", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("city", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("zip", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("hostName", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("hostPhone", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("hostEmail", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("user_alias.username", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("user_alias.firstName", filter, MatchMode.ANYWHERE));
            properties.add(Restrictions.ilike("user_alias.lastName", filter, MatchMode.ANYWHERE));
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

}

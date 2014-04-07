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

package org.davidmendoza.hyperion.dao.impl;

import java.util.List;
import org.davidmendoza.hyperion.dao.BaseDao;
import org.davidmendoza.hyperion.dao.PartyDao;
import org.davidmendoza.hyperion.model.Party;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Repository
@Transactional
public class PartyDaoHibernate extends BaseDao implements PartyDao {

    @Override
    public Integer getAllotedSeats(Party party) {
        Criteria criteria = currentSession().createCriteria(Party.class);
        
        criteria.createCriteria("event").add(Restrictions.idEq(party.getEvent().getId()));
        criteria.setProjection(Projections.sum("seats"));
        
        Long results = (Long) criteria.uniqueResult();
        log.debug("Results {}", results);
        if (results == null) {
            results = 0L;
        }
        return results.intValue();
    }

    @Override
    public Party create(Party party) {
        currentSession().save(party);
        return party;
    }
    
}

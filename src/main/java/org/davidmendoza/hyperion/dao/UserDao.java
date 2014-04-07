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

package org.davidmendoza.hyperion.dao;

import java.util.Map;
import org.davidmendoza.hyperion.model.Connection;
import org.davidmendoza.hyperion.model.Role;
import org.davidmendoza.hyperion.model.User;


/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
public interface UserDao {

    public User get(String username);

    public User getByOpenId(String openId);

    public User update(User user);

    public Role getRole(String authority);

    public Role createRole(Role role);

    public User create(User user);

    public Connection getConnection(String username);

    public Map<String, Object> list(Map<String, Object> params);

    public User get(Long userId);

    public void delete(User user);
    
}

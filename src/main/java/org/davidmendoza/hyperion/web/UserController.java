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

package org.davidmendoza.hyperion.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.davidmendoza.hyperion.model.User;
import org.davidmendoza.hyperion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Controller
@RequestMapping("/admin/user")
public class UserController extends BaseController {
    
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/show/{userId}", method = RequestMethod.GET)
    public String show(@PathVariable Long userId, Model model) {
        User user = userService.get(userId);
        model.addAttribute("user", user);
        return "user/show";
    }
    
    @RequestMapping(value = "/delete/{userId}", method = RequestMethod.GET)
    public String delete(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        try {
        String username = userService.delete(userId);
        redirectAttributes.addFlashAttribute("successMessage", "user.deleted");
        redirectAttributes.addFlashAttribute("successMessageAttrs", username);
        } catch(Exception e) {
            log.error("Could not delete user", e);
            redirectAttributes.addFlashAttribute("errorMessage", "user.not.deleted");
        }
        return "redirect:/admin/user";
    }
    
    @RequestMapping(value = {"", "/list"}, method = RequestMethod.GET)
    public String list(Model model,
            Principal principal,
            @RequestParam(required = false) Boolean mine,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order) {

        Map<String, Object> params = new HashMap<>();
        if (max != null) {
            params.put("max", max);
            if (offset != null) {
                params.put("offset", offset);
            }
        } else {
            params.put("max", 10);
            params.put("offset", 0);
        }

        if (StringUtils.isNotBlank(filter)) {
            params.put("filter", filter);
        }

        if (StringUtils.isNotBlank(sort)) {
            params.put("sort", sort);
        } else {
            params.put("sort", "username");
        }

        if (StringUtils.isNotBlank(order)) {
            params.put("order", order);
        } else {
            params.put("order", "asc");
        }
        params.put("order2", params.get("order"));
        
        params = userService.list(params);

        this.paginate(params, model, page);

        model.addAllAttributes(params);

        return "user/list";
    }
}

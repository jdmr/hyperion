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
package org.davidmendoza.hyperion.web;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;

public abstract class BaseController {

    protected final transient Logger log = LoggerFactory.getLogger(getClass());

    protected void paginate(Map<String, Object> params, Model model, Long page) {
        if (page != null) {
            params.put("page", page);
            model.addAttribute("page", page);
        } else {
            page = 1L;
            model.addAttribute("page", page);
        }
        // pagination start
        Long totalItems = (Long) params.get("totalItems");
        Integer max = (Integer) params.get("max");
        List<Long> pageList = pagination(page, totalItems, max);
        List<?> list = (List<?>) params.get("list");
        Long first = ((page - 1) * max) + 1;
        Long last = first + (list.size() - 1);
        String[] pagination = new String[]{first.toString(),
            last.toString(), totalItems.toString()};
        model.addAttribute("pagination", pagination);
        model.addAttribute("pages", pageList);
        // pagination end
    }

    private List<Long> pagination(Long page, Long totalItems, Integer max) {
        Long numberOfPages = totalItems / max;
        if (totalItems % max > 0) {
            numberOfPages++;
        }
        log.debug("Pagination: {} {} {} {}", new Object[]{page, totalItems,
            max, numberOfPages});
        Set<Long> pageList = new LinkedHashSet<>();
        long h = page - 1;
        long i = page;
        long j = page + 1;
        boolean isThousands = false;
        boolean isFiveHundreds = false;
        boolean isHundreds = false;
        boolean isFifties = false;
        boolean isTens = false;
        boolean isStarted = false;
        if (h > 0 && h > 1000) {
            for (long y = 0; y < h; y += 1000) {
                if (y == 0) {
                    isStarted = true;
                    pageList.add(1l);
                } else {
                    pageList.add(y);
                }
            }
        } else if (h > 0 && h > 500) {
            for (long y = 0; y < h; y += 500) {
                if (y == 0) {
                    isStarted = true;
                    pageList.add(1l);
                } else {
                    pageList.add(y);
                }
            }
        } else if (h > 0 && h > 100) {
            for (long y = 0; y < h; y += 100) {
                if (y == 0) {
                    isStarted = true;
                    pageList.add(1l);
                } else {
                    pageList.add(y);
                }
            }
        } else if (h > 0 && h > 50) {
            for (long y = 0; y < h; y += 50) {
                if (y == 0) {
                    isStarted = true;
                    pageList.add(1l);
                } else {
                    pageList.add(y);
                }
            }
        } else if (h > 0 && h > 10) {
            for (long y = 0; y < h; y += 10) {
                if (y == 0) {
                    isStarted = true;
                    pageList.add(1l);
                } else {
                    pageList.add(y);
                }
            }
        }
        if (i > 1 && i < 4) {
            for (long x = 1; x < i; x++) {
                pageList.add(x);
            }
        } else if (h > 0) {
            if (!isStarted) {
                pageList.add(1L);
            }
            for (long x = h; x < i; x++) {
                pageList.add(x);
            }
        }
        do {
            pageList.add(i);
            if (i > j) {
                if (isThousands || (i + 1000) < numberOfPages) {
                    isThousands = true;
                    i -= i % 1000;
                    i += 999;
                } else if (isFiveHundreds || (i + 500) < numberOfPages) {
                    isFiveHundreds = true;
                    i -= i % 500;
                    i += 499;
                } else if (isHundreds || (i + 100) < numberOfPages) {
                    isHundreds = true;
                    i -= i % 100;
                    i += 99;
                } else if (isFifties || (i + 50) < numberOfPages) {
                    isFifties = true;
                    i -= i % 50;
                    i += 49;
                } else if (isTens || (i + 10) < numberOfPages) {
                    isTens = true;
                    i -= i % 10;
                    i += 9;
                }
            }
        } while (i++ < numberOfPages);
        if (numberOfPages > 0) {
            pageList.add(numberOfPages);
        }

        log.debug("Pages {}: {}", page, pageList);
        return new ArrayList<>(pageList);
    }
    
    public UserDetails getUserDetails(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
          return (UserDetails)principal;
        } else {
          return null;
        }
    }
    
    public Boolean hasPrivileges(String role){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null) return false;
        
        for(GrantedAuthority authority : auth.getAuthorities()){
            log.debug("authority.getAuthority() - {}", authority.getAuthority());
            
            if(authority.getAuthority().equals(role)){
                return true;
            }
        }
        return false;  
    }
}

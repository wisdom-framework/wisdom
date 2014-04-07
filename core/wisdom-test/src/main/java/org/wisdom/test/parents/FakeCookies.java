/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.test.parents;

import com.google.common.collect.Maps;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;

import java.util.Map;


/**
 * A fake implementation of cookies.
 */
public class FakeCookies implements Cookies {

    private Map<String, Cookie> cookies = Maps.newTreeMap();

    /**
     * Adds a cookie. Except the value, others cookie's attributes are meaningless.
     *
     * @param name  the name, must not be {@literal null}
     * @param value the value
     * @return the current cookies holder
     */
    public FakeCookies add(String name, String value) {
        Cookie cookie = new Cookie(name, value, null, null, 3600, null, false, false);
        cookies.put(name, cookie);
        return this;
    }

    /**
     * Adds a cookie.
     *
     * @param cookie the cookie, must not be {@literal null}
     * @return the current cookies holder
     */
    public FakeCookies add(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
        return this;
    }


    /**
     * @param name Name of the cookie to retrieve
     * @return the cookie that is associated with the given name, or null if there is no such cookie
     */
    @Override
    public Cookie get(String name) {
        return cookies.get(name);
    }
}

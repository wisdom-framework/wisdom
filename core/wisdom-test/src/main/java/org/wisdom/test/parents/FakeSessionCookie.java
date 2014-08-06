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

import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * A fake implementation of the session cookie.
 */
public class FakeSessionCookie implements SessionCookie {

    private final Map<String, String> data = new HashMap<>();

    @Override
    public void init(Context context) {
        // Does nothing.
    }

    @Override
    public String getId() {
        return Integer.toString(this.hashCode());
    }

    @Override
    public Map<String, String> getData() {
        return data;
    }

    @Override
    public void save(Context context, Result result) {
        // Does nothing.
    }

    @Override
    public void put(String key, String value) {
        data.put(key, value);
    }

    @Override
    public String get(String key) {
        return data.get(key);
    }

    @Override
    public String remove(String key) {
        return data.remove(key);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }
}

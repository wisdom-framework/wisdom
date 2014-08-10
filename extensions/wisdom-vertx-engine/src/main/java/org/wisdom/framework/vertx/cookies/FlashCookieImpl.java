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
package org.wisdom.framework.vertx.cookies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.utils.CookieDataCodec;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Flash scope: A client side cookie that can be used to transfer information
 * from one request to another.
 * <p/>
 * Stuff in a flash cookie gets deleted after the next request.
 * <p/>
 * Please note also that flash cookies are not signed.
 */
public class FlashCookieImpl implements FlashCookie {

    public static final String FLASH_SUFFIX = "_FLASH";
    public static final String ERROR = "Encoding exception - this must not happen";
    private static final Logger LOGGER = LoggerFactory.getLogger(FlashCookieImpl.class);
    private Map<String, String> currentFlashCookieData = new HashMap<>();
    private Map<String, String> outgoingFlashCookieData = new HashMap<>();
    private final String applicationCookiePrefix;

    public FlashCookieImpl(ApplicationConfiguration configuration) {
        applicationCookiePrefix = configuration.getWithDefault(Cookie.APPLICATION_COOKIE_PREFIX, "wisdom");
    }

    @Override
    public void init(Context context) {
        // get flash cookie:
        Cookie flashCookie = context.request().cookie(applicationCookiePrefix
                + FLASH_SUFFIX);
        if (flashCookie != null) {
            try {
                CookieDataCodec.decode(currentFlashCookieData, flashCookie.value());
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(ERROR, e);
            }
        }

    }

    @Override
    public void save(Context context, Result result) {

        if (outgoingFlashCookieData.isEmpty()) {

            if (context.hasCookie(applicationCookiePrefix
                    + FLASH_SUFFIX)) {
                // Clear the cookie.
                Cookie.Builder cookie = Cookie.builder(applicationCookiePrefix
                        + FLASH_SUFFIX, "");
                cookie.setPath("/");
                cookie.setSecure(false);
                cookie.setMaxAge(0);
                result.with(cookie.build());

            }
        } else {
            try {
                String flashData = CookieDataCodec.encode(outgoingFlashCookieData);

                Cookie.Builder cookie = Cookie.builder(applicationCookiePrefix
                        + FLASH_SUFFIX, flashData);
                cookie.setPath("/");
                cookie.setSecure(false);
                cookie.setMaxAge(3600);
                result.with(cookie.build());

            } catch (Exception e) {
                LOGGER.error(ERROR, e);
            }
        }
    }

    @Override
    public void put(String key, String value) {
        if (key.contains(":")) {
            throw new IllegalArgumentException(
                    "Character ':' is invalid in a flash key.");
        }
        currentFlashCookieData.put(key, value);
        outgoingFlashCookieData.put(key, value);
    }

    @Override
    public void put(String key, Object value) {
        if (value == null) {
            put(key, null);
        } else {
            put(key, value);
        }
    }

    @Override
    public void error(String value) {
        put(FLASH_ERROR, value);
    }

    @Override
    public void success(String value) {
        put(FLASH_SUCCESS, value);
    }

    @Override
    public void discard(String key) {
        outgoingFlashCookieData.remove(key);
    }

    @Override
    public void discard() {
        outgoingFlashCookieData.clear();
    }

    @Override
    public void keep(String key) {
        if (currentFlashCookieData.containsKey(key)) {
            outgoingFlashCookieData.put(key, currentFlashCookieData.get(key));
        }
    }

    @Override
    public void keep() {
        outgoingFlashCookieData.putAll(currentFlashCookieData);
    }

    @Override
    public String get(String key) {
        String value = currentFlashCookieData.get(key);
        if (value == null) {
            value = outgoingFlashCookieData.get(key);
        }
        return value;
    }

    @Override
    public boolean remove(String key) {
        return currentFlashCookieData.remove(key) != null;
    }

    @Override
    public void clearCurrentFlashCookieData() {
        currentFlashCookieData.clear();
    }

    @Override
    public boolean contains(String key) {
        return currentFlashCookieData.containsKey(key);
    }

    @Override
    public Map<String, String> getCurrentFlashCookieData() {
        return currentFlashCookieData;
    }

    @Override
    public Map<String, String> getOutgoingFlashCookieData() {
        return outgoingFlashCookieData;
    }
}

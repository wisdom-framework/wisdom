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
package org.wisdom.wamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * Represents a client accessing the server using WAMP.
 *
 * This class manages the topics listened by the clients and its URL-CURL relations (prefix).
 */
public class WampClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(WampClient.class);

    private final String id;
    private final HashSet<String> topics;
    private final HashMap<String, String> prefixes;
    private final String wisdomClientId;

    public WampClient(String wisdomClientId) {
        this.topics = new HashSet<>();
        this.prefixes = new HashMap<>();
        this.wisdomClientId = wisdomClientId;
        this.id = UUID.randomUUID().toString();
    }

    public synchronized void subscribe(String topic) {
        topics.add(getUri(topic));
    }

    public synchronized boolean isSubscribed(String topic) {
        return topics.contains(getUri(topic));
    }

    public synchronized void unsubscribe(String topic) {
        topics.remove(getUri(topic));
    }

    public String session() {
        return this.id;
    }

    public String wisdomClientId() {
        return this.wisdomClientId;
    }

    public void registerPrefix(String prefix, String uri) {
        prefixes.put(prefix, uri);
    }

    public String getUri(String uri) {
        int index = uri.indexOf(":");
        // Detect if the given uri is prefixed.
        if (index != -1  && ! uri.startsWith("http")) {
            String prefix = uri.substring(0, index);
            String tail = uri.substring(index + 1);
            String url = prefixes.get(prefix);
            if (url != null) {
                return url + tail;
            }
        }
        return uri;
    }
}

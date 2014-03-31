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
package org.wisdom.wamp.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * The CALLERROR message.
 * http://wamp.ws/spec/#callerror_message
 */
public class RPCError extends Message {

    private final String callId;
    private final Throwable exception; //NOSONAR
    private final Object details;
    private final String prefix;

    public RPCError(String callId, Throwable exception, String prefix) {
        this(callId, exception, null, prefix);
    }

    public RPCError(String callId, Throwable exception, Object details, String prefix) {
        this.callId = callId;
        this.exception = exception;
        this.details = details;
        this.prefix = prefix;
        if (callId == null) {
            throw new IllegalArgumentException("callId cannot be null");
        }
        if (exception == null) {
            throw new IllegalArgumentException("exception cannot be null");
        }
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be null");
        }
    }


    @Override
    public MessageType getType() {
        return MessageType.CALLERROR;
    }

    @Override
    public List<Object> toList() {
        List<Object> res = new ArrayList<>();
        res.add(getType().code());
        res.add(this.callId);
        res.add(getUrl(exception));
        res.add(exception.getMessage());
        if (this.details != null) {
            res.add(details);
        }
        return res;
    }

    private String getUrl(Throwable exception) {
        return prefix + "#" + exception.getClass().getSimpleName();
    }
}

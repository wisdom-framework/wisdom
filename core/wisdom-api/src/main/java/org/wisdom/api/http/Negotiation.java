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
package org.wisdom.api.http;

import com.google.common.net.MediaType;

import java.util.Collection;
import java.util.Map;

/**
 * A class providing methods to ease the selection of negotiated content.
 */
public final class Negotiation {

    private Negotiation() {
        // Avoid direct instantiation.
    }

    /**
     * 'Accept' based negotiation.
     * This method determines the result to send to the client based on the 'Accept' header of the request.
     * The returned result is enhanced with the 'Vary' header set to {@literal Accept}.
     * <p/>
     * This methods retrieves the accepted media type in their preference order and check,
     * one-by-one if the given results match one of them. So, it ensures we get the most acceptable result.
     *
     * @param results the set of result structured as follows: mime-type -> result. The mime-type (keys) must be
     *                valid mime type such as 'application/json' or 'text/html'.
     * @return the selected result, or a result with the status {@link org.wisdom.api.http.Status#NOT_ACCEPTABLE} if
     * none of the given results match the request.
     */
    public static Result accept(Map<String, ? extends Result> results) {
        Context context = Context.CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("Negotiation cannot be achieved outside of a request");
        }
        Collection<MediaType> accepted = context.request().mediaTypes();
        // accepted cannot be empty, if the header is missing text/* is added.
        for (MediaType media : accepted) {
            // Do we have a matching key.
            for (Map.Entry<String, ? extends Result> entry : results.entrySet()) {
                MediaType input = MediaType.parse(entry.getKey());
                if (input.is(media)) {
                    return entry.getValue().with(HeaderNames.VARY, HeaderNames.ACCEPT);
                }
            }
        }
        return Results.status(Status.NOT_ACCEPTABLE);
    }

    //TODO Negotiation based on the Languages, Content-Type...
}

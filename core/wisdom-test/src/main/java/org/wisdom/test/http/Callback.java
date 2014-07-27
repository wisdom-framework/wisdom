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
package org.wisdom.test.http;

/**
 * Callback structured used for async HTTP invocation such as
 * {@link org.wisdom.test.http.BaseRequest#asJsonAsync(Callback)} or {@link org.wisdom.test.http
 * .BaseRequest#asStringAsync(Callback)}.
 * <p>
 * Instances of {@link org.wisdom.test.http.Callback} are notified when the HTTP request on which they are associated
 * is completed, failed or cancelled.
 *
 * @param <T> the expected response content type.
 */
public interface Callback<T> {

    /**
     * Method called when the HTTP Response is received.
     *
     * @param response the response
     */
    void completed(HttpResponse<T> response);

    /**
     * Method called when the HTTP Request associated with the current {@link org.wisdom.test.http.Callback} instance
     * has failed.
     *
     * @param e the exception
     */
    void failed(Exception e);

    /**
     * Method called when the HTTP Request associated with the current {@link org.wisdom.test.http.Callback} instance
     * has been cancelled.
     */
    void cancelled();
}

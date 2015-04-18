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
package controller;


import org.apache.http.client.methods.HttpHead;
import org.junit.Test;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.ClientFactory;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class HeadIT extends WisdomBlackBoxTest {

    @Test
    public void testHeadToGetSwitch() throws Exception {
        HttpHead head = new HttpHead(getHttpURl("/hello/html"));
        // When checking the content length, we must disable the compression:
        head.setHeader(HeaderNames.ACCEPT_ENCODING, "identity");
        HttpResponse<String> response;
        try {
            org.apache.http.HttpResponse resp = ClientFactory.getHttpClient().execute(head);
            response = new HttpResponse<>(resp, String.class);
        } finally {
            head.releaseConnection();
        }

        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.HTML);
        System.out.println(response.headers());
        assertThat(Integer.valueOf(response.header(CONTENT_LENGTH))).isEqualTo(20);
    }

    @Test
    public void testHeadToGetSwitchOnMissingPage() throws Exception {
        HttpHead head = new HttpHead(getHttpURl("/hello/missing"));

        HttpResponse<String> response;
        try {
            org.apache.http.HttpResponse resp = ClientFactory.getHttpClient().execute(head);
            response = new HttpResponse<>(resp, String.class);
        } finally {
            head.releaseConnection();
        }

        assertThat(response.code()).isEqualTo(NOT_FOUND);
        assertThat(response.body()).isNull();
    }
}

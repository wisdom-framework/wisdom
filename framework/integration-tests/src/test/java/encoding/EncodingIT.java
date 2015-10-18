/*
 * #%L
 * Wisdom-FrameworkPragma:no-cacheUser-Agent:Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36
Name


 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package encoding;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodingIT extends WisdomBlackBoxTest {

    private HttpClient client = HttpClientBuilder.create()
            .disableContentCompression()
            .build();

    @Test
    public void testDefaultOn() throws Exception {

        HttpGet request = new HttpGet(getHttpURl("/encoding/default"));
        request.addHeader(HeaderNames.ACCEPT_ENCODING, "gzip, deflate");

        HttpResponse response = client.execute(request);

        //Too Small
        assertThat(response.getFirstHeader(HeaderNames.CONTENT_ENCODING)).isNull();

        // Try on a bigger file
        request = new HttpGet(getHttpURl("/assets/LICENSE.txt"));
        request.addHeader(HeaderNames.ACCEPT_ENCODING, "gzip, deflate");

        response = client.execute(request);
        assertThat(response.getFirstHeader(HeaderNames.CONTENT_ENCODING).getValue()).isEqualTo("gzip");
    }

    @Test
    public void testDeflate() throws Exception {

        HttpGet request = new HttpGet(getHttpURl("/encoding/default"));
        request.addHeader(HeaderNames.ACCEPT_ENCODING, "deflate");

        HttpResponse response = client.execute(request);

        // Too small
        assertThat(response.getFirstHeader(HeaderNames.CONTENT_ENCODING)).isNull();

        // Try on a bigger file
        request = new HttpGet(getHttpURl("/assets/LICENSE.txt"));
        request.addHeader(HeaderNames.ACCEPT_ENCODING, "deflate");

        response = client.execute(request);
        assertThat(response.getFirstHeader(HeaderNames.CONTENT_ENCODING).getValue())
                .isEqualTo("deflate");
    }

    @Test
    public void testWithoutCompression() throws Exception {
        HttpGet request = new HttpGet(getHttpURl("/encoding/disabled"));
        request.addHeader(HeaderNames.ACCEPT_ENCODING, "gzip, deflate");

        HttpResponse response = client.execute(request);
        System.out.println(Arrays.toString(response.getAllHeaders()));

        assertThat(response.getFirstHeader(HeaderNames.CONTENT_ENCODING)).isNull();
    }
}

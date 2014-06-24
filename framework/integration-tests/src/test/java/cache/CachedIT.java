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
package cache;

import org.junit.Test;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class CachedIT extends WisdomBlackBoxTest {

    @Test
    public void testCache() throws Exception {
        HttpResponse<String> response = get("/cached").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.TEXT);
        long nano = Long.parseLong(response.body());

        response = get("/cached").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.contentType()).isEqualTo(MimeTypes.TEXT);
        long nano2 = Long.parseLong(response.body());
        assertThat(nano).isEqualTo(nano2);

        response = get("/cached").header(HeaderNames.CACHE_CONTROL, HeaderNames.NOCACHE_VALUE).asString();
        assertThat(response.code()).isEqualTo(OK);
        long nano3 = Long.parseLong(response.body());
        assertThat(nano).isNotEqualTo(nano3);
    }

}

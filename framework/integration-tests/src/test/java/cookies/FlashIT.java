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
package cookies;

import org.apache.http.cookie.Cookie;
import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class FlashIT extends WisdomBlackBoxTest {

    @Test
    public void testFlash() throws Exception {
        HttpResponse<String> response = get("/flash").asString();
        Cookie flash = response.cookie("wisdom_FLASH");
        assertThat(flash).isNotNull();
        assertThat(flash.getPath()).isEqualTo("/");
        assertThat(flash.getValue()).contains("flash_error=Fail").contains("flash_success=Success").contains
                ("message=Hello");

        response = get("/flash/no").asString();
        flash = response.cookie("wisdom_FLASH");
        assertThat(flash).isNull();
        assertThat(response.body()).contains("Hello").contains("Success !");

        response = get("/flash/no").asString();
        flash = response.cookie("wisdom_FLASH");
        assertThat(flash).isNull();
        assertThat(response.body()).doesNotContain("Hello").doesNotContain("Success !");
    }
}

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
package asset;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetIT extends WisdomBlackBoxTest {

    @Test
    public void testMissingAssets() throws Exception {
        HttpResponse<InputStream> response = get("/assets/missing").asBinary();
        assertThat(response.code()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void testThatTheAssetsDirectoryIsNotFound() throws Exception {
        HttpResponse<InputStream> response = get("/assets").asBinary();
        assertThat(response.code()).isEqualTo(NOT_FOUND);

        response = get("/assets/").asBinary();
        assertThat(response.code()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void testAssets() throws Exception {
        HttpResponse<InputStream> response = get("/assets/empty.txt").asBinary();
        assertThat(response.code()).isEqualTo(OK);
        response = get("/assets//empty.txt").asBinary();
        assertThat(response.code()).isEqualTo(OK);
    }

}

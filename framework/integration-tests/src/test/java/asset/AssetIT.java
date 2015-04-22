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

import org.junit.Test;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.utils.KnownMimeTypes;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks asset.
 */
public class AssetIT extends WisdomBlackBoxTest {

    @Test
    public void testMissingAssets() throws Exception {
        HttpResponse<InputStream> response = get("/assets/missing").asBinary();
        assertThat(response.code()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void testThatTheAssetsDirectoryIsTheAssetListPage() throws Exception {
        HttpResponse<String> response = get("/assets").asString();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.body()).contains("Available Assets");
    }

    @Test
    public void testAssets() throws Exception {
        HttpResponse<InputStream> response = get("/assets/empty.txt").asBinary();
        assertThat(response.code()).isEqualTo(OK);

        // Test with another /
        response = get("/assets//empty.txt").asBinary();
        assertThat(response.code()).isEqualTo(OK);

        final String etag = response.header(ETAG);
        assertThat(etag).isNotNull().isNotEmpty();
        response = get("/assets/empty.txt").header(IF_NONE_MATCH, etag).asBinary();
        assertThat(response.code()).isEqualTo(NOT_MODIFIED);

        response = get("/assets/empty.txt").header(IF_NONE_MATCH, etag + "-changed").asBinary();
        assertThat(response.code()).isEqualTo(OK);
    }

    @Test
    public void testMimeType() throws Exception {
        HttpResponse<InputStream> response = get("/assets/test_for_mimetypes.dxf").asBinary();
        assertThat(response.code()).isEqualTo(OK);
        assertThat(response.header(CONTENT_TYPE)).isEqualTo(KnownMimeTypes.getMimeTypeByExtension("dxf"));
    }

    /**
     * We have configured the asset controller to expose assets from /public on /public.
     */
    @Test
    public void testPublicAssets() throws Exception {
        HttpResponse<String> response = get("/public/stuff/my-public-asset.js").asString();
        assertThat(response.code()).isEqualTo(OK);
        final String etag = response.header(ETAG);
        assertThat(etag).isNotNull().isNotEmpty();
        response = get("/public/stuff/my-public-asset.js").header(IF_NONE_MATCH, etag).asString();
        assertThat(response.code()).isEqualTo(NOT_MODIFIED);

        response = get("/public/my-internal-asset.js").asString();
        assertThat(response.code()).isEqualTo(NOT_FOUND);
    }

    /**
     * We have configured the asset controller to expose assets from /interns (in bundle).
     */
    @Test
    public void testInternalAssets() throws Exception {
        HttpResponse<String> response = get("/internal/my-internal-asset.js").asString();
        assertThat(response.code()).isEqualTo(OK);
        final String etag = response.header(ETAG);
        assertThat(etag).isNotNull().isNotEmpty();
        response =  get("/internal/my-internal-asset.js").header(IF_NONE_MATCH, etag).asString();
        assertThat(response.code()).isEqualTo(NOT_MODIFIED);

        response = get("/internal/stuff/my-public-asset.js").asString();
        assertThat(response.code()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void testAssetListing() throws Exception {
        HttpResponse<String> response = get("/assets").asString();
        assertThat(response.code()).isEqualTo(OK);

        assertThat(response.body())
                .contains("/assets/empty.txt")
                .contains("/internal/my-internal-asset.js")
                .contains("/public/stuff/my-public-asset.js");
    }
}

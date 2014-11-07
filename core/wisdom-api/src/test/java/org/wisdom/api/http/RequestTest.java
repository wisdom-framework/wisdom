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

import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public class RequestTest {

    @Test
    public void testMimeTypeAndCharsetOnFullHeader() throws Exception {
        Request my = Mockito.mock(Request.class, Mockito.CALLS_REAL_METHODS);
        doReturn("text/html; charset=ISO-8859-4").when(my).contentType();

        assertThat(my.contentMimeType()).isEqualTo(MimeTypes.HTML);
        assertThat(my.contentCharset()).isEqualTo(Charset.forName("ISO-8859-4"));
    }

    @Test
    public void testMimeTypeAndCharsetOnFullHeaderWithoutSpace() throws Exception {
        Request my = Mockito.mock(Request.class, Mockito.CALLS_REAL_METHODS);
        doReturn("text/html;charset=UTF-8").when(my).contentType();

        assertThat(my.contentMimeType()).isEqualTo(MimeTypes.HTML);
        assertThat(my.contentCharset()).isEqualTo(Charset.forName("UTF-8"));
    }

    @Test
    public void testMimeTypeAndCharsetOnHeaderWithoutCharset() throws Exception {
        Request my = Mockito.mock(Request.class, Mockito.CALLS_REAL_METHODS);
        doReturn("text/html").when(my).contentType();

        assertThat(my.contentMimeType()).isEqualTo(MimeTypes.HTML);
        assertThat(my.contentCharset()).isNull();
    }

    @Test
    public void testMimeTypeAndCharsetOnNotSetHeader() throws Exception {
        Request my = Mockito.mock(Request.class, Mockito.CALLS_REAL_METHODS);
        doReturn(null).when(my).contentType();

        assertThat(my.contentMimeType()).isNull();
        assertThat(my.contentCharset()).isNull();
    }

}
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
package org.wisdom.api.content;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service exposed by content manager allowing data, as InputStream, to be processed to and from a given compression format.
 */
public interface ContentCodec {

    /**
     * Encodes data to this codec format.
     *
     * @param toEncode Data to encode
     * @return Encoded data
     * @throws IOException if the data cannot be encoded
     */
    public InputStream encode(InputStream toEncode) throws IOException;

    /**
     * Decodes data to this codec format.
     *
     * @param toDecode Data to decode
     * @return Decoded data
     * @throws IOException if the data cannot be decoded
     */
    public InputStream decode(InputStream toDecode) throws IOException;

    /**
     * @return Standard name for this encoding, according to <a href="http://tools.ietf.org/html/rfc2616#section-3
     * .5">RFC2616</a>s
     */
    public String getEncodingType();

    /**
     * @return Encoding_Type content used for this encoding, according to <a href="http://tools.ietf
     * .org/html/rfc2616#section-3.5">RFC2616</a>s
     */
    public String getContentEncodingHeaderValue();

}

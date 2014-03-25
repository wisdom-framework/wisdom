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
package org.wisdom.content.encoders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wisdom.api.content.ContentCodec;
import org.wisdom.api.http.EncodingNames;
import org.wisdom.content.codecs.DeflateCodec;
import org.wisdom.content.codecs.GzipCodec;
import org.wisdom.content.codecs.IdentityCodec;

public class CodecsTest {
	
	@Test
	public void testGzipCodec(){
		ContentCodec codec = testCodec(GzipCodec.class);
		if(codec == null)
			return;
		
		assertThat(codec.getContentEncodingHeaderValue()).isEqualTo(EncodingNames.GZIP);
		assertThat(codec.getEncodingType()).isEqualTo(EncodingNames.GZIP);
	}
	
	@Test
	public void testDeflateCodec(){
		ContentCodec codec = testCodec(DeflateCodec.class);
		if(codec == null)
			return;
		
		assertThat(codec.getContentEncodingHeaderValue()).isEqualTo(EncodingNames.DEFLATE);
		assertThat(codec.getEncodingType()).isEqualTo(EncodingNames.DEFLATE);
	}
	
	@Test
	public void testIdentityCodec(){
		ContentCodec codec = testCodec(IdentityCodec.class);
		if(codec == null)
			return;
		
		assertThat(codec.getContentEncodingHeaderValue()).isEqualTo(null);
		assertThat(codec.getEncodingType()).isEqualTo(EncodingNames.IDENTITY);
	}
	
	private ContentCodec testCodec(Class<? extends ContentCodec> codecClass) {
		String data = "abcdefghijklmonpqrstuvwxyz";
		
		ContentCodec codec;
		try {
			codec = codecClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			fail("Unable to instantiate " + codecClass);
			e1.printStackTrace();
			return null;
		}
		
		InputStream inputData, encodedData = null, decodedData = null;
		inputData = IOUtils.toInputStream(data);
		
		try {
			encodedData = codec.encode(inputData);
			decodedData = codec.decode(encodedData);
			assertThat(IOUtils.toString(decodedData)).isEqualTo(data);
		} catch (IOException e) {
			assertThat(false);
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(inputData);
			IOUtils.closeQuietly(encodedData);
			IOUtils.closeQuietly(decodedData);
		}
		
		return codec;
	}
}

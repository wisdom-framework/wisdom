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
package org.wisdom.content.codecs;

import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.http.EncodingNames;

@Component
@Instantiate
@Provides
public class DeflateCodec extends AbstractDefInfCodec {

	@Override
	public String getEncodingType() {
		return EncodingNames.DEFLATE;
	}

	@Override
	public String getContentEncodingHeaderValue() {
		return EncodingNames.DEFLATE;
	}
	
	@Override
	public Class<? extends DeflaterOutputStream> getEncoderClass() {
		return DeflaterOutputStream.class;
	}

	@Override
	public Class<? extends InflaterInputStream> getDecoderClass() {
		return InflaterInputStream.class;
	}
}

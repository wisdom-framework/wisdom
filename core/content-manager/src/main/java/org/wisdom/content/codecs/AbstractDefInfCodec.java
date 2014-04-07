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

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.ContentCodec;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Abstract codec using an {@link DeflaterOutputStream} instance to encode and {@link InflaterInputStream} instance to decode.
 * Subclasses of this two classes can also be used, for instance ({@link GZIPOutputStream and {@link GZIPInputStream}}
 * <br/>
 * Subclasses should implements {@link #getEncoderClass} and {@link #getDecoderClass} to return the chosen encoder classes.
 * <br/>
 * @see ContentCodec
 */
public abstract class AbstractDefInfCodec implements ContentCodec {

	@Override
	public InputStream encode(InputStream toEncode) throws IOException {
		ByteArrayOutputStream bout =  new ByteArrayOutputStream();

		OutputStream encoderout;
		try {
			encoderout = getEncoderClass().getConstructor(OutputStream.class).newInstance(bout);
			encoderout.write(IOUtils.toByteArray(toEncode));
			encoderout.flush();
			encoderout.close();
		} 
		catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
            LoggerFactory.getLogger(AbstractDefInfCodec.class).error("Error while encoding", e);
			//TODO notify encoding has not been done
			return toEncode;
		}

		toEncode.close();

		bout.flush();
		InputStream encoded = new ByteArrayInputStream(bout.toByteArray());
		bout.close();
		return encoded;
	}

	@Override
	public InputStream decode(InputStream toDecode) throws IOException {
		InputStream decoderin;
		try {
			decoderin = getDecoderClass().getConstructor(InputStream.class).newInstance(toDecode);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
            LoggerFactory.getLogger(AbstractDefInfCodec.class).error("Error while decoding", e);
            //TODO notify encoding has not been done
			return toDecode;
		}
		return decoderin;
	}

	@Override
	public abstract String getEncodingType();

	@Override
	public abstract String getContentEncodingHeaderValue();
	
	/**
	 * @return Encoder class the codec use to encode data 
	 */
	public abstract Class<? extends DeflaterOutputStream> getEncoderClass();
	
	/**
	 * @return Decoder class the codec use to decode data
	 */
	public abstract Class<? extends InflaterInputStream> getDecoderClass();
}

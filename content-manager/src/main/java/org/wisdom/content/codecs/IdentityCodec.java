package org.wisdom.content.codecs;

import java.io.IOException;
import java.io.InputStream;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.content.ContentCodec;
import org.wisdom.api.http.EncodingNames;

@Component
@Instantiate
@Provides
public class IdentityCodec implements ContentCodec {

	@Override
	public InputStream encode(InputStream toEncode) throws IOException {
		return toEncode;
	}
	
	@Override
	public InputStream decode(InputStream toDecode) throws IOException {
		return toDecode;
	}

	@Override
	public String getEncodingType() {
		return EncodingNames.IDENTITY;
	}

	@Override
	public String getContentEncodingHeaderValue() {
		return null;
	}
}

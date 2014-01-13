package org.wisdom.content.encoders;

import java.io.IOException;
import java.io.InputStream;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.content.ContentEncoder;
import org.wisdom.api.http.EncodingNames;

@Component
@Instantiate
@Provides
public class IdentityEncoder implements ContentEncoder {

	@Override
	public InputStream encode(InputStream toEncode) throws IOException {
		return toEncode;
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

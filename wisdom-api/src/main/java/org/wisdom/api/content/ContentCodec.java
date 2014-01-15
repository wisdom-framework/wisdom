package org.wisdom.api.content;

import java.io.IOException;
import java.io.InputStream;

public interface ContentCodec {
	
	public InputStream encode(InputStream toEncode) throws IOException;
	
	public InputStream decode(InputStream toDecode) throws IOException;
	
	public String getEncodingType();
	
	public String getContentEncodingHeaderValue();

}

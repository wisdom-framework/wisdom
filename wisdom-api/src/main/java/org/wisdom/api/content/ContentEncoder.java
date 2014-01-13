package org.wisdom.api.content;

import java.io.IOException;
import java.io.InputStream;

public interface ContentEncoder {
	
	public InputStream encode(InputStream toEncode) throws IOException;
	
	public String getEncodingType();
	
	public String getContentEncodingHeaderValue();

}

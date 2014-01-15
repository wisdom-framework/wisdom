package org.wisdom.api.content;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service exposed by content manager allowing data, as InputStream, to be processed to and from a given compression format.
 */
public interface ContentCodec {
	
	/**
	 * Encode data to this codec format
	 * @param toEncode Data to encode
	 * @return Encoded data
	 * @throws IOException
	 */
	public InputStream encode(InputStream toEncode) throws IOException;
	
	/**
	 * Decode data to this codec format
	 * @param toDecode Data to decode
	 * @return Decoded data
	 * @throws IOException
	 */
	public InputStream decode(InputStream toDecode) throws IOException;
	
	/**
	 * @return Standard name for this encoding, according to <a href="http://tools.ietf.org/html/rfc2616#section-3.5">RFC2616</a> 
	 */
	public String getEncodingType();
	/**
	 * @return Encoding_Type content used	 for this encoding, according to <a href="http://tools.ietf.org/html/rfc2616#section-3.5">RFC2616</a> 
	 */
	public String getContentEncodingHeaderValue();

}

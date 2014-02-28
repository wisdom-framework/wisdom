package org.wisdom.api.http;

/**
 * Defines all standard encoding methods.
 */
public interface EncodingNames {

	String GZIP = "gzip";
	String COMPRESS = "compress";
	String DEFLATE = "deflate";
	String IDENTITY = "identity";
	
	String[] ALL_ENCODINGS = {IDENTITY, COMPRESS, DEFLATE, GZIP};
}

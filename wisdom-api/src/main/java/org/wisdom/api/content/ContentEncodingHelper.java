package org.wisdom.api.content;

import java.util.List;

import org.wisdom.api.http.Renderable;
import org.wisdom.api.router.Route;

public interface ContentEncodingHelper {
    
	/**
     * Parse a string to return an ordered list according to the Accept_Encoding HTTP Header grammar
     * @param acceptEncoding String to parse. Should be an Accept_Encoding header.
     * @return An ordered list of encodings
     */
	public List<String> parseAcceptEncodingHeader(String headerContent);
	
	public boolean shouldEncode(Route route, Renderable<?> renderable);
	
	public boolean shouldEncodeWithRoute(Route route);
	
	public boolean shouldEncodeWithSize(Route route, Renderable<?> renderable);
	
	public boolean shouldEncodeWithMimeType(Renderable<?> renderable);
}

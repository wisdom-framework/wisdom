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
package org.wisdom.api.content;

import java.util.List;
import java.util.Map;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;

public interface ContentEncodingHelper {
    
	/**
     * Parse a string to return an ordered list according to the Accept_Encoding HTTP Header grammar
     * @param acceptEncoding String to parse. Should be an Accept_Encoding header.
     * @return An ordered list of encodings
     */
	public List<String> parseAcceptEncodingHeader(String headerContent);
	
	public boolean shouldEncode(Context context, Result result, Renderable<?> renderable);
	
	public boolean shouldEncodeWithRoute(Route route);
	
	public boolean shouldEncodeWithSize(Route route, Renderable<?> renderable);
	
	public boolean shouldEncodeWithMimeType(Renderable<?> renderable);
	
	public boolean shouldEncodeWithHeaders(Map<String, String> headers);
}

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

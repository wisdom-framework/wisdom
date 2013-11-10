package org.ow2.chameleon.wisdom.api.content;


import org.ow2.chameleon.wisdom.api.http.Context;

public interface BodyParser {

    /**
     * Invoke the parser and get back a Java object populated
     * with the content of this request.
     * 
     * MUST BE THREAD SAFE TO CALL!
     * 
     * @param context The context
     * @param classOfT The class we expect
     * @return The object instance populated with all values from raw request
     */
    <T> T invoke(Context context, Class<T> classOfT);
    
    /**
     * The content type this BodyParserEngine can handle
     * 
     * MUST BE THREAD SAFE TO CALL!
     * 
     * @return the content type. this parser can handle - eg. "application/json"
     */
    String getContentType();

}

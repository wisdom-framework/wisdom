package org.wisdom.api.content;

import org.wisdom.api.http.Renderable;

/**
 * Service interface published by components able to render the given content in the given mime-type.
 */
public interface ContentSerializer {

    /**
     * The content type this BodyParserEngine can handle
     *
     * MUST BE THREAD SAFE TO CALL!
     *
     * @return the content type. this parser can handle - eg. "application/json"
     */
    public String getContentType();


    public void serialize(Renderable<?> renderable);

}

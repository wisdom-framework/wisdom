package org.wisdom.api.content;

import com.fasterxml.jackson.databind.Module;

/**
 * A service exposed by the content manager to let applications register custom JSON serializer and deserializer.
 */
public interface JacksonModuleRepository {

    public void register(Module module);

    public void unregister(Module module);

}

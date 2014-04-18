package org.wisdom.resources;

import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

import java.util.Collection;

/**
 * Represents Web Jar Libraries.
 */
public abstract class WebJarLib {

    public final String name;
    public final String version;

    public WebJarLib(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public abstract Collection<String> names();

    public boolean contains(String path) {
        return names().contains(path);
    }

    public abstract Result get(String path, Context context, ApplicationConfiguration configuration, Crypto crypto);

    @Override
    public String toString() {
        return name + "-" + version;
    }


}

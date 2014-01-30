package org.wisdom.wamp.services;

import java.util.Map;

/**
 * A reference on a service exported using WAMP.
 */
public class ExportedService {

    /**
     * The service object.
     */
    public final Object service;

    /**
     * Properties.
     */
    public final Map<String, Object> properties;

    /**
     * The endpoint url.
     */
    public final String url;

    public ExportedService(Object service, Map<String, Object> properties, String url) {
        if (service == null) {
            throw new IllegalArgumentException("service cannot be null");
        }
        if (url == null  || url.isEmpty()) {
            throw new IllegalArgumentException("url cannot be null or empty");
        }
        this.service = service;
        this.properties = properties;
        this.url = url;
    }
}

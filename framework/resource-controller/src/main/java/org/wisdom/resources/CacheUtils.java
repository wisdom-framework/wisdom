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
package org.wisdom.resources;

import org.osgi.framework.Bundle;
import org.slf4j.LoggerFactory;
import org.wisdom.api.asset.Asset;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.*;
import org.wisdom.api.utils.DateUtil;

import java.io.File;
import java.net.URL;

/**
 * Some cache control utilities.
 */
public class CacheUtils {

    /**
     * Value to set max age in header. E.g. Cache-Control:max-age=XXXXXX.
     */
    public static final String HTTP_CACHE_CONTROL_MAX_AGE = "http.cache_control_max_age";
    /**
     * Default value for Cache-Control http header when not set in application.conf.
     */
    public static final String HTTP_CACHE_CONTROL_DEFAULT = "3600";
    /**
     * Enable / disable etag E.g. ETag:"f0680fd3".
     */
    public static final String HTTP_USE_ETAG = "http.useETag";
    /**
     * Default value / etag enabled by default.
     */
    public static final boolean HTTP_USE_ETAG_DEFAULT = true;

    /**
     * Add the last modified header to the given result. This method handle the HTTP Date format.
     *
     * @param result       the result
     * @param lastModified the date
     */
    public static void addLastModified(Result result, long lastModified) {
        result.with(HeaderNames.LAST_MODIFIED, DateUtil.formatForHttpHeader(lastModified));
    }

    /**
     * Check whether the request can send a NOT_MODIFIED response.
     *
     * @param context      the context
     * @param lastModified the last modification date
     * @param etag         the etag.
     * @return true if the content is modified
     */
    public static boolean isNotModified(Context context, long lastModified, String etag) {
        // First check etag. Important, if there is an If-None-Match header, we MUST not check the
        // If-Modified-Since header, regardless of whether If-None-Match matches or not. This is in
        // accordance with section 14.26 of RFC2616.

        final String browserEtag = context.header(HeaderNames.IF_NONE_MATCH);
        if (browserEtag != null) {
            // We check the given etag against the given one.
            // If the given one is null, that means that etags are disabled.
            return browserEtag.equals(etag);
        }

        // IF_NONE_MATCH not set, check IF_MODIFIED_SINCE
        final String ifModifiedSince = context.header(HeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && lastModified > 0 && !ifModifiedSince.isEmpty()) {
            try {
                // We do a double check here because the time granularity is important here.
                // If the written date headers are still the same, we are unchanged (the granularity is the
                // second).
                return ifModifiedSince.equals(DateUtil.formatForHttpHeader(lastModified));
            } catch (IllegalArgumentException ex) {
                LoggerFactory.getLogger(CacheUtils.class)
                        .error("Cannot build the date string for {}", lastModified, ex);
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the ETAG value based on the last modification date passed as parameter.
     *
     * @param lastModification the last modification (must be valid)
     * @param configuration    the configuration
     * @param crypto           the crypto service
     * @return the encoded etag
     */
    public static String computeEtag(long lastModification, ApplicationConfiguration configuration, Crypto crypto) {
        boolean useEtag = configuration.getBooleanWithDefault(HTTP_USE_ETAG,
                HTTP_USE_ETAG_DEFAULT);
        if (!useEtag) {
            return null;
        }
        String raw = Long.toString(lastModification);
        return crypto.hexSHA1(raw);
    }

    /**
     * Adds cache control and etag to the given result.
     *
     * @param result        the result
     * @param etag          the etag
     * @param configuration the application configuration
     */
    public static void addCacheControlAndEtagToResult(Result result, String etag, ApplicationConfiguration configuration) {
        String maxAge = configuration.getWithDefault(HTTP_CACHE_CONTROL_MAX_AGE,
                HTTP_CACHE_CONTROL_DEFAULT);

        if ("0".equals(maxAge)) {
            result.with(HeaderNames.CACHE_CONTROL, "no-cache");
        } else {
            result.with(HeaderNames.CACHE_CONTROL, "max-age=" + maxAge);
        }

        // Use etag on demand:
        boolean useEtag = configuration.getBooleanWithDefault(HTTP_USE_ETAG,
                HTTP_USE_ETAG_DEFAULT);

        if (useEtag) {
            result.with(HeaderNames.ETAG, etag);
        }
    }

    /**
     * Computes the result to sent the given file. Cache headers are automatically set by this method.
     *
     * @param file          the file to send to the client
     * @param context       the context
     * @param configuration the application configuration
     * @param crypto        the crypto service
     * @return the result, it can be a NOT_MODIFIED if the file was not modified since the last request,
     * or an OK result with the cache headers set.
     */
    public static Result fromFile(File file, Context context, ApplicationConfiguration configuration, Crypto crypto) {
        long lastModified = file.lastModified();
        String etag = computeEtag(lastModified, configuration, crypto);
        if (isNotModified(context, lastModified, etag)) {
            return new Result(Status.NOT_MODIFIED);
        } else {
            Result result = Results.ok(file);
            addLastModified(result, lastModified);
            addCacheControlAndEtagToResult(result, etag, configuration);
            return result;
        }
    }

    public static Result fromBundle(Bundle bundle, URL url, Context context, ApplicationConfiguration configuration,
                                    Crypto crypto) {
        long lastModified = bundle.getLastModified();
        String etag = CacheUtils.computeEtag(lastModified, configuration, crypto);
        if (CacheUtils.isNotModified(context, lastModified, etag)) {
            return new Result(Status.NOT_MODIFIED);
        } else {
            Result result = Results.ok(url);
            addLastModified(result, lastModified);
            addCacheControlAndEtagToResult(result, etag, configuration);
            return result;
        }
    }

    public static Result fromAsset(Context context, Asset asset, ApplicationConfiguration configuration) {
        if (CacheUtils.isNotModified(context, asset.getLastModified(), asset.getEtag())) {
            return new Result(Status.NOT_MODIFIED);
        } else {
            Result result;
            if (asset.getContent() instanceof File) {
                result = Results.ok((File) asset.getContent());
            } else if (asset.getContent() instanceof URL) {
                result = Results.ok((URL) asset.getContent());
            } else {
                // Use object, probably won't work.
                result = Results.ok(asset.getContent());
            }
            addLastModified(result, asset.getLastModified());
            addCacheControlAndEtagToResult(result, asset.getEtag(), configuration);
            return result;
        }
    }
}

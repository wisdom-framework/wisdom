package org.wisdom.resources;

import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.*;
import org.wisdom.api.utils.DateUtil;

import java.io.File;
import java.util.Date;

/**
 * Some cache control utilities.
 */
public class CacheUtils {

    /**
     * Value to set max age in header. E.g. Cache-Control:max-age=XXXXXX
     */
    public static final String HTTP_CACHE_CONTROL_MAX_AGE = "http.cache_control_max_age";
    /**
     * Default value for Cache-Control http header when not set in application.conf
     */
    public static final String HTTP_CACHE_CONTROL_DEFAULT = "3600";
    /**
     * Enable / disable etag E.g. ETag:"f0680fd3"
     */
    public static final String HTTP_USE_ETAG = "http.useETag";
    /**
     * Default value / etag enabled by default.
     */
    public static final boolean HTTP_USE_ETAG_DEFAULT = true;

    /**
     * Add the last modified header to the given result. This method handle the HTTP Date format.
     * @param result the result
     * @param lastModified the date
     */
    public static void addLastModified(Result result, long lastModified) {
        result.with(HeaderNames.LAST_MODIFIED, DateUtil.formatForHttpHeader(lastModified));
    }

    /**
     * Check whether the request can send a NOT_MODIFIED response.
     * @param context the context
     * @param lastModified the last modification date
     * @param etag the etag.
     * @return true if the content is modified
     */
    public static boolean isModified(Context context, long lastModified, String etag) {
        // First check etag. Important, if there is an If-None-Match header, we MUST not check the
        // If-Modified-Since header, regardless of whether If-None-Match matches or not. This is in
        // accordance with section 14.26 of RFC2616.

        final String browserEtag = context.header(HeaderNames.IF_NONE_MATCH);
        if (browserEtag != null) {
            // We check the given etag against the given one.
            // If the given one is null, that means that etags are disabled.
            return !browserEtag.equals(etag);
        }

        // IF_NONE_MATCH not set, check IF_MODIFIED_SINCE
        final String ifModifiedSince = context.header(HeaderNames.IF_MODIFIED_SINCE);

        if (ifModifiedSince != null && lastModified > 0 && !ifModifiedSince.isEmpty()) {
            try {
                Date browserDate = DateUtil.parseHttpDateFormat(ifModifiedSince);
                if (browserDate.getTime() >= lastModified) {
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                LoggerFactory.getLogger(CacheUtils.class)
                        .error("Cannot parse the data value from the " + HeaderNames.IF_MODIFIED_SINCE + " " +
                        "value (" + ifModifiedSince + ")", ex);
                return false;
            }
            return true;
        }
        return true;
    }

    /**
     * Computes the ETAG value based on the last modification date passed as parameter
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
     * Adds cache control and etag to the given result
     * @param result the result
     * @param etag the etag
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

    public static Result fromFile(File file, Context context, ApplicationConfiguration configuration, Crypto crypto) {
        long lastModified = file.lastModified();
        String etag = computeEtag(lastModified, configuration, crypto);
        if (!isModified(context, lastModified, etag)) {
            return new Result(Status.NOT_MODIFIED);
        } else {
            Result result = Results.ok(file);
            addLastModified(result, lastModified);
            addCacheControlAndEtagToResult(result, etag, configuration);
            return result;
        }
    }
}

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
package org.wisdom.content.encoding;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.annotations.encoder.AllowEncoding;
import org.wisdom.api.annotations.encoder.DenyEncoding;
import org.wisdom.api.bodies.RenderableURL;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEncodingHelper;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.api.utils.KnownMimeTypes;

import java.util.*;

/**
 * An implementation of the {@link org.wisdom.api.content.ContentEncodingHelper} service. This service decides
 * whether or not a response should be encoded.
 */
@Component
@Instantiate
@Provides
public class ContentEncodingHelperImpl implements ContentEncodingHelper {

    @Requires(specification = ApplicationConfiguration.class, optional = false)
    ApplicationConfiguration configuration;

    Boolean allowEncodingGlobalSetting = null;

    Boolean allowUrlEncodingGlobalSetting = null;

    Long maxSizeGlobalSetting = null;

    Long minSizeGlobalSetting = null;

    /**
     * Sets the application configuration.  For testing purpose only.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @return whether or not the global encoding is enabled. It returns the {@literal encoding.global} value from
     * the application configuration. {@literal true} by default.
     */
    public boolean getAllowEncodingGlobalSetting() {
        if (allowEncodingGlobalSetting == null) {
            allowEncodingGlobalSetting = configuration.getBooleanWithDefault(
                    ApplicationConfiguration.ENCODING_GLOBAL,
                    ApplicationConfiguration.DEFAULT_ENCODING_GLOBAL);
        }
        return allowEncodingGlobalSetting;
    }

    /**
     * @return whether or not the global encoding for response reading URLs is enabled. It returns the {@literal
     * encoding.url} value from the application configuration. {@literal true} by default.
     */
    public boolean getAllowUrlEncodingGlobalSetting() {
        if (allowUrlEncodingGlobalSetting == null) {
            allowUrlEncodingGlobalSetting = configuration.getBooleanWithDefault(
                    ApplicationConfiguration.ENCODING_URL,
                    ApplicationConfiguration.DEFAULT_ENCODING_URL);
        }
        return allowUrlEncodingGlobalSetting;
    }

    /**
     * @return the maximum (response) size (in bits) on which response encoding is applied. It returns the
     * {@literal encoding.max.size} value from the application configuration. {@literal 10 Mb} by default,
     * so response that are bigger are not encoded. This is because such large responses are generally already zipped.
     */
    public long getMaxSizeGlobalSetting() {
        if (maxSizeGlobalSetting == null) {
            maxSizeGlobalSetting = configuration.getLongWithDefault(
                    ApplicationConfiguration.ENCODING_MAX_SIZE_OLD, -1l);
        }
        if (maxSizeGlobalSetting == -1) {
            maxSizeGlobalSetting = configuration.getBytes(
                    ApplicationConfiguration.ENCODING_MAX_SIZE,
                    ApplicationConfiguration.DEFAULT_ENCODING_MAX_SIZE);
        }
        return maxSizeGlobalSetting;
    }

    /**
     * @return the minimum (response) size (in bits) on which response encoding is applied. It returns the
     * {@literal encoding.min.size} value from the application configuration. {@literal 10 Kb} by default,
     * so response that are smaller are not encoded. This is because for such small responses,
     * the encoding process is more expensive that the gain.
     */
    public long getMinSizeGlobalSetting() {
        if (minSizeGlobalSetting == null) {
            minSizeGlobalSetting = configuration.getLongWithDefault(
                    ApplicationConfiguration.ENCODING_MIN_SIZE_OLD, -1l);
        }
        if (minSizeGlobalSetting == -1) {
            minSizeGlobalSetting = configuration.getBytes(
                    ApplicationConfiguration.ENCODING_MIN_SIZE,
                    ApplicationConfiguration.DEFAULT_ENCODING_MIN_SIZE);
        }
        return minSizeGlobalSetting;
    }

    /**
     * Checks whether the result (i.e. response) must be encoded or not.
     *
     * @param context    the context
     * @param result     the result
     * @param renderable the renderable
     * @return {@code true} if the encoding must be applied on the given result, {@code false} otherwise.
     */
    @Override
    public boolean shouldEncode(Context context, Result result, Renderable<?> renderable) {
        //If no result or context, abort
        return !(context == null || result == null)
                && shouldEncodeWithHeaders(result.getHeaders())
                && shouldEncodeWithRoute(context.route())
                && shouldEncodeWithSize(context.route(), renderable)
                && shouldEncodeWithMimeType(renderable);

    }

    /**
     * Checks whether or not the given headers enable the encoding. This method checks the value of the {@literal
     * Content-Encoding} header. If this header is not set, the encoding is enabled.
     *
     * @param headers the headers
     * @return {@code true} if the given headers enable the encoding, {@code false} otherwise.
     */
    @Override
    public boolean shouldEncodeWithHeaders(Map<String, String> headers) {
        //No header provided, allow encoding
        if (headers == null) {
            return true;
        }

        String contentEncoding = headers.get(HeaderNames.CONTENT_ENCODING);

        return contentEncoding == null
                || contentEncoding.length() == 0
                || contentEncoding.equals("\n")
                || contentEncoding.equals(EncodingNames.IDENTITY);
    }

    /**
     * Checks whether or not the given mime-type support encoding. Indeed, some mime types are already compressed,
     * and so re-applying encoding on these result is particularly inefficient.
     *
     * @param renderable the renderable objet
     * @return {@code true} if the mime type of the given renderable object is not known as a compressed type. If the
     * given renderable is {@code null} or the mime type is unknown, returns {@code false}.
     */
    @Override
    public boolean shouldEncodeWithMimeType(Renderable<?> renderable) {
        //No renderable provided, abort encoding
        if (renderable == null) {
            return false;
        }

        String mime = renderable.mimetype();

        // Drop on unknown mime types
        return mime != null && !KnownMimeTypes.COMPRESSED_MIME.contains(mime);

    }

    /**
     * Checks whether or not the given renderable meet the encoding requirements. This means that its size is
     * (strictly) between the min encoding size and max encoding size. The min and max sizes can be configured globally,
     * for the controller or for the invoked method with the {@link org.wisdom.api.annotations.encoder.AllowEncoding}
     * and {@link org.wisdom.api.annotations.encoder.DenyEncoding} annotations.
     *
     * @param renderable the renderable objet
     * @return {@code true} if the renderable object has a size suitable with encoding. If the size is unknown
     * ({@literal -1}), or {@literal 0} or if the renderable is {@literal null},
     * returns {@code false}. If the renderable object is serving an URL ( so is an instance of{@link org.wisdom.api
     * .bodies.RenderableURL}), it returns the configured value for URL ({@link #getAllowUrlEncodingGlobalSetting()}).
     */
    @Override
    public boolean shouldEncodeWithSize(Route route, Renderable<?> renderable) {
        //No renderable provided, abort encoding
        if (renderable == null) {
            return false;
        }

        long renderableLength = renderable.length();

        // Renderable is url, return config value
        if (renderable instanceof RenderableURL) {
            return getAllowUrlEncodingGlobalSetting();
        }

        // Not an URL and value is -1 or 0
        if (renderableLength <= 0) {
            return false;
        }

        long confMaxSize = getMaxSizeGlobalSetting();
        long confMinSize = getMinSizeGlobalSetting();
        long methodMaxSize = -1;
        long controllerMaxSize = -1;
        long methodMinSize = -1;
        long controllerMinSize = -1;

        if (route != null && !route.isUnbound()) {
            // Retrieve size limitation on method if any
            AllowEncoding allowOnMethod = route.getControllerMethod().getAnnotation(AllowEncoding.class);
            methodMaxSize = allowOnMethod != null ? allowOnMethod.maxSize() : -1;
            methodMinSize = allowOnMethod != null ? allowOnMethod.minSize() : -1;
            // Retrieve size limitation on class if any
            AllowEncoding allowOnController = route.getControllerClass().getAnnotation(AllowEncoding.class);
            controllerMaxSize = allowOnController != null ? allowOnController.maxSize() : -1;
            controllerMinSize = allowOnController != null ? allowOnController.minSize() : -1;
        }

        // Find max size first on method, then on controller and, if none, use default
        long maxSize = methodMaxSize != -1 ? methodMaxSize : controllerMaxSize != -1 ? controllerMaxSize : confMaxSize;
        // Find min size first on method, then on controller and, if none, use default
        long minSize = methodMinSize != -1 ? methodMinSize : controllerMinSize != -1 ? controllerMinSize : confMinSize;

        // Ensure renderableLength is in min - max boundaries
        return !(renderableLength > maxSize || renderableLength < minSize);

    }

    /**
     * Checks whether or not the given route allows encoding.
     *
     * @param route the route
     * @return {@literal true} if the route is not {@literal null} or {@literal unbound} and if the invoked method
     * and controller do not disabled the encoding explicitly (with the {@link org.wisdom.api.annotations.encoder
     * .DenyEncoding} annotation).
     */
    @Override
    public boolean shouldEncodeWithRoute(Route route) {
        boolean isAllowOnMethod = false;
        boolean isDenyOnMethod = false;
        boolean isAllowOnController = false;
        boolean isDenyOnController = false;

        if (route != null && !route.isUnbound()) {
            // Retrieve @AllowEncoding annotations
            isAllowOnMethod = route.getControllerMethod().getAnnotation(AllowEncoding.class) != null;
            isAllowOnController = route.getControllerClass().getAnnotation(AllowEncoding.class) != null;
            // Retrieve @DenyEncoding annotations
            isDenyOnMethod = route.getControllerMethod().getAnnotation(DenyEncoding.class) != null;
            isDenyOnController = route.getControllerClass().getAnnotation(DenyEncoding.class) != null;
        }

        if (getAllowEncodingGlobalSetting()) {
            // Configuration tells yes, check local configuration.
            return !isDenyOnMethod && !(isDenyOnController && !isAllowOnMethod);
        } else {
            // Configuration tells no, check local configuration.
            return isAllowOnMethod || isAllowOnController && !isDenyOnMethod;
        }
    }

    /**
     * Parses the {@literal ACCEPT-ENCODING} header.
     *
     * @param headerContent String to parse. {@literal ACCEPT-ENCODING} header.
     * @return the list of values from the {@literal ACCEPT-ENCODING} header sorted by preferences.
     */
    @Override
    public List<String> parseAcceptEncodingHeader(String headerContent) {
        List<String> result = new ArrayList<>();
        // Intermediate list to sort encoding types
        List<ValuedEncoding> tmp = new ArrayList<>();

        //Empty or null Accept_Encoding => return empty list
        if (headerContent == null || headerContent.trim().length() == 0 || headerContent.trim().equals("\n")) {
            return result;
        }

        //Parse Accept_Encoding for different encodings declarations
        String[] encodingItems = headerContent.split(",");
        int position = 0;

        for (String encodingItem : encodingItems) {
            // Build valued encoding from the current item ("gzip", "gzip;q=0.5", ...)
            ValuedEncoding encoding = new ValuedEncoding(encodingItem, position);
            // Don't insert 0 qValued encodings
            if (encoding.qValue > 0)
                tmp.add(encoding);
            //High increment pace for wildcard insertion
            position += 100;
        }

        ValuedEncoding wildCard = null;

        //Search for wildcard
        for (ValuedEncoding valuedEncoding : tmp) {
            //wildcard found
            if (valuedEncoding.encoding.equals("*")) {
                wildCard = valuedEncoding;
                break;
            }
        }

        // Wildcard found
        if (wildCard != null) {
            // Retrieve all possible encodings
            List<String> encodingsToAdd = Arrays.asList(EncodingNames.ALL_ENCODINGS);
            // Remove wildcard from encodings, it will be replaced by encodings not yet found
            tmp.remove(wildCard);
            // Remove all already found encodings from available encodings
            for (ValuedEncoding valuedEncoding : tmp) {
                encodingsToAdd.remove(valuedEncoding.encoding);
            }
            // Add remaining encodings with wildCard qValue and position (still incremented by 1 for each)
            for (String remainingEncoding : encodingsToAdd) {
                tmp.add(new ValuedEncoding(remainingEncoding, wildCard.qValue, wildCard.position++));
            }
        }

        // Sort ValuedEncodings
        Collections.sort(tmp);

        //Create the result List
        for (ValuedEncoding encoding : tmp) {
            result.add(encoding.encoding);
        }

        return result;
    }
}

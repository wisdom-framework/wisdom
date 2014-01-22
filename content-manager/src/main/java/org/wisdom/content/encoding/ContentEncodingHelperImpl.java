package org.wisdom.content.encoding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.annotations.encoder.AllowEncoding;
import org.wisdom.api.annotations.encoder.DenyEncoding;
import org.wisdom.api.bodies.RenderableURL;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEncodingHelper;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.EncodingNames;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.utils.KnownMimeTypes;

@Component
@Instantiate
@Provides
public class ContentEncodingHelperImpl implements ContentEncodingHelper{
	
	@Requires(specification=ApplicationConfiguration.class, optional=false)
	ApplicationConfiguration configuration;
	
	Boolean allowEncodingGlobalSetting = null;
	
	Boolean allowUrlEncodingGlobalSetting = null;
	
	Long maxSizeGlobalSetting = null;
	
	Long minSizeGlobalSetting = null;
	
	public boolean getAllowEncodingGlobalSetting(){
		if(allowEncodingGlobalSetting == null)
			allowEncodingGlobalSetting = configuration.getBooleanWithDefault(ApplicationConfiguration.ENCODING_GLOBAL, ApplicationConfiguration.DEFAULT_ENCODING_GLOBAL);
		return allowEncodingGlobalSetting;
	}
	
	public boolean getAllowUrlEncodingGlobalSetting(){
		if(allowUrlEncodingGlobalSetting == null)
			allowUrlEncodingGlobalSetting = configuration.getBooleanWithDefault(ApplicationConfiguration.ENCODING_URL, ApplicationConfiguration.DEFAULT_ENCODING_URL);
		return allowUrlEncodingGlobalSetting;
	}
	
	public long getMaxSizeGlobalSetting(){
		if(maxSizeGlobalSetting == null)
			maxSizeGlobalSetting = configuration.getLongWithDefault(ApplicationConfiguration.ENCODING_MAX_SIZE, ApplicationConfiguration.DEFAULT_ENCODING_MAX_SIZE);
		return maxSizeGlobalSetting;
	}
	
	public long getMinSizeGlobalSetting(){
		if(minSizeGlobalSetting == null)
			minSizeGlobalSetting = configuration.getLongWithDefault(ApplicationConfiguration.ENCODING_MIN_SIZE, ApplicationConfiguration.DEFAULT_ENCODING_MIN_SIZE);
		return minSizeGlobalSetting;
	}
	
	@Override
	public boolean shouldEncode(Context context, Result result, Renderable<?> renderable) {
		return shouldEncodeWithHeaders(result.getHeaders()) && shouldEncodeWithRoute(context.getRoute()) && shouldEncodeWithSize(context.getRoute(), renderable) && shouldEncodeWithMimeType(renderable);
	}
	
	@Override
	public boolean shouldEncodeWithHeaders(Map<String, String> headers){
		String contentEncoding = headers.get(HeaderNames.CONTENT_ENCODING);
		
		if(contentEncoding != null){// There is a content encoding already set
			//if empty or identity, we can encode
			if(contentEncoding.equals("") || contentEncoding.equals("\n") || contentEncoding.equals(EncodingNames.IDENTITY)){
				return true;
			}else{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean shouldEncodeWithMimeType(Renderable<?> renderable){
		String mime = renderable.mimetype();
		
		if(mime == null){
			//TODO What to do when we can't know the mime type ? drop or allow ?
			//Drop on unknown mime type
			return false;
		}
		
		if(KnownMimeTypes.COMPRESSED_MIME.contains(mime)){
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean shouldEncodeWithSize(Route route, Renderable<?> renderable){
		long renderableLength = renderable.length();
    	// Renderable is stream, return config value
    	if(renderable instanceof RenderableURL){
    		return getAllowUrlEncodingGlobalSetting();
    	}
    	// Not a stream and value is -1 or 0
    	if(renderableLength <= 0)
    		return false;
    	
    	long confMaxSize = getMaxSizeGlobalSetting();
    	long confMinSize = getMinSizeGlobalSetting();
    	long methodMaxSize = -1, controllerMaxSize = -1, methodMinSize = -1, controllerMinSize = -1;
    	
    	if(route != null){
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
    	if(renderableLength > maxSize || renderableLength < minSize)
    		return false;
    	
    	return true;
	}
	
	@Override
    public boolean shouldEncodeWithRoute(Route route){
    	boolean isAllowOnMethod = false, isDenyOnMethod = false, isAllowOnController = false, isDenyOnController = false;
    	
    	if(route != null){
	    	// Retrieve @AllowEncoding annotations
			isAllowOnMethod = route.getControllerMethod().getAnnotation(AllowEncoding.class) == null ? false : true;
			isAllowOnController = route.getControllerClass().getAnnotation(AllowEncoding.class) == null ? false : true;
			// Retrieve @DenyEncoding annotations
			isDenyOnMethod = route.getControllerMethod().getAnnotation(DenyEncoding.class) == null ? false : true;
			isDenyOnController = route.getControllerClass().getAnnotation(DenyEncoding.class) == null ? false : true;
    	}
    	
    	if(getAllowEncodingGlobalSetting()){ // Configuration tells yes
    		if(isDenyOnMethod) // Method tells no
    			return false;
    		if(isDenyOnController && !isAllowOnMethod) // Class tells no & Method doesn't tell yes
    			return false;

    		return true;
    	}else{ // Configuration tells no
    		if(isAllowOnMethod) // Method tells yes
    			return true;
    		if(isAllowOnController && !isDenyOnMethod) // Class tells yes & Method doesn't tell no
    			return true;
    					
    		return false;
    	}
    }

	@Override
	public List<String> parseAcceptEncodingHeader(String headerContent){
    	List<String> result = new ArrayList<String>();
    	// Intermediate list to sort encoding types
    	List<ValuedEncoding> tmp = new ArrayList<ValuedEncoding>();
    	
    	//Empty or null Accept_Encoding => return empty list
    	if(headerContent == null || headerContent.trim().equals("") || headerContent.trim().equals("\n")){
    		return result;
    	}

    	//Parse Accept_Encoding for different encodings declarations
    	String[] encodingItems = headerContent.split(",");
    	int position = 0;
    	
    	for(String encodingItem : encodingItems){
    		// Build valued encoding from the current item ("gzip", "gzip;q=0.5", ...)
    		ValuedEncoding encoding = new ValuedEncoding(encodingItem, position);
    		// Don't insert 0 qValued encodings
    		if(encoding.qValue > 0)
    			tmp.add(encoding);
    		//High increment pace for wildcard insertion
    		position += 100;
    	}
    	
    	ValuedEncoding wildCard = null;
    	
    	//Search for wildcard
    	for(ValuedEncoding valuedEncoding : tmp){
    		//wildcard found
    		if(valuedEncoding.encoding.equals("*")){
    			wildCard = valuedEncoding;
    			break;
    		}
    	}
    	
    	// Wildcard found
    	if(wildCard != null){
        	// Retrieve all possible encodings
        	List<String> encodingsToAdd = Arrays.asList(EncodingNames.ALL_ENCODINGS);
        	// Remove wildcard from encodings, it will be replaced by encodings not yet found
    		tmp.remove(wildCard);
    		// Remove all already found encodings from available encodings
    		for(ValuedEncoding valuedEncoding : tmp){
    			encodingsToAdd.remove(valuedEncoding.encoding);
    		}
    		// Add remaining encodings with wildCard qValue and position (still incremented by 1 for each)
    		for(String remainingEncoding : encodingsToAdd){
    			tmp.add(new ValuedEncoding(remainingEncoding, wildCard.qValue, wildCard.position++));
    		}
    	}
    	
    	// Sort ValuedEncodings
    	Collections.sort(tmp);
    	
    	//Create the result List
    	for(ValuedEncoding encoding : tmp){
    		result.add(encoding.encoding);
    	}
    	
    	return result;
	}
}

class ValuedEncoding implements Comparable<ValuedEncoding>{
	String encoding = null;
	Double qValue = 1.0; 
	Integer position;
	
	public ValuedEncoding(String encodingName, Double qValue, int position){
		this.encoding = encodingName;
		this.qValue = qValue;
		this.position = position;
	}
	
	public ValuedEncoding(String encodingItem, int position){
		this.position = position;
		//Split an encoding item between encoding and its qValue
		String[] encodingParts = encodingItem.split(";");
		//Grab encoding name
		encoding = encodingParts[0].trim().replace("\n", "");
		//Grab encoding's qValue if it exists (default 1.0 otherwise)
		if(encodingParts.length > 1){
			qValue = Double.parseDouble(encodingParts[1].trim().replace("\n", ""));
		}
	}

	@Override
	public int compareTo(ValuedEncoding o) {
		if(qValue.equals(o.qValue)){ 
			// In case 2 encodings have the same qValue, the first one has priority
			return position.compareTo(o.position);
		}
		//Highest qValue first, invert default ascending comparison
		return qValue.compareTo(o.qValue) * -1;
	}
}

package org.wisdom.api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.wisdom.api.annotations.encoder.AllowEncoding;
import org.wisdom.api.annotations.encoder.DenyEncoding;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.EncodingNames;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.router.Route;

//TODO Maybe org.wisdom.api.utils is not the place for this class
public class EncodingHelper {
	
    /**
     * Parse a string to return an ordered list according to the Accept_Encoding HTTP Header grammar
     * @param acceptEncoding String to parse. Should be an Accept_Encoding header.
     * @return An ordered list of encodings
     */
    public static List<String> parseAcceptEncodingHeader(String acceptEncoding){
    	List<String> result = new ArrayList<String>();
    	// Intermediate list to sort encoding types
    	List<ValuedEncoding> tmp = new ArrayList<ValuedEncoding>();
    	
    	//Empty or null Accept_Encoding => return empty list
    	if(acceptEncoding == null || acceptEncoding.trim().equals("") || acceptEncoding.trim().equals("\n")){
    		return result;
    	}

    	//Parse Accept_Encoding for different encodings declarations
    	String[] encodingItems = acceptEncoding.split(",");
    	int position = 0;
    	
    	for(String encodingItem : encodingItems){
    		// Build valued encoding from the current item ("gzip", "gzp;q=0.5", ...)
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
    
    public static boolean shouldEncode(Renderable<?> renderable, ApplicationConfiguration config, Route route){
    	long renderableLength = renderable.length();
    	
    	//TODO Maybe we should continue but abort size lookup for size == -1
    	if(renderableLength <= 0)
    		return false;
    	
    	//TODO Discuss default max size
    	//TODO Do not request config value each time
    	int confMaxSize = config.getIntegerWithDefault(ApplicationConfiguration.ENCODING_MAX_SIZE, ApplicationConfiguration.DEFAULT_ENCODING_MAX_SIZE);
    	int methodMaxSize = -1, controllerMaxSize = -1;
    	
    	//TODO Discuss default min size
    	//TODO Do not request config value each time
    	int confMinSize = config.getIntegerWithDefault(ApplicationConfiguration.ENCODING_MIN_SIZE, ApplicationConfiguration.DEFAULT_ENCODING_MIN_SIZE);
    	int methodMinSize = -1, controllerMinSize = -1;
    	
    	//TODO Filter by extensions ?
    	
    	//TODO The default configuration (true here) should be discussed
    	//TODO Do not request config value each time
    	boolean configuration = config.getBooleanWithDefault(ApplicationConfiguration.ENCODING_GLOBAL, true);
    	boolean isAllowOnMethod = false, isDenyOnMethod = false, isAllowOnController = false, isDenyOnController = false;
    	
    	if(route != null){
    		//Retrieve @AllowEncoding on route method and, if exists, set a flag, compute max & min size
    		AllowEncoding allowOnMethod = route.getControllerMethod().getAnnotation(AllowEncoding.class);
    		isAllowOnMethod = allowOnMethod == null ? false : true;
    		methodMaxSize = isAllowOnMethod ? allowOnMethod.maxSize() : -1;
    		methodMinSize = isAllowOnMethod ? allowOnMethod.minSize() : -1;
    		//Retrieve @AllowEncoding on route controller and, if exists, set a flag, compute max & min size
    		AllowEncoding allowOnController = route.getControllerClass().getAnnotation(AllowEncoding.class);
    		isAllowOnController = allowOnController == null ? false : true;
    		controllerMaxSize = isAllowOnController ? allowOnController.maxSize() : -1;
    		controllerMinSize = isAllowOnController ? allowOnController.minSize() : -1;
    		//Retrieve @DenyEncoding on route method and route controller and set a flag
    		isDenyOnMethod = route.getControllerMethod().getAnnotation(DenyEncoding.class) == null ? false : true;
    		isDenyOnController = route.getControllerClass().getAnnotation(DenyEncoding.class) == null ? false : true;
    	}
    	
    	// Find max size first on method, then on controller and, if none, use default
    	int maxSize = methodMaxSize != -1 ? methodMaxSize : controllerMaxSize != -1 ? controllerMaxSize : confMaxSize;
    	// Find min size first on method, then on controller and, if none, use default
    	int minSize = methodMinSize != -1 ? methodMinSize : controllerMinSize != -1 ? controllerMinSize : confMinSize;
    	
    	// Ensure renderableLength is in min - max boundaries
    	if(renderableLength > maxSize || renderableLength < minSize)
    		return false;
    	
    	if(configuration){ // Configuration tells yes
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
}

class ValuedEncoding implements Comparable<ValuedEncoding>{
	String encoding = null;
	Double qValue = 1.0; //TODO Not sure that no qValue means 1.0
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
			// TODO not sure its true according to the standard
			// In case 2 encodings have the same qValue, the first one has priority
			return position.compareTo(o.position);
		}
		//Highest qValue first, invert default ascending comparison
		return qValue.compareTo(o.qValue) * -1;
	}
}

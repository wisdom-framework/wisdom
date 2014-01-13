package org.wisdom.api.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.wisdom.api.http.EncodingNames;

public class EncodingHelper {
	
    /**
     * Parse a string to return an ordered list according to the Accept_Encoding HTTP Header grammar
     * @param acceptEncoding String to parse. Should be an Accept_Encoding header
     * @return An ordered list of encodings
     */
    public static List<String> parseAcceptEncodingHeader(String acceptEncoding){
    	List<String> result = new ArrayList<String>();
    	List<ValuedEncoding> tmp = new ArrayList<ValuedEncoding>();
    	//Empty or null Accept_Encoding
    	if(acceptEncoding == null || acceptEncoding.trim().equals("") || acceptEncoding.trim().equals("\n")){
    		return result;
    	}
    	//TODO That's not the real meaning of "*"
    	/*
    	 The special "*" symbol in an Accept-Encoding field matches any
         available content-coding not explicitly listed in the header
         field.
    	 */
    	if(acceptEncoding.trim().equals("*")){
    		for(String encoding : EncodingNames.ALL_ENCODINGS)
    			result.add(encoding);
    		return result;
    	}

    	String[] encodingItems = acceptEncoding.split(",");
    	
    	int position = 0;
    	for(String encodingItem : encodingItems){
    		ValuedEncoding encoding = new ValuedEncoding(encodingItem, position++);
    		//Remove 0 qValued encodings
    		if(encoding.qValue > 0)
    			tmp.add(encoding);
    	}
    	Collections.sort(tmp);
    	
    	for(ValuedEncoding encoding : tmp){
    		result.add(encoding.encoding);
    	}
    	
    	return result;
    }
}

class ValuedEncoding implements Comparable<ValuedEncoding>{
	String encoding = null;
	Double qValue = 1.0; //TODO I am not sure that no qValue means 1.0
	Integer position;
	
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

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

public class ValuedEncoding implements Comparable<ValuedEncoding>{

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
            qValue = Double.parseDouble(encodingParts[1].trim().replace("\n", "").replace("q=", ""));
        }
    }
    
    public String getEncoding() {
        return encoding;
    }

    public Double getqValue() {
        return qValue;
    }

    public Integer getPosition() {
        return position;
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
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ValuedEncoding)){
            return false;
        }
        ValuedEncoding ov = (ValuedEncoding)obj;
        
        if(this.getPosition().equals(ov.getPosition()) && this.getEncoding().equals(ov.getEncoding()) && this.getqValue().equals(ov.getqValue())){
            return true;
        }
        return false;
    }
}
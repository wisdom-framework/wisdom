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
package org.wisdom.wisit.shell;

/**
 * @author Jonathan M. Bardin
 */
public class CommandResult {
    private String result = null;
    private String err = null;
    private Long timeStamp;

    public CommandResult() {
        this.timeStamp = System.currentTimeMillis();
    }

    public boolean isEmpty(){
    	return err == null && result == null;
    }

    public String toString(){
    	if(result != null){
    		return "res:"+result; 
    	}

    	if(err != null){
			return "err:"+err;
    	}

		//TODO log
    	return "";
    }
    
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

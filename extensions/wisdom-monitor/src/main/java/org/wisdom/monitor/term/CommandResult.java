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
package org.wisdom.monitor.term;

/**
 * A CommandResult encapsulate the result of a shell command as well at its error.
 *
 * @author Jonathan M. Bardin
 */
public class CommandResult {
    private String result;
    private String err;
    private Long timeStamp;

    /**
     * Create a new command result.
     */
    protected CommandResult() {
        this.timeStamp = System.currentTimeMillis();
    }

    /**
     * @return true if this CommandResult does not contain any result or error.
     */
    public boolean isEmpty(){
    	return err == null && result == null;
    }

    /**
     * If this CommandResult is a result it will start by <code>"res:"</code>.
     * Similarly if the result is an error it will start by <code>err:</code>
     *
     * @return The String representation of the command result.
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

    	if(result != null){
    		sb.append("res:");
            sb.append(result);
    	}

    	if(err != null){
			sb.append("err:");
            sb.append(err);
    	}

    	return sb.toString();
    }

    /**
     * @return The result part of this CommandResult.
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result The result part of this CommandResult.
     */
    protected void setResult(String result) {
        this.result = result;
    }

    /**
     * @return The error part of this CommandResult.
     */
    public String getErr() {
        return err;
    }

    /**
     * @param err The error part of this CommandResult.
     */
    protected void setErr(String err) {
        this.err = err;
    }

    /**
     * @return This CommandResult creation time stamp.
     */
    public Long getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp This CommandResult creation time stamp.
     */
    protected void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

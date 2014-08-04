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
package org.wisdom.monitor.extensions.terminal;

/**
 * A CommandResult encapsulate the result of a shell command as well at its error.
 */
public class CommandResult {
    private String content;
    private final Long timeStamp;
    private OutputType type;

    /**
     * Create a new command result.
     */
    protected CommandResult(OutputType pType) {
        this.timeStamp = System.currentTimeMillis();
        this.type = pType;
    }

    /**
     * @return true if this CommandResult does not contain any result or error.
     */
    public boolean isEmpty() {
        return content == null;
    }

    /**
     * If this CommandResult is a result it will start by <code>"res:"</code>.
     * Similarly if the result is an error it will start by <code>err:</code>
     *
     * @return The String representation of the command result.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        switch (type) {
            case ERR:
                sb.append("err:");
                break;
            case RESULT:
                sb.append("res:");
                break;
        }

        sb.append(content);

        return sb.toString();
    }

    /**
     * @return The content of this CommandResult.
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content The content CommandResult.
     */
    protected void setContent(String content) {
        this.content = content;
    }


    /**
     * @return type The OutputType of this CommandResult.
     */
    public OutputType getType() {
        return type;
    }

    /**
     * @param type The CommandResult OutputType.
     */
    protected void setType(OutputType type) {
        this.type = type;
    }

    /**
     * @return This CommandResult creation time stamp.
     */
    public Long getTimeStamp() {
        return timeStamp;
    }

}

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
package org.wisdom.wamp.data;

import java.util.Arrays;

/**
 * A structure checking complex argument, payload and result.
 */
public class Struct {
    private String message;
    private int count;
    private boolean flag;
    private String[] array;

    public Struct(String message, int count, boolean flag, String[] array) {
        this.message = message;
        this.count = count;
        this.flag = flag;
        this.array = array;
    }

    public Struct() {
        // used by jackson.
    }

    public String getMessage() {
        return message;
    }

    public int getCount() {
        return count;
    }

    public boolean getFlag() {
        return flag;
    }

    public boolean isFlag() {
        return flag;
    }

    public String[] getArray() {
        return array;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setArray(String[] array) {
        this.array = array;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Struct struct = (Struct) o;

        return count == struct.count && flag == struct.flag && Arrays.equals(array, struct.array) && message.equals(struct.message);

    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + count;
        result = 31 * result + (flag ? 1 : 0);
        result = 31 * result + Arrays.hashCode(array);
        return result;
    }
}

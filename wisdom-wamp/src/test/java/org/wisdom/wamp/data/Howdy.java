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

public class Howdy {


    public String howdy() {
        return "Hi!";
    }

    public void noop() {
    }

    public Struct struc() {
        return new Struct("hi", 1000, true, new String[] {"a", "b"});
    }

    public Struct complex(Struct s) {
        return s;
    }

    public boolean operation(String m, Struct s) {
        return m != null && s != null;
    }

    public void buggy() {
        throw new NullPointerException("I'm a bug");
    }
}

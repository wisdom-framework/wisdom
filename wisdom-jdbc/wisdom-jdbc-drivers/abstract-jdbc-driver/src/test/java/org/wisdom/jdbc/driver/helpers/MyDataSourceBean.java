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
package org.wisdom.jdbc.driver.helpers;

/**
 * A bean...
 */
public class MyDataSourceBean {


    String url;
    boolean flag;
    int count;
    long date;
    double d;

    byte b;
    short s;
    char c;
    float f;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setD(double d) {
        this.d = d;
    }

    public void setB(byte b) {
        this.b = b;
    }

    public void setC(char c) {
        this.c = c;
    }

    public void setS(short s) {
        this.s = s;
    }

    public void setF(float f) {
        this.f = f;
    }
}

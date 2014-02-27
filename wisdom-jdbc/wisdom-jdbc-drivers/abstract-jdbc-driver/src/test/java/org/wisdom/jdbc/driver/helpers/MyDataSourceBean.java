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

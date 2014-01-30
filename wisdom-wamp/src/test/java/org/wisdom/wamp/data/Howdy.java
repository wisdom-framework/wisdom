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

package org.wisdom.samples.wamp.logic;

/**
 * The awesome calc service.
 */
public class Calc {

    public int add(int a, int b) {
        return a + b;
    }

    public int sum(int... numbers) {
        int acc = 0;
        for (int n : numbers) {
            acc += n;
        }
        return acc;
    }
}

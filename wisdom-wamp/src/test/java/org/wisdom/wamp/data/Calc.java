package org.wisdom.wamp.data;

public class Calc {

    public int add(int a, int b) {
        return a + b;
    }

    public int sum(int... numbers) {
        int sum = 0;
        for (int i : numbers) {
            sum += i;
        }
        return sum;
    }

}

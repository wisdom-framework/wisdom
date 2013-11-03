package org.ow2.chameleon.wisdom.test.init;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.wisdom.test.WisdomRunner;

/**
 *
 */
@RunWith(WisdomRunner.class)
public class TestRunner {

    BundleContext context;

    @Before
    public void setUp() {
        System.out.println("Before");
    }

    @Before
    public void tearDown() {
        System.out.println("After");
    }

    @Test
    public void test1() {
        System.out.println(context);
    }

    @Test
    public void test2() {
        Thread.dumpStack();
    }

    @Test
    public void test3() {
        //Assert.fail();
    }
}

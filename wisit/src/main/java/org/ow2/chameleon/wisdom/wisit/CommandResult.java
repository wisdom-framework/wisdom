package org.ow2.chameleon.wisdom.wisit;

/**
 * Created with IntelliJ IDEA.
 * User: barjo
 * Date: 11/21/13
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandResult {
    public String content;
    public String err;
    public Long timeStamp;

    public CommandResult() {
        this.timeStamp = System.currentTimeMillis();
    }
}

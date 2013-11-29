package org.ow2.chameleon.wisdom.wisit.shell;

/**
 * @author Jonathan M. Bardin
 */
public class CommandResult {
    public String content;
    public String err;
    public Long timeStamp;

    public CommandResult() {
        this.timeStamp = System.currentTimeMillis();
    }
}

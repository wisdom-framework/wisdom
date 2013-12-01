package org.ow2.chameleon.wisdom.wisit.shell;

/**
 * @author Jonathan M. Bardin
 */
public class CommandResult {
    public String result = null;
    public String err = null;
    public Long timeStamp;

    public CommandResult() {
        this.timeStamp = System.currentTimeMillis();
    }

    public boolean isEmpty(){
    	return err == null && result == null;
    }

    public String toString(){
    	if(result != null){
    		return "res:"+result; 
    	}

    	if(err != null){
			return "err:"+err;
    	}

		//TODO log
    	return "";
    }
}

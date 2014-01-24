package org.wisdom.wisit.shell;

/**
 * @author Jonathan M. Bardin
 */
public class CommandResult {
    private String result = null;
    private String err = null;
    private Long timeStamp;

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
    
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

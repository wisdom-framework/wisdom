package org.ow2.chameleon.wisdom.wisit;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: barjo
 * Date: 11/21/13
 * Time: 10:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class WisitSession {

    /**
     * Gogo shell session
     */
    private CommandSession shellSession;

    public WisitSession(final CommandProcessor processor, PrintStream out) {
        //We use the same stream for the output and the error
        //TODO wrap the error stream
        shellSession = processor.createSession(null,out,out);
    }

    public void close(){
        shellSession.close();
    }


    public CommandResult exec(String commandLine) {
        CommandResult result = new CommandResult();

        try {
            Object raw = shellSession.execute(commandLine);

            if(raw != null){
                result.content = shellSession.format(raw, Converter.INSPECT).toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.err = e.getMessage();
        }

        return result;
    }
}

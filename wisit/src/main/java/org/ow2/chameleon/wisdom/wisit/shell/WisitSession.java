package org.ow2.chameleon.wisdom.wisit.shell;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;
import org.ow2.chameleon.wisdom.api.http.websockets.Publisher;

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
    private final CommandSession shellSession;


    public WisitSession(final CommandProcessor processor,final Publisher publisher,final String topic) {
        WisitPrintStream printStream = new WisitPrintStream(this,publisher,topic);

        //We use the same stream for the output and the error
        //TODO wrap the error stream
        shellSession = processor.createSession(null,printStream,printStream);
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

    public String format(Object o) {
        return shellSession.format(o, Converter.INSPECT).toString();
    }
}

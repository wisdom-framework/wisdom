package org.ow2.chameleon.wisdom.wisit.shell;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;
import org.ow2.chameleon.wisdom.api.http.websockets.Publisher;

import java.io.PrintStream;

import static org.ow2.chameleon.wisdom.wisit.shell.WisitOutputStream.OutputType;

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
        WisitOutputStream resultStream = new WisitOutputStream(publisher,topic);
        WisitOutputStream errorStream = new WisitOutputStream(publisher,topic, OutputType.err);

        shellSession = processor.createSession(null, new PrintStream(resultStream,true),
                                                     new PrintStream(errorStream,true));
    }

    public void close(){
        shellSession.close();
    }


    public CommandResult exec(String commandLine) {
        CommandResult result = new CommandResult();

        try {
            Object raw = shellSession.execute(commandLine);

            if(raw != null){
                result.result = shellSession.format(raw, Converter.INSPECT).toString();
            }

        } catch (Exception e) {
            result.err = e.getMessage();
        }

        return result;
    }

    public String format(Object o) {
        return shellSession.format(o, Converter.INSPECT).toString();
    }
}

package org.ow2.chameleon.wisdom.wisit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;
import org.apache.felix.ipojo.annotations.*;
import org.fusesource.jansi.AnsiString;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.*;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.http.websockets.Publisher;
import org.ow2.chameleon.wisdom.api.router.Router;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;
import org.ow2.shelbie.core.console.util.Streams;
import org.ow2.shelbie.core.registry.CommandRegistry;
import org.ow2.shelbie.core.registry.info.CommandInfo;

import javax.activation.MimeType;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple controller to manage a todo list (in memory).
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
public class WisitShellController extends DefaultController {

    private WisitSession shellSession;



    @Requires
    private CommandRegistry commandRegistry;

    @Requires
    private CommandProcessor processor;

    @Requires
    private Publisher publisher;

    @Requires
    private Router router;

    @Validate
    private void start(){
        shellSession = new WisitSession(processor,new WisitPrintStream(publisher,"/wisit/stream"));
    }

    @Invalidate
    private void stop(){
        shellSession.close();
    }

    @Opened("/wisit/stream")
    public void open(){
        System.out.print("[WS] open");
    }

    @Route(method = HttpMethod.GET,uri = "/wisit/stream")
    public Result ping(){
        return ok();
    }

    @Closed("/wisit/stream")
    public void close(){
        System.out.print("[WS] close");
    }


    @Route(method = HttpMethod.GET, uri = "/wisit/command")
    public Result commands() {
        return ok(getCommands()).json();
    }

    @Route(method = HttpMethod.POST, uri = "/wisit/command/{name}")
    public Result command(@Parameter("name") String name,@Body String args) {
        CommandResult result = shellSession.exec(name+" "+args);
        return status(Result.OK).render(result).json();
    }


    public List<String> getCommands(){
        List<String> commands = new ArrayList<String>();

        Collection<? extends CommandInfo> commandInfos = commandRegistry.getAllCommands();

        for (CommandInfo info : commandInfos){
            commands.add(info.getName());
        }

        return commands;
    }
}

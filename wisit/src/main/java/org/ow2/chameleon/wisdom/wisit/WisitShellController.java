package org.ow2.chameleon.wisdom.wisit;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.CommandProcessor;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.*;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.http.websockets.Publisher;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.wisit.shell.CommandResult;
import org.ow2.chameleon.wisdom.wisit.auth.WisitAuthService;
import org.ow2.chameleon.wisdom.wisit.shell.WisitSession;
import org.ow2.shelbie.core.registry.CommandRegistry;
import org.ow2.shelbie.core.registry.info.CommandInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
public class WisitShellController extends DefaultController {

    private WisitSession shellSession;

    @Requires
    private WisitAuthService authService;

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
        shellSession = new WisitSession(processor,publisher,"/wisit/stream");
    }

    @Invalidate
    private void stop(){
        shellSession.close();
    }

    @Opened("/wisit/stream")
    public void open(){ }

    @Closed("/wisit/stream")
    public void close(){ }

    @Route(method = HttpMethod.GET,uri = "/wisit/stream")
    public Result ping(){
        if(!authService.isAuthorised()){
            return unauthorized();
        }

        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/wisit/command")
    public Result commands() {
        if(!authService.isAuthorised()){
            return unauthorized();
        }
        return ok(getCommands()).json();
    }

    @Route(method = HttpMethod.POST, uri = "/wisit/command/{name}")
    public Result command(@Parameter("name") String name,@Body String args) {
        if(!authService.isAuthorised()){
            return unauthorized();
        }

        CommandResult result = shellSession.exec(name+" "+args);
        
        if(result.isEmpty()){
            return ok();
        }

        return ok(result.toString());
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

/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.monitor.term;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.CommandProcessor;
import org.ow2.shelbie.core.registry.CommandRegistry;
import org.ow2.shelbie.core.registry.info.CommandInfo;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.security.Authenticated;

import java.util.Collection;
import java.util.HashSet;

import static org.wisdom.api.http.HttpMethod.*;

import org.wisdom.api.Controller;

/**
 * The Wisit Terminal allows for an user to run commands from the web.
 *
 * POST /monitor/terminal/wisit/command/{name} -> run the {name} command, arguments in body
 * GET  /monitor/terminal/wisit/command -> return the json list of available commands
 *
 * Command result are published on the `/wisit/stream` websocket.
 *
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
@Path("/monitor/terminal/wisit")
public class WisitShellController extends DefaultController {

    private WisitSession shellSession;

    @Requires
    private CommandRegistry commandRegistry;

    @Requires
    private CommandProcessor processor;

    @Requires
    private Publisher publisher;

    @Validate
    private void start(){
        shellSession = new WisitSession(processor,publisher,"/wisit/stream");
    }

    @Invalidate
    private void stop(){
        shellSession.close();
    }

    @Opened("/wisit/stream")
    public void open(){ 
        //Unused
    }

    @Closed("/wisit/stream")
    public void close(){ 
        //Unused
    }

    /**
     * Ping.
     * @return OK
     */
    @Route(method = OPTIONS,uri = "/")
    public Result ping(){
        return ok();
    }

    @Authenticated(value = WisitAuthController.class)
    @Route(method = GET, uri = "/command")
    public Result commands() {
        return ok(getCommands()).json();
    }

    @Authenticated(value = WisitAuthController.class)
    @Route(method = POST, uri = "/command/{name}")
    public Result command(@Parameter("name") String name,@Body String args) {
        CommandResult result = shellSession.exec(name+" "+args);

        if(result.isEmpty()){
            return ok();
        }

        return ok(result.toString());
    }

    /**
     * @return The collection of commands available in the terminal.
     */
    public Collection<String> getCommands(){
        Collection<String> commands = new HashSet<>();
        Collection<? extends CommandInfo> commandInfos = commandRegistry.getAllCommands();

        for (CommandInfo info : commandInfos){
            commands.add(info.getName());
        }

        return commands;
    }
}

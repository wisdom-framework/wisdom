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
package org.wisdom.wisit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command.CommandProcessor;
import org.ow2.shelbie.core.registry.CommandRegistry;
import org.ow2.shelbie.core.registry.info.CommandInfo;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Closed;
import org.wisdom.api.annotations.Opened;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.wisit.auth.WisitAuthService;
import org.wisdom.wisit.shell.CommandResult;
import org.wisdom.wisit.shell.WisitSession;

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

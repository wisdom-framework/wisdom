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
package org.wisdom.monitor.extensions.terminal;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.CommandProcessor;
import org.ow2.shelbie.core.registry.CommandRegistry;
import org.ow2.shelbie.core.registry.info.CommandInfo;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.extensions.security.MonitorAuthenticator;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.Collection;
import java.util.HashSet;

import static org.wisdom.api.http.HttpMethod.*;

/**
 * The Wisit Terminal allows for an user to run commands from the web.
 * <p/>
 * POST /monitor/terminal/wisit/command/{name} -> run the {name} command, arguments in body
 * GET  /monitor/terminal/wisit/command -> return the json list of available commands
 * <p/>
 * Command result are published on the `/wisit/stream` websocket.
 */
@Controller
@Path("/monitor/terminal")
@Authenticated(MonitorAuthenticator.class)
public class WisitShellController extends DefaultController implements MonitorExtension {

    private WisitSession shellSession;

    @Requires
    private CommandRegistry commandRegistry;

    @Requires
    private CommandProcessor processor;

    @Requires
    private Publisher publisher;

    @View("monitor/terminal")
    Template terminal;

    @Validate
    private void start() {
        shellSession = new WisitSession(processor, publisher, "/monitor/terminal/stream");
    }

    @Invalidate
    private void stop() {
        shellSession.close();
    }


    @Route(method = HttpMethod.GET, uri = "")
    public Result terminal() {
        return ok(render(terminal));
    }


    /**
     * Ping.
     *
     * @return OK
     */
    @Route(method = OPTIONS, uri = "")
    public Result ping() {
        return ok();
    }

    @Route(method = GET, uri = "/command")
    public Result commands() {
        return ok(getCommands()).json();
    }

    @Route(method = POST, uri = "/command/{name}")
    public Result command(@Parameter("name") String name, @Body String args) {
        CommandResult result = shellSession.exec(name + " " + args);

        if (result.isEmpty()) {
            return ok();
        }

        return ok(result.toString());
    }

    /**
     * @return The collection of commands available in the terminal.
     */
    public Collection<String> getCommands() {
        Collection<String> commands = new HashSet<>();
        Collection<? extends CommandInfo> commandInfos = commandRegistry.getAllCommands();

        for (CommandInfo info : commandInfos) {
            commands.add(info.getName());
        }

        return commands;
    }

    @Override
    public String label() {
        return "Shell";
    }

    @Override
    public String url() {
        return "/monitor/terminal";
    }

    @Override
    public String category() {
        return "osgi";
    }
}

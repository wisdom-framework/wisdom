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
package org.wisdom.wisit.shell;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;
import org.wisdom.api.http.websockets.Publisher;

import java.io.PrintStream;

import static org.wisdom.wisit.shell.WisitOutputStream.OutputType;

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
        WisitOutputStream errorStream = new WisitOutputStream(publisher,topic, OutputType.ERR);

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
                result.setResult(shellSession.format(raw, Converter.INSPECT).toString());
            }

        } catch (Exception e) {
            result.setErr(e.getMessage());
        }

        return result;
    }

    public String format(Object o) {
        return shellSession.format(o, Converter.INSPECT).toString();
    }
}

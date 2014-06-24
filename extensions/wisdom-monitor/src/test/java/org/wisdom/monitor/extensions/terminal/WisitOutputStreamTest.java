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
/*
 * Copyright 2014, Technologic Arts Vietnam.
 * All right reserved.
 */

package org.wisdom.monitor.extensions.terminal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.wisdom.api.http.websockets.Publisher;

import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.wisdom.monitor.extensions.terminal.OutputType.ERR;
import static org.wisdom.monitor.extensions.terminal.OutputType.RESULT;

/**
 * Test the WisitOutputStream.
 *
 * @author <a href="mailto:jbardin@tech-arts.com">Jonathan M. Bardin</a>
 */
public class WisitOutputStreamTest {

    @Mock
    private Publisher mockpub;


    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    //
    // Constructor test
    //

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenPublisherNull(){
        new WisitOutputStream(null,"default");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenTopicNull(){
        new WisitOutputStream(mockpub,null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenOutputTypeIsNull(){
        new WisitOutputStream(mockpub,"default",null);
    }

    @Test
    public void shouldCreateObjectWhenValidArguments(){
        assertThat(new WisitOutputStream(mockpub, "default", RESULT)).isNotNull();
    }

    @Test
    public void shouldCreateOutputTypeResultByDefault(){
        assertThat((new WisitOutputStream(mockpub,"default")).getType()).isEqualTo(RESULT);
    }

    //
    // getType() test
    //

    @Test
    public void shouldReturnOutputTypeErr(){
        assertThat((new WisitOutputStream(mockpub,"default", ERR)).getType()).isEqualTo(ERR);
    }

    //
    // write operations
    //

    @Test
    public void shouldPublishAResultWhenWritingAResultString(){
        String droids = "This is not the test you are looking for.";
        //GIVEN
        PrintStream ps = new PrintStream(new WisitOutputStream(mockpub,"default"));

        //WHEN
        ps.print(droids);
        ps.close();

        //THEN
        CommandResult result = new CommandResult(RESULT);
        result.setContent(droids);

        verify(mockpub).publish("default", result.toString());
        verifyNoMoreInteractions(mockpub);
    }
}

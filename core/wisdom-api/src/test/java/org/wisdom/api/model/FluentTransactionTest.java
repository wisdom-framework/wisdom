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
package org.wisdom.api.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;

/**
 * Test case for the FluentTransaction abstract class.
 */
public class FluentTransactionTest {

    @Mock
    private TransactionManager txManager;

    @Mock
    private Callable<Object> content;

    @Mock
    private RolledBackHandler rolledBackHandler;

    @Mock
    private CommittedHandler<Object> committedHandler;

    private FluentTransaction<Object> ftx;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        ftx = FluentTransaction.transaction(txManager);
    }

    @Test
    public void shouldCallBeginCommitCloseWhenTransactionIsExecuted() throws Exception{
        //When
        ftx.with(content).onRolledBack(rolledBackHandler).execute();

        //Then
        verify(txManager).begin();
        verify(txManager).commit();
        verify(txManager).close();
        verifyNoMoreInteractions(txManager);
        verify(content).call();
        verifyNoMoreInteractions(content);
        verifyZeroInteractions(rolledBackHandler);
    }

    @Test
    public void shouldCallOnCommittedCallBackWhenTransactionWithOnCommitedIsExecuted() throws Exception{
        //When
        ftx.with(content).onRolledBack(rolledBackHandler).onCommitted(committedHandler).execute();

        //Then
        verify(txManager).begin();
        verify(txManager).commit();
        verify(txManager).close();
        verifyNoMoreInteractions(txManager);
        verify(content).call();
        verify(committedHandler).committed(any(Object.class));
        verifyNoMoreInteractions(committedHandler);
        verifyNoMoreInteractions(content);
        verifyZeroInteractions(rolledBackHandler);
    }


    @Test
    public void shouldCallBeginRollBackCloseWhenTransactionIsExecutedAndFail() throws Exception{
        //Given
        when(content.call()).thenThrow(new Exception());

        //When
        ftx.with(content).onRolledBack(rolledBackHandler).execute();

        //Then
        verify(txManager).begin();
        verify(txManager).close();
        verify(txManager).rollback();
        verifyNoMoreInteractions(txManager);
        verify(content).call();
        verifyNoMoreInteractions(content);

        verify(rolledBackHandler).rolledBack(any(Exception.class));
        verifyZeroInteractions(rolledBackHandler);
    }
}

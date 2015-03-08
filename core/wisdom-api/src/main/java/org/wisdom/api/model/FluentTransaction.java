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

import java.util.concurrent.Callable;

/**
 * A FluentTransaction ease the creation of database transaction linked with a {@link Crud} service.
 *
 * The fluent transaction use a {@link TransactionManager} in order to run the transaction.
 * The fluent requires a {@link Callable}, that will be the code block to be run in the transaction as well as a
 * {@link RolledBackHandler} that will be call if the transaction has been rollback.
 *
 * An optional  {@link CommittedHandler} that will be call on successful commit is also supported.
 *
 * {@code
 *     Grammar  ::= 'transaction(' TransactionManager ')'
 *                  '.with(' Callable ')'
 *                  '.onRolledBack(' RolledBackHandler ')'
 *                  ( '.onCommitted(' CommittedHandler ')' )?
 *                  '.execute()
 * }
 *
 * @param <R> the return type of the transaction block.
 * @author barjo
 */
public final class FluentTransaction<R> {
    private Callable<R> txContent;
    private RolledBackHandler txOnRolledBack;
    private CommittedHandler<R> txOnCommitted = null;

    private final TransactionManager txManager;

    /**
     * Create a new FluentTransaction
     * @param txManager The {@link TransactionManager} used to manage the transaction,begin,commit,rollback.
     */
    private FluentTransaction(TransactionManager txManager) {
        this.txManager = txManager;
    }

    /**
     * Create a new FluentTransaction
     * @param txManager The {@link TransactionManager} used to manage the transaction,begin,commit,rollback.
     */
    public static FluentTransaction transaction(TransactionManager txManager){
        return new FluentTransaction<>(txManager);
    }

    /**
     * @return The {@link TransactionManager} used by this FluentTransaction.
     */
    public TransactionManager getTransactionManager(){
        return txManager;
    }

    /**
     * The block that will be executed in a transaction.
     *
     * @param content The transaction block.
     * @return Intermediate, This FluentTransaction with the transaction block defined.
     */
    public Intermediate with(Callable<R> content){
        txContent = content;
        return new Intermediate();
    }

    /**
     * The Intermediate FluentTransaction contains the content of the transaction but does not have a defined
     * {@link RolledBackHandler} yet.
     */
    public final class Intermediate {
        private Intermediate(){}

        /**
         * Set the {@link RolledBackHandler} that will be call if the transaction block failed and has been rollback.
         * @param rollcall The {@link RolledBackHandler} that will be call
         * @return OptionalIntermediate, This FluentTransaction that can be executed.
         */
        public OptionalIntermediate onRolledBack(RolledBackHandler rollcall){
            txOnRolledBack = rollcall;
            return new OptionalIntermediate();
        }
    }

    /**
     * The OptionalIntermediate FluentTransaction has a content and a {@link RolledBackHandler} set, it can be executed.
     * An optional {@link CommittedHandler} can be set too.
     */
    public final class OptionalIntermediate extends Ready {
        private OptionalIntermediate(){}

        /**
         * Set the optional {@link CommittedHandler} handler that will be call if this FluentTransaction
         * has been properly committed.
         * @param onCommitted The {@link CommittedHandler} that will be call on successful commit.
         * @return This FluentTransaction ready to be executed.
         */
        public Ready onCommitted(CommittedHandler<R> onCommitted){
            txOnCommitted = onCommitted;
            return new Ready();
        }

    }

    /**
     * The FluentTransaction.Ready is ready to be executed.
     */
    public class Ready {
        private Ready(){}

        /**
         * Execute the transaction.
         * @throws InitTransactionException if A problem occurred while the transaction is initiated.
         * @throws RollBackHasCauseAnException if A problem occurred while the transaction is being rollback.
         */
        public void execute() throws InitTransactionException,RollBackHasCauseAnException {
            txManager.begin();

            try{
                R result = txContent.call();
                txManager.commit();
                if(txOnCommitted != null){
                    txOnCommitted.committed(result);
                }
            }catch (Exception cause){
                txManager.rollback();
                txOnRolledBack.rolledBack(cause);
            } finally {
                txManager.close();
            }
        }
    }
}

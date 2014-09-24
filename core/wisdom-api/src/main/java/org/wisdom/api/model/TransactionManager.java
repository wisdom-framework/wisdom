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

/**
 * The TransactionManager  allows for managing transactions.
 * Each instance of a {@link FluentTransaction}use a TransactionManager.
 *
 * @author barjo
 */
public interface TransactionManager {
    /**
     * Create a new transaction and associate it to the current thread.
     *
     * @throws InitTransactionException if an exception occurred while beginning the transaction.
     */
    void begin() throws InitTransactionException;

    /**
     * Commit the transaction associated with the current thread in the database.
     *
     * @throws Exception If an Exception occurred while committing a transaction.
     */
    void commit() throws Exception;

    /**
     * Rollback the transaction associated with the current thread.
     *
     * @throws RollBackHasCauseAnException if an exception occurred while rolling back the transaction.
     */
    void rollback() throws RollBackHasCauseAnException;

    /**
     * Close the transaction associated with the current thread and release the database if necessary.
     */
    void close();
}

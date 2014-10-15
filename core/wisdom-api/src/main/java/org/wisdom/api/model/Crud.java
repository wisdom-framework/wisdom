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

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * A service to let components access entities stored in a repository (i.e. persistence layer).
 * This service manages only one type of entity (i.e. class). For example, the entity Task would have its own
 * CrudService<Task> while the entity User would have another one.
 *
 * The CRUD Service is independent of the persistent layer used. If you need to access low-level API,
 * use the getRepository method.
 *
 * Providers must register the 'entity.class' and 'entity.classname' properties in their service registration. The
 * first property register the class object, while the second only its name.
 *
 * @param <T> the type of the entity
 * @param <I> the type of the ids
 */
public interface Crud<T, I extends Serializable> {

    /**
     * The service property that <strong>must</strong> be published to indicate the entity class.
     */
    public static final String ENTITY_CLASS_PROPERTY = "entity.class";

    /**
     * The service property that <strong>must</strong> be published to indicate the entity qualified class name.
     */
    public static final String ENTITY_CLASSNAME_PROPERTY = "entity.classname";

    /**
     * Gets the class of the represented entity.
     * @return the entity's class.
     */
    Class<T> getEntityClass();

    /**
     * Gets the class of the Id used by the persistent layer.
     * @return the type of the id.
     */
    Class<I> getIdClass();

    /**
     * Deletes the given entity instance. The instance is removed from the persistent layer.
     * @param t the instance
     * @return the entity instance, may be the same as the parameter t but can also be different
     */
    T delete(T t);

    /**
     * Deletes the given entity instance (specified by its id). The instance is removed from the persistent layer.
     * @param id the id
     */
    void delete(I id);

    /**
     * Deletes the given entity instances. The instances are removed from the persistent layer.
     * @param entities the entities to remove from the persistent layer
     * @return the set of entity instances
     */
    Iterable<T> delete(Iterable<T> entities);

    /**
     * Saves a given entity. Use the returned instance for further operations as the operation might have
     * changed the entity instance completely.
     *
     * This method is used to save a new entity or to update it.
     * @param t the instance to save
     * @return the saved entity
     */
    T save(T t);

    /**
     * Saves all given entities. Use the returned instances for further operations as the operation might have
     * changed the entity instances completely.
     * @param entities the entities to save, must not contains {@literal null} values
     * @return the saved entities
     */
    Iterable<T> save(Iterable<T> entities);

    /**
     * Retrieves an entity by its id.
     * @param id the id, must not be null
     * @return the entity instance, {@literal null} if there are no entities matching the given id.
     */
    T findOne(I id);

    /**
     * Retrieves the entity matching the given filter. If several entities matches, the first is returned.
     * @param filter the filter
     * @return the first matching instance, {@literal null} if none
     */
    T findOne(EntityFilter<T> filter);

    /**
     * Checks whether an entity instance with the given id exists, i.e. has been saved and is persisted.
     * @param id the id, must not be null
     * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
     */
    boolean exists(I id);

    /**
     * Returns all instances of the entity.
     * @return the instances, empty if none.
     */
    Iterable<T> findAll();

    /**
     * Returns all instances of the type with the given IDs.
     * @param ids the ids.
     * @return the instances, empty if none.
     */
    Iterable<T> findAll(Iterable<I> ids);

    /**
     * Retrieves the entities matching the given filter.
     * Be aware that the implementation may load all stored entities in memory to retrieve the right set of entities.
     * @param filter the filter
     * @return the matching instances, empty if none.
     */
    Iterable<T> findAll(EntityFilter<T> filter);

    /**
     * Gets the number of stored instances.
     * @return the number of stored instances, 0 if none.
     */
    long count();

    /**
     * Gets the repository storing the instances of this entity.
     * @return the repository object, may be {@literal null} if there are no repository.
     */
    Repository getRepository();

    /**
     * Executes the given runnable in a transaction. If the block throws an exception, the transaction is rolled back.
     * This method may not be supported by all persistent technologies, as they are not necessary supporting
     * transactions. In that case, this method throw a {@link java.lang.UnsupportedOperationException}.
     *
     * @param runnable the runnable to execute in a transaction
     * @throws HasBeenRollBackException if the transaction has been rollback.
     * @throws java.lang.UnsupportedOperationException if transactions are not supported.
     * @throws org.wisdom.api.model.InitTransactionException if an exception occurred before running the transaction block.
     * @throws RollBackHasCauseAnException if an exception occurred when the transaction is rollback.
     */
    void executeTransactionalBlock(Runnable runnable) throws HasBeenRollBackException;

    /**
     * Executes the given runnable in a transaction. If the block throws an exception, the transaction is rolled back.
     * This method may not be supported by all persistent technologies, as they are not necessary supporting
     * transactions. In that case, this method throw a {@link java.lang.UnsupportedOperationException}.
     * @param callable the block to execute in a transaction
     * @return A the result
     * @throws HasBeenRollBackException if the transaction has been rollback.
     * @throws java.lang.UnsupportedOperationException if transactions are not supported.
     * @throws org.wisdom.api.model.InitTransactionException if an exception occurred before running the transaction block.
     * @throws RollBackHasCauseAnException if an exception occurred when the transaction is rollback.
     */
    <A> A executeTransactionalBlock(Callable<A> callable) throws HasBeenRollBackException;

    /**
     * @return The {@link TransactionManager} used by this crud in order to run the transaction.
     */
    TransactionManager getTransactionManager();

    /**
     * Create a FluentTransaction with this Crud service,
     *
     * @param <R> the return type of the transaction block.
     * @return a new FluentTransaction.
     * @throws java.lang.UnsupportedOperationException if transactions are not supported.
     * @throws org.wisdom.api.model.InitTransactionException if an exception occurred before running the transaction block.
     * @throws RollBackHasCauseAnException if an exception occurred when the transaction is rollback.
     */
    <R> FluentTransaction<R> transaction();

    /**
     * Create a FluentTransaction, with the given transaction block.
     *
     * @param <R> the return type of the transaction block
     * @param callable, The transaction block to be executed by the returned FluentTransaction
     * @return a new FluentTransaction with a transaction block already defined.
     * @throws java.lang.UnsupportedOperationException if transactions are not supported.
     */
    <R> FluentTransaction<R>.Intermediate transaction(Callable<R> callable);
}

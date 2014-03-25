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
package org.wisdom.ebean.runtime;

import com.avaje.ebean.EbeanServer;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.wisdom.api.model.Crud;
import org.wisdom.api.model.EntityFilter;
import org.wisdom.api.model.Repository;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by clement on 19/02/2014.
 */
public class EbeanCrudService<T> implements Crud<T, Long> {
    private final EbeanServer server;
    private final Class<T> clazz;
    private final EbeanRepository repository;

    public EbeanCrudService(EbeanRepository repository, Class<T> clazz) {
        this.server = repository.get();
        this.repository = repository;
        this.clazz = clazz;
    }

    /**
     * Gets the class of the represented entity.
     *
     * @return the repository managing this entity.
     */
    @Override
    public Class<T> getEntityClass() {
        return clazz;
    }

    /**
     * Gets the class of the Id used by the persistent layer.
     *
     * @return the type of the id.
     */
    @Override
    public Class<Long> getIdClass() {
        return Long.class;
    }

    /**
     * Deletes the given entity instance. The instance is removed from the persistent layer.
     *
     * @param t the instance
     * @return the entity instance, may be the same as the parameter t but can also be different
     */
    @Override
    public T delete(T t) {
        server.delete(t);
        return t;
    }

    /**
     * Deletes the given entity instance (specified by its id). The instance is removed from the persistent layer.
     *
     * @param id the id
     * @return the entity instance, may be the same as the parameter t but can also be different
     */
    @Override
    public void delete(Long id) {
        server.delete(clazz, id);
    }

    /**
     * Deletes the given entity instances. The instances are removed from the persistent layer.
     *
     * @param entities the entities to remove from the persistent layer
     * @return the set of entity instances
     */
    @Override
    public Iterable<T> delete(Iterable<T> entities) {
        server.delete(entities);
        return entities;
    }

    /**
     * Saves a given entity. Use the returned instance for further operations as the operation might have
     * changed the entity instance completely.
     * <p/>
     * This method is used to save a new entity or to update it.
     *
     * @param t the instance to save
     * @return the saved entity
     */
    @Override
    public T save(T t) {
        server.save(t);
        return t;
    }

    /**
     * Saves all given entities. Use the returned instances for further operations as the operation might have
     * changed the entity instances completely.
     *
     * @param entities the entities to save, must not contains {@literal null} values
     * @return the saved entities
     */
    @Override
    public Iterable<T> save(Iterable<T> entities) {
        server.save(entities);
        return entities;
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id the id, must not be null
     * @return the entity instance, {@literal null} if there are no entities matching the given id.
     */
    @Override
    public T findOne(Long id) {
        return server.find(clazz, id);
    }

    /**
     * Retrieves the entity matching the given filter. If several entities matches, the first is returned.
     *
     * @param filter the filter
     * @return the first matching instance, {@literal null} if none
     */
    @Override
    public T findOne(final EntityFilter<T> filter) {
        return Iterables.get(findAll(filter), 0, null);
    }

    /**
     * Checks whether an entity instance with the given id exists, i.e. has been saved and is persisted.
     *
     * @param id the id, must not be null
     * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
     */
    @Override
    public boolean exists(Long id) {
        return findOne(id) != null;
    }

    /**
     * Returns all instances of the entity.
     *
     * @return the instances, empty if none.
     */
    @Override
    public Iterable<T> findAll() {
        return server.find(clazz).findList();
    }

    /**
     * Returns all instances of the type with the given IDs.
     *
     * @param ids the ids.
     * @return the instances, empty if none.
     */
    @Override
    public Iterable<T> findAll(Iterable<Long> ids) {
        List<?> list = Lists.newArrayList(ids);
        return server.createQuery(clazz).where().idIn(list).query().findList();
    }

    /**
     * Retrieves the entities matching the given filter.
     * Be aware that the implementation may load all stored entities in memory to retrieve the right set of entities.
     *
     * @param filter the filter
     * @return the matching instances, empty if none.
     */
    @Override
    public Iterable<T> findAll(final EntityFilter<T> filter) {
        return Iterables.filter(findAll(), new Predicate<T>() {
            @Override
            public boolean apply(T t) {
                return filter.accept(t);
            }
        });
    }

    /**
     * Gets the number of stored instances.
     *
     * @return the number of stored instances, 0 if none.
     */
    @Override
    public long count() {
        return Iterables.size(findAll());
    }

    /**
     * Gets the repository storing the instances of this entity.
     *
     * @return the repository object, may be {@literal null} if there are no repository.
     */
    @Override
    public Repository getRepository() {
        return repository;
    }

    /**
     * Executes the given runnable in a transaction. If the block throws an exception, the transaction is rolled back.
     * This method may not be supported by all persistent technologies, as they are not necessary supporting
     * transactions. In that case, this method throw a {@link UnsupportedOperationException}.
     *
     * @param runnable the runnable to execute in a transaction
     * @throws UnsupportedOperationException if transactions are not supported.
     */
    @Override
    public void executeTransactionalBlock(Runnable runnable) {
        server.beginTransaction();
        try {
            runnable.run();
            server.commitTransaction();
        } catch (Exception e) {
            server.rollbackTransaction();
        } finally {
            server.endTransaction();
        }
    }

    /**
     * Executes the given runnable in a transaction. If the block throws an exception, the transaction is rolled back.
     * This method may not be supported by all persistent technologies, as they are not necessary supporting
     * transactions. In that case, this method throw a {@link UnsupportedOperationException}.
     *
     * @param callable the block to execute in a transaction
     * @return A the result
     * @throws UnsupportedOperationException if transactions are not supported.
     */
    @Override
    public <A> A executeTransactionalBlock(Callable<A> callable) {
        server.beginTransaction();
        try {
            A result = callable.call();
            server.commitTransaction();
            return result;
        } catch (Exception e) {
            server.rollbackTransaction();
            return null;
        } finally {
            server.endTransaction();
        }
    }
}

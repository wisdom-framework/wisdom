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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.model.Crud;
import org.wisdom.api.model.Repository;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by clement on 18/02/2014.
 */
public class EbeanRepository implements Repository<EbeanServer> {

    private final EbeanServer server;
    private ServiceRegistration<? extends Repository> registration;
    private Map<EbeanCrudService<?>, ServiceRegistration<Crud>> cruds = new HashMap<>();

    public EbeanRepository(EbeanServer server, DataSource source) {
        this.server = server;
    }

    public synchronized void register(BundleContext context) {
        if (registration != null) {
            unregister();
        }
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put(REPOSITORY_NAME_PROPERTY, getName());
        properties.put(REPOSITORY_TYPE_PROPERTY, getType());
        registration = context.registerService(Repository.class, this, properties);

        for (EbeanCrudService<?> svc : cruds.keySet()) {
            Dictionary<String, Object> props = new Hashtable<>();
            props.put(Crud.ENTITY_CLASS_PROPERTY, svc.getEntityClass());
            props.put(Crud.ENTITY_CLASSNAME_PROPERTY, svc.getEntityClass().getName());

            ServiceRegistration<Crud> reg = context.registerService(Crud.class, svc, props);
            cruds.put(svc, reg);
        }
    }

    public synchronized void unregister() {
        for (Map.Entry<EbeanCrudService<?>, ServiceRegistration<Crud>> entry : cruds.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().unregister();
            }
        }

        cruds.clear();

        if (registration != null) {
            registration.unregister();
        }
    }

    /**
     * Gets all Crud Service managed by the current repository. This allow retrieving the set of entity class managed
     * by the current repository.
     *
     * @return the set of Curd service, empty if none.
     */
    @Override
    public Collection<Crud<?, ?>> getCrudServices() {
        return null;
    }

    /**
     * The name of the repository.
     *
     * @return the current repository name
     */
    @Override
    public String getName() {
        return server.getName();
    }

    /**
     * The type of repository, generally the technology name.
     *
     * @return the type of repository
     */
    @Override
    public String getType() {
        return "ebean";
    }

    /**
     * The class of the technical object represented by this repository. For instance, in the Ebean case,
     * it would be 'com.avaje.ebean.EbeanServer', while for MongoJack it would be 'org.mongojack.JacksonDBCollection'
     *
     * @return the class of the repository
     */
    @Override
    public Class<EbeanServer> getRepositoryClass() {
        return EbeanServer.class;
    }

    /**
     * The technical object represented by this repository
     *
     * @return the current repository
     */
    @Override
    public EbeanServer get() {
        return server;
    }


    public void addCrud(EbeanCrudService<?> svc) {
        // Crud services need to be known before the registration, it's a limitation from Ebean.
        cruds.put(svc, null);
    }
}

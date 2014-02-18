package org.wisdom.api.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * Service representing a repository, i.e.  a persistent technology. For instance, each Ebean server is a repository
 * of type "ebean", while MongoJack is another repository.
 *
 * Providers must register the 'repository.name' and 'repository.type' properties with their service registration.
 *
 * @param <T> the type of repository
 */
public interface Repository<T> {

    /**
     * The service property that <strong>must</strong> be published to indicate the repository name.
     */
    public static final String REPOSITORY_NAME_PROPERTY = "repository.name";

    /**
     * The service property that <strong>must</strong> be published to indicate the repository type.
     */
    public static final String REPOSITORY_TYPE_PROPERTY = "repository.type";

    /**
     * Gets all Crud Service managed by the current repository. This allow retrieving the set of entity class managed
     * by the current repository.
     * @return the set of Curd service, empty if none.
     */
    Collection<Crud<?, ?>> getCrudServices();

    /**
     * The name of the repository.
     * @return the current repository name
     */
    String getName();

    /**
     * The type of repository, generally the technology name.
     * @return the type of repository
     */
    String getType();

    /**
     * The class of the technical object represented by this repository. For instance, in the Ebean case,
     * it would be 'com.avaje.ebean.EbeanServer', while for MongoJack it would be 'org.mongojack.JacksonDBCollection'
     * @return the class of the repository
     */
    Class<T> getRepositoryClass();

    /**
     * The technical object represented by this repository
     * @return the current repository
     */
    T get();
}

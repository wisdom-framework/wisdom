package org.wisdom.api.model.evolution;

import org.wisdom.api.model.Repository;

import java.util.concurrent.Callable;

/**
 * Represent an evolution, i.e. a set of action to migrate from a data (i.e. schema) version to another version.
 */
public interface Evolution {

    /**
     * Gets the source version on which the evolution should be applied.
     * @return the source version, {@link Version#emptyVersion} to ignore sources.
     */
    public Version source();

    /**
     * Gets the target version that should be obtained if this evolution is applied correctly.
     * @return the target version.
     */
    public Version target();

    /**
     * Gets the name of the data persistence layer, i.e. repository, on which the evolution should be applied. It is
     * generally the data source name.
     * @return the name of the repository.
     */
    public String repository();

    /**
     * Apply the evolution.
     * @throws Exception
     */
    public void apply(Repository repository) throws Exception;

}

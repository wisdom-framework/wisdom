package org.wisdom.api.model.evolution;

import org.wisdom.api.model.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Evolution engine is responsible to apply migration on the persistence layer.
 * Such evolution are not pro-active and must be called explicitly.
 *
 * Implementation can rely on {@link org.wisdom.api.model.evolution.EvolutionResolver} services to locate and
 * retrieve evolutions.
 */
public interface EvolutionEngine {


    java.util.Collection<Evolution> getAllEvolutions(Repository repository);

    Collection<Evolution> getApplicableEvolutions(Repository repository);

    boolean update(Repository repository);

    boolean isUpToDate(Repository repository);


}

package org.wisdom.evolution.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.model.Repository;
import org.wisdom.api.model.evolution.Evolution;
import org.wisdom.api.model.evolution.EvolutionEngine;
import org.wisdom.api.model.evolution.EvolutionResolver;
import org.wisdom.api.model.evolution.Version;

import java.util.*;

/**
 *
 */
@Component
@Provides
@Instantiate
public class EvolutionEngineImpl implements EvolutionEngine {

    @Requires(specification = EvolutionResolver.class)
    List<EvolutionResolver> resolvers;

    @Override
    public Collection<Evolution> getAllEvolutions(Repository repository) {
        Set<Evolution> list = createEvolutionChain();
        for (EvolutionResolver resolver : resolvers) {
            for (Evolution evolution : resolver.getEvolutions(repository.getName())) {
                list.add(evolution);
            }
        }
        return list;
    }

    @Override
    public Collection<Evolution> getApplicableEvolutions(Repository repository) {
        Set<Evolution> list = createEvolutionChain();

        for (EvolutionResolver resolver : resolvers) {
            for (Evolution evolution : resolver.getEvolutions(repository.getName())) {
                if (evolution.target().compareTo(repository.getCurrentDataVersion()) > 0) {
                    list.add(evolution);
                }
            }
        }
        return list;
    }

    private TreeSet<Evolution> createEvolutionChain() {
        return new TreeSet<>(new Comparator<Evolution>() {
                @Override
                public int compare(Evolution o1, Evolution o2) {
                    return o1.source().compareTo(o2.source());
                }
            });
    }

    /**
     * Computes the chain of evolution to go from the current repository version to the given target version.
     * @param repository the repository
     * @return the chain of evolution, {@literal empty} if nothing has to be executed,
     * {@literal null} if no chain can be computed.
     */
    public Collection<Evolution> getEvolutionChain(Repository repository, Version target) {
        Collection<Evolution> evolutions = getApplicableEvolutions(repository);
        Set<Evolution> chain = createEvolutionChain();
        // Check if the chain has holes
        Version current = repository.getCurrentDataVersion();

        // We may be already there or beyond
        if (current.compareTo(target) >= 0  && ! target.equals(Version.emptyVersion)) {
            return chain;
        }

        for (Evolution evolution : evolutions) {
            if (! evolution.source().equals(current)  && ! evolution.source().equals(Version.emptyVersion)) {
                // Hole detected.
                return null;
            } else {
                current = evolution.target();
                chain.add(evolution);
                if (current.equals(target)) {
                    // Destination reached.
                    return chain;
                }

            }
        }

        // We have added all the available evolution without reaching the target.
        // Check that the target can is not 0.0.0 (meaning max)
        if (current.equals(target) ||target.equals(Version.emptyVersion)) {
            return chain;
        }
        // Can't reach destination
        return null;
    }


    @Override
    public boolean update(Repository repository) {
        //TODO
        return false;
    }

    @Override
    public boolean isUpToDate(Repository repository) {
        Version version = repository.getCurrentDataVersion();
        for (Evolution evolution : getAllEvolutions(repository)) {
            if (evolution.target().compareTo(version) > 0) {
                return false;
            }
        }
        return true;
    }
}

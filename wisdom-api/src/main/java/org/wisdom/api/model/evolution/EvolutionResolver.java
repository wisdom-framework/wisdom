package org.wisdom.api.model.evolution;

import java.util.Collection;

/**
 * Created by clement on 24/03/2014.
 */
public interface EvolutionResolver {


    public Collection<Evolution> getEvolutions();

    public Collection<Evolution> getEvolutions(String name);

}

package org.wisdom.evolution.impl;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.wisdom.api.model.evolution.Evolution;
import org.wisdom.api.model.evolution.EvolutionResolver;
import org.wisdom.api.model.evolution.Version;

import javax.naming.spi.Resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by clement on 24/03/2014.
 */
public class EvolutionEngineImplTest {


    @Test
    public void test() {
        EvolutionEngineImpl engine = new EvolutionEngineImpl();
        FakeRepository repository = new FakeRepository("repo", null);
        EvolutionResolver resolver = mock(EvolutionResolver.class);
        engine.resolvers = ImmutableList.of(resolver);

        Evolution evolution = new FakeEvolution("1.0.0", "1.0.1", "repo");
        Evolution evolution2 = new FakeEvolution("1.0.1", "1.0.2", "repo2");
        Evolution evolution3 = new FakeEvolution("1.0.1", "1.0.2", "repo");
        Evolution evolution4 = new FakeEvolution("1.0.2", "1.0.3", "repo");
        Evolution evolution5 = new FakeEvolution("1.0.3", "1.1.0", "repo");

        when(resolver.getEvolutions()).thenReturn(ImmutableList.of(evolution, evolution2));
        when(resolver.getEvolutions("repo")).thenReturn(ImmutableList.of(evolution, evolution3, evolution4, evolution5));

        assertThat(engine.getAllEvolutions(repository)).hasSize(4);
        assertThat(engine.getApplicableEvolutions(repository)).hasSize(4);
        repository.setVersion(new Version("1.0.2"));
        assertThat(engine.getApplicableEvolutions(repository)).hasSize(2);
        System.out.println(engine.getApplicableEvolutions(repository));

        assertThat(engine.getEvolutionChain(repository, new Version("1.1.0"))).hasSize(2);
        assertThat(engine.getEvolutionChain(repository, new Version("0.0.0"))).hasSize(2);
        assertThat(engine.getEvolutionChain(repository, new Version("1.0.2"))).hasSize(0);
        assertThat(engine.getEvolutionChain(repository, new Version("1.0.0"))).hasSize(0);
        assertThat(engine.getEvolutionChain(repository, new Version("1.0.4"))).isNull();
        assertThat(engine.getEvolutionChain(repository, new Version("1.0.0"))).isEmpty();


    }
}

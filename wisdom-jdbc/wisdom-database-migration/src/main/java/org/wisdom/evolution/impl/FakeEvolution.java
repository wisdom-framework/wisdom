package org.wisdom.evolution.impl;

import com.google.common.base.Objects;
import org.wisdom.api.model.Repository;
import org.wisdom.api.model.evolution.Evolution;
import org.wisdom.api.model.evolution.Version;

/**
 * Created by clement on 24/03/2014.
 */
public class FakeEvolution implements Evolution {
    private final Version source;
    private final Version target;
    private final String repository;

    public FakeEvolution(String source, String target, String repo) {
        this.source = new Version(source);
        this.target = new Version(target);
        this.repository = repo;
    }

    /**
     * Gets the source version on which the evolution should be applied.
     *
     * @return the source version, {@link org.wisdom.api.model.evolution.Version#emptyVersion} to ignore sources.
     */
    @Override
    public Version source() {
        return source;
    }

    /**
     * Gets the target version that should be obtained if this evolution is applied correctly.
     *
     * @return the target version.
     */
    @Override
    public Version target() {
        return target;
    }

    /**
     * Gets the name of the data persistence layer, i.e. repository, on which the evolution should be applied. It is
     * generally the data source name.
     *
     * @return the name of the repository.
     */
    @Override
    public String repository() {
        return repository;
    }

    /**
     * Apply the evolution.
     *
     * @param repository
     * @throws Exception
     */
    @Override
    public void apply(Repository repository) throws Exception {
        if (repository.getCurrentDataVersion().equals(source)  || source.equals(Version.emptyVersion)) {
            System.out.println("Migrating " + repository + " to version " + target);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("repository", repository)
                .add("source", source)
                .add("target", target)
                .toString();
    }
}

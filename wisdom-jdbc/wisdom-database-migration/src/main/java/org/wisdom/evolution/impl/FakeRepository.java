package org.wisdom.evolution.impl;

import org.wisdom.api.model.Crud;
import org.wisdom.api.model.Repository;
import org.wisdom.api.model.evolution.Version;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by clement on 24/03/2014.
 */
public class FakeRepository implements Repository {

    private final String name;
    private Version version;

    public FakeRepository(String name, String init) {
        this.name = name;
        if (init != null) {
            version = new Version(init);
        } else {
            version = Version.emptyVersion;
        }
    }

    @Override
    public Collection<Crud<?, ?>> getCrudServices() {
        return Collections.emptyList();
    }


    @Override
    public String getName() {
        return this.name;
    }

    /**
     * The type of repository, generally the technology name.
     *
     * @return the type of repository
     */
    @Override
    public String getType() {
        return null;
    }

    @Override
    public Class getRepositoryClass() {
        return Repository.class;
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public Version getCurrentDataVersion() {
        return version;
    }

    @Override
    public Version getCurrentClassVersion() {
        return null;
    }

    public void setVersion(Version v) {
        this.version = v;
    }
}

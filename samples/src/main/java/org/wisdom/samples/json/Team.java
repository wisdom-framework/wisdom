package org.wisdom.samples.json;

import java.util.ArrayList;
import java.util.List;

/**
 * A team is a set of contributor
 */
public class Team {

    List<Contributor> contributors = new ArrayList<>();

    public void addContributor(Contributor contributor) {
        contributors.add(contributor);
    }

    public List<Contributor> getContributors() {
        return new ArrayList<>(contributors);
    }

}

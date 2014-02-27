package org.wisdom.samples.json;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

/**
 * A controller returning the team. The team is serialized using {@link org.wisdom.samples.json.TeamSerializer}.
 */
@Controller
public class TeamController extends DefaultController {

    private final Team team;

    public TeamController() {
        team = new Team();
        team.addContributor(new Contributor("clement", "escoffier"));
        team.addContributor(new Contributor("nicolas", "rempulski"));
        team.addContributor(new Contributor("jonathan", "bardin"));
    }

    @Route(method = HttpMethod.GET, uri = "/team")
    public Result get() {
        return  ok(team).json();
    }

}

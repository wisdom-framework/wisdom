package org.wisdom.samples.security;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.security.Authenticator;

@Component
@Provides
@Instantiate
public class MyAuthenticator implements Authenticator {

    @Override
    public String getUserName(Context context) {
        if (context.session().get("username") != null) {
            return context.session().get("username");
        }
        String username = context.parameter("username");
        if (username != null  && username.equals("admin")) {
            context.session().put("username", "admin");
            return "admin";
        } else {
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Context context) {
         return Results.unauthorized("Your are not authenticated !");
    }
}

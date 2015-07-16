package org.heartfulness.upar;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import or.heartfulness.upar.pojo.User;

import org.heartfulness.upar.admin.resources.BroadCastService;
import org.heartfulness.upar.auth.AdminConstraintSecurityHandler;
import org.heartfulness.upar.auth.OAuthenticator;
import org.heartfulness.upar.health.TemplateHealthCheck;
import org.heartfulness.upar.resources.UparService;

public class App extends Application<UparConfiguration> {

    public static void main( String[] args ) throws Exception {
        new App().run(args);
    }

    @Override
    public String getName() {
        return "upar-service";
    }

    @Override
    public void initialize(Bootstrap<UparConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(UparConfiguration configuration,
                    Environment environment) {
        
        environment.jersey().register(AuthFactory.binder(new OAuthFactory<User>(new OAuthenticator(),
                "admin",
                User.class)));
        environment.admin().setSecurityHandler(new AdminConstraintSecurityHandler("admin", "admin"));
            final UparService resource = new UparService();

        final TemplateHealthCheck healthCheck =
        new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
        
        final BroadCastService broadcast = new BroadCastService(
                configuration.getDefaultName(),
                configuration.getDefaultTopic()
                );
        environment.jersey().register(broadcast);
    }

}

package org.heartfulness.upar;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.heartfulness.upar.auth.AdminConstraintSecurityHandler;
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
        
//        environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<User>(new SimpleAuthenticator(),
//                "admin",
//                User.class)));
        environment.admin().setSecurityHandler(new AdminConstraintSecurityHandler("admin", "admin"));
            final UparService resource = new UparService(
                configuration.getTemplate(),
                configuration.getDefaultName()
    );

        final TemplateHealthCheck healthCheck =
        new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
    }

}

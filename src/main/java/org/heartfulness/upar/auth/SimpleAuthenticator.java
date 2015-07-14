package org.heartfulness.upar.auth;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import or.heartfulness.upar.pojo.User;

import com.google.common.base.Optional;

public class SimpleAuthenticator implements Authenticator<BasicCredentials, User> {
    
    public Optional<User> authenticate(BasicCredentials credentials) {
        if ("secret".equals(credentials.getPassword())) {
            return Optional.of(new User(credentials.getUsername()));
        }
        return Optional.absent();
    }
}

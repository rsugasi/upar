package org.heartfulness.upar.auth;

import org.heartfulness.upar.pojo.User;

import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

public class OAuthenticator implements Authenticator<String, User>  {

    public Optional<User> authenticate(String credentials)
            throws AuthenticationException {
        // TODO Auto-generated method stub
        return null;
    }

}

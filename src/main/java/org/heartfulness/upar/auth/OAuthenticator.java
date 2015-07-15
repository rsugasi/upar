package org.heartfulness.upar.auth;

import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import or.heartfulness.upar.pojo.User;

public class OAuthenticator implements Authenticator<String, User>  {

    public Optional<User> authenticate(String credentials)
            throws AuthenticationException {
        // TODO Auto-generated method stub
        return null;
    }

}

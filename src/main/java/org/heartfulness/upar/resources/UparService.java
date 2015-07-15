package org.heartfulness.upar.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.heartfulness.upar.gcm.GcmSender;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class UparService {
    private final String defaultName;
    private final String defaultId;

    public UparService(String defaultName, String defaultId) {
        this.defaultName = defaultName;
        this.defaultId = defaultId;
    }
    
    @Path("/register")
    @GET
    @Timed
    public void registerToken(@QueryParam("regId") Optional<String> regId) {
        // retrieve the abhyasi id for this reg Id, 
        // if none exist, add a new row in the persistence storage
        // if one exists, delete and add this reg Id to it 
        final String value = String.format("%s", regId.or(defaultName));
        int length = value.length();
        System.out.println("Registration ID received " + ((length>32)?value.substring(0, 32):value));
    }
    
    @Path("/send")
    @GET
    @Timed
    public void sendMessage(@QueryParam("regId") Optional<String> regId, 
                              @QueryParam("msg") Optional<String> msg) {
        final String value = String.format("%s", msg.or(defaultName));
        final String registrationId = String.format("%s", regId.or(defaultId));
        GcmSender gcmSender = new GcmSender();
        gcmSender.sendMessage(registrationId, value);
    }
}

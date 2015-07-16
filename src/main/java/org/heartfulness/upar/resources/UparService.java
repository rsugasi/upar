package org.heartfulness.upar.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import or.heartfulness.upar.pojo.RegistrationResponse;

import org.heartfulness.upar.gcm.GcmSender;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class UparService {
    private final String defaultName;
    private final String defaultId;
    private static final String defaultType = "ABHYASI";

    public UparService(String defaultName, String defaultId) {
        this.defaultName = defaultName;
        this.defaultId = defaultId;
    }
    
    @Path("/register")
    @GET
    @Timed
    public RegistrationResponse registerToken(@QueryParam("regId") Optional<String> regId, 
            @QueryParam("name") Optional<String> name, 
            @QueryParam("id") Optional<String> abhyasiId,
            @QueryParam("type") Optional<String> type) {
        // retrieve the abhyasi id for this reg Id, 
        // if none exist, add a new row in the persistence storage
        // if one exists, delete and add this reg Id to it 
        final String value = String.format("%s", regId.or(defaultName)).replaceAll("[^a-zA-Z0-9]","");
        int length = value.length();
        
        String token = ((length>32)?value.substring(0, 32):value);
        System.out.println("Registration ID received " + token);
        
        String typeOfMember = String.format("%s", regId.or(defaultType));
        // TODO Get information about the User
        boolean isPrefect = false;
        if(typeOfMember.equalsIgnoreCase("PREFECT")) {
            isPrefect = true;
        }
        RegistrationResponse response = new RegistrationResponse();
        response.setAuthToken(token);
        
        String[] topics = null;

        if(isPrefect) {
            typeOfMember = "PREFECT";
            topics = new String[] {"global", "prefect", token};
        } else {
            typeOfMember = "ABHYASI";
            topics = new String[] {"global", "abhyasi", token};
        }
        response.setType(typeOfMember);
        response.setTopics(topics);
        response.setName("Foo Bar");
        
        return response;
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

package org.heartfulness.upar.admin.resources;

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
public class BroadCastService {

    private final String defaultName;
    private final String defaultId;
    
    public BroadCastService(String defaultName, String defaultId) {
        this.defaultName = defaultName;
        this.defaultId = defaultId;
    }
    
    @GET
    @Timed
    public void sendMessageToGlobal(@QueryParam("msg") Optional<String> msg) {
        final String value = String.format("%s", msg.or(defaultName));
        GcmSender gcmSender = new GcmSender();
        gcmSender.sendMessage(defaultId, value);
    }

}

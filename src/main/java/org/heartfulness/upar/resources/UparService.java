package org.heartfulness.upar.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import or.heartfulness.upar.pojo.RegistrationResponse;

import org.eclipse.jetty.server.UserIdentity;
import org.heartfulness.upar.gcm.GcmSender;
import org.heartfulness.upar.input.UparInput;
import org.heartfulness.upar.input.UparInput.GenericMessageType;
import org.heartfulness.upar.input.UparInput.SubmitType;
import org.heartfulness.upar.queue.AbhyasiQueueManager;
//import org.heartfulness.upar.queue.AbhyasiQueue;
import org.heartfulness.upar.queue.Pair;
import org.heartfulness.upar.queue.PairingManager;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class UparService {
    private final String defaultName = "Random ID";
    private static final String defaultType = "PREFECT";
    private static final String PREFECT_TOPIC = "prefect";
    
    public UparService() {    }
    
    @Path("/registerUser")
    @GET
    @Timed
    public RegistrationResponse registerUser(@QueryParam("regId") Optional<String> regId, 
            @QueryParam("fullname") Optional<String> name, 
            @QueryParam("abhyasiid") Optional<String> abhyasiId,
            @QueryParam("type") Optional<String> type) {
        // retrieve the abhyasi id for this reg Id, 
        // if none exist, add a new row in the persistence storage
        // if one exists, delete and add this reg Id to it 
        final String value = String.format("%s", regId.or(defaultName)).replaceAll("[^a-zA-Z0-9]","");
        int length = value.length();
        
        String token = ((length>32)?value.substring(0, 32):value);
        System.out.println("Registration ID received " + token);
        
        String typeOfMember = String.format("%s", type.or(defaultType));
        // TODO Get information about the User
        boolean isPrefect = false;
        boolean isAbhyasi = false;
        if(typeOfMember.equalsIgnoreCase("PREFECT")) {
            isPrefect = true;
        } else if(typeOfMember.equalsIgnoreCase("ABHYASI")) {
            isAbhyasi = true;
        }
        RegistrationResponse response = new RegistrationResponse();
        response.setAuthToken(token);
        
        String[] topics = null;
        String notification = "";
        if(isPrefect) {
            typeOfMember = "PREFECT";
            topics = new String[] {"global", "prefect", token};
            notification = "yes";
        } else if(isAbhyasi){
            typeOfMember = "ABHYASI";
            topics = new String[] {"global", "abhyasi", token};
        } else {
            typeOfMember = "";
            topics = new String[] {"global"};
        }
        response.setType(typeOfMember);
        response.setTopics(topics);
        response.setName("Foo Bar");
        response.setNotification(notification);
        
        return response;
    }
    
    @Path("/registerDevice")
    @GET
    @Timed
    public RegistrationResponse registerDevice(@QueryParam("registrationId") Optional<String> regId) {
        // retrieve the abhyasi id for this reg Id, 
        // if none exist, add a new row in the persistence storage
        // if one exists, delete and add this reg Id to it 
        final String value = String.format("%s", regId.or(defaultName)).replaceAll("[^a-zA-Z0-9]","");
        int length = value.length();
        
        String token = ((length>32)?value.substring(0, 32):value);
        System.out.println("Registration ID received " + token);
        
        
        RegistrationResponse response = new RegistrationResponse();
        response.setAuthToken(token);
        // no other data will be sent
        return response;
    }
    
    @Path("/send")
    @GET
    @Timed
    public void sendMessage(@QueryParam("regId") String regId,
    						@QueryParam("pairId") String pairId,
                              @QueryParam("msg") String msg) {

    	Pair pair = PairingManager.getInstance().getPair(pairId);
    	if(pair == null) {
    	    return;
    	}
    	String targetRegId = pair.getAbhyasiRegID();
    	if(targetRegId.equals(regId)){
    		targetRegId = pair.getPrefectRegID();
    	}
    	UparInput input = new UparInput();
    	input.setSubmit(SubmitType.chat);
		input.setMessage(msg);
		sendMessage(targetRegId, input);
    }
    
    @Path("/getSitting")
    @GET
    @Timed
    public UparInput getSitting(@QueryParam("regId") String regId, @QueryParam("pairId") Optional<String> pairId){
    	if(checkOngoingSitting(pairId)){
    		return getOngoingSittingError();
    	}
		UparInput input = new UparInput();
    	if(AbhyasiQueueManager.getInstance().add(regId)){
    		input.setSubmit(SubmitType.success);
    		input.setMessage("Added to queue with " + (AbhyasiQueueManager.getInstance().getAbhyasiCount() - 1) + " abhyasi in front of you");
    		broadcastBadgeToPrefects(AbhyasiQueueManager.getInstance().getAbhyasiCount(), true);
    	}
    	else{
    		input.setSubmit(SubmitType.error);    
    		input.setMessage(GenericMessageType.alreadyRequestedASitting);
    	}
        return input;
    }
    
    private void broadcastBadgeToPrefects(Integer count, boolean added){
    	UparInput input = new UparInput();
    	input.setCount(count);
    	input.setSubmit(SubmitType.badge);
         if(added){
    	   input.setMessage(GenericMessageType.abhyasiJoined);
    	}
    	sendMessage(PREFECT_TOPIC, input);
    }
    
    @Path("/giveSitting")
    @GET
    @Timed
    public UparInput giveSitting(@QueryParam("regId") String regId, @QueryParam("regId") Optional<String> pairId){
    	if(checkOngoingSitting(pairId)){
    		return getOngoingSittingError();
    	}
    	int initiialCout = AbhyasiQueueManager.getInstance().getAbhyasiCount();
    	String pairID = null;
        String abhyasiRegID = AbhyasiQueueManager.getInstance().poll();
        UparInput input = new UparInput();
        if(abhyasiRegID != null){
            pairID = PairingManager.getInstance().pair(regId, abhyasiRegID);
		    input.setSubmit(SubmitType.sharePair);
		    input.setMessage(pairID);
		    sendMessage(abhyasiRegID, input);
		} else {
		    input.setSubmit(SubmitType.error);
		    input.setMessage(GenericMessageType.noAbhyasiAvailable);
		}
        if(initiialCout != AbhyasiQueueManager.getInstance().getAbhyasiCount()){
            broadcastBadgeToPrefects(AbhyasiQueueManager.getInstance().getAbhyasiCount(), false);
        }
        return input;
    }
    
    private boolean checkOngoingSitting(Optional<String> pairID){
    	return (pairID != null) && (PairingManager.getInstance().isPairCached(pairID.toString()));
    }
    
    private UparInput getOngoingSittingError(){
    	UparInput input = new UparInput();
    	input.setSubmit(SubmitType.error);
    	input.setMessage(GenericMessageType.alreadyInASitting);
    	return input;
    }
    
    @Path("/cancelSitting")
    @GET
    @Timed
    public void cancelSitting(@QueryParam("regId") String regId){
    	AbhyasiQueueManager.getInstance().remove(regId.toString());
    	broadcastBadgeToPrefects(AbhyasiQueueManager.getInstance().getAbhyasiCount(), false);
    }
    
    private UparInput sendJSONMessage(String regId, UparInput input) {
        return input;
    }

    private UparInput alreadyInSitting(String pairId, String regId) {
        Pair pair = PairingManager.getInstance().getPair(pairId);
        UparInput input = null;
        if(pair != null) {
            input = new UparInput();
            input.setSubmit(SubmitType.error);
            input.setMessage(GenericMessageType.alreadyInASitting);
            //topic = regId;
        }
        return input;
    }
    
    @Path("/begin")
    @GET
    @Timed
    public void beginSitting(@QueryParam("pairId") String pairId){
    	Pair pair = PairingManager.getInstance().getPair(pairId);
    	if(pair != null) {
    	    UparInput input = new UparInput();
    	    input.setSubmit(SubmitType.start);
    	    sendMessage(pair.getAbhyasiRegID(), input);
    	} else {
    	    // TODO send error
    	}
    }
    
    @Path("/end")
    @GET
    @Timed
    public void endSitting(@QueryParam("pairId") String pairId){
    	Pair pair = PairingManager.getInstance().getPair(pairId);
    	if(pair != null) {
    	    UparInput input = new UparInput();
    	    input.setSubmit(SubmitType.end);
    	    sendMessage(pair.getAbhyasiRegID(), input);
    	} else {
    	    // TODO send error
    	}
    }

    @Path("/closeSession")
    @GET
    @Timed
    public UparInput closeSession(@QueryParam("regId") String regId,
    		@QueryParam("pairId") String pairId){
    	
    	Pair pair = PairingManager.getInstance().getPair(pairId);
    	UparInput input = new UparInput();
        input.setSubmit(SubmitType.close);
        input.setMessage(GenericMessageType.sessionClose);
    	if(PairingManager.getInstance().closePair(pairId)){
	    	String targetRegId = pair.getAbhyasiRegID();
	    	if(targetRegId.equals(regId)){
	    		targetRegId = pair.getPrefectRegID();
	    	}
			sendMessage(targetRegId, input);
    	}
    	else{
    	    input.setMessage(GenericMessageType.invalidSittingSession);
    	}
    	return input;
    }
    
    public void sendMessage(String regId, UparInput input) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mapper.writeValue(bos, input);
			GcmSender gcmSender = new GcmSender();
			gcmSender.sendMessage(regId, bos.toString());
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
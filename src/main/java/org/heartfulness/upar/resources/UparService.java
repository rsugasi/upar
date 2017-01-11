package org.heartfulness.upar.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.heartfulness.upar.exception.UparException;
import org.heartfulness.upar.exception.UparExceptionType;
import org.heartfulness.upar.gcm.GcmSender;
import org.heartfulness.upar.input.UparInput;
import org.heartfulness.upar.input.UparInput.GenericMessageType;
import org.heartfulness.upar.input.UparInput.SubmitType;
import org.heartfulness.upar.pojo.Abhyasi;
import org.heartfulness.upar.pojo.DeviceType;
import org.heartfulness.upar.pojo.RegistrationResponse;
import org.heartfulness.upar.pojo.UserType;
import org.heartfulness.upar.queue.AbhyasiQueueManager;
import org.heartfulness.upar.queue.Pair;
import org.heartfulness.upar.queue.PairingManager;
import org.heartfulness.upar.queue.RegistrationQueueManager;
import org.heartfulness.upar.util.ValidationUtil;
import org.srcm.openerp.dto.AbhyasiDetailsDTO;
import org.srcm.ssi.openerp.abhyasi.AbhyasiClient;
import org.srcm.ssi.openerp.abhyasi.PrefectClient;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class UparService {
    private static final String defaultType = "PREFECT";
    private static final String PREFECT_TOPIC = "prefect";
    private final boolean isAIMSEnabled;
    public UparService(boolean isAIMSEnabled) { this.isAIMSEnabled = isAIMSEnabled;   }
    
    @Path("/registerUser")
    @GET
    @Timed
    public Abhyasi registerUser(@QueryParam("regId") String regId, 
            @QueryParam("name") String name, 
            @QueryParam("abhyasiId") Optional<String> abhyasiIdArg,
            @DefaultValue("ABHYASI")@QueryParam("userType") String userTypeArg,
            @DefaultValue("android")@QueryParam("deviceType") String deviceTypeArg) throws UparException{
        // retrieve the abhyasi id for this reg Id, 
        // if none exist, add a new row in the persistence storage
    	System.out.println("regId : " + regId + " size : " + regId.length());
        try{
        	boolean isRegistered = RegistrationQueueManager.getInstance().registeredToGCM(regId);
	    	if(!isRegistered){
	    		throw UparExceptionType.Not_Registered_With_GCM.getException();
	    	}
	        AbhyasiDetailsDTO abhyasiDTO;
	        UserType userType = UserType.valueOf(userTypeArg);
	        DeviceType deviceType = DeviceType.valueOf(deviceTypeArg);
	        String abhyasiId = null;
	        if(abhyasiIdArg != null){
	        	abhyasiId = abhyasiIdArg.toString();
	        	if(isAIMSEnabled) {
	        		AbhyasiClient client = new AbhyasiClient();
	        		abhyasiDTO = client.searchAbhyasiByNameAndPermanentId(name, abhyasiId.toString());
	        		if(abhyasiDTO == null){
		            	throw UparExceptionType.Invalid_Abhyasi_ID.getException();
		            }
	        	} else{
	            	userType = UserType.ABHYASI;
	            	if(isAIMSEnabled && (new PrefectClient().readPrefect(abhyasiId.toString()) != null)) {
	            		userType = UserType.PREFECT;
	            	}
	            }
	        }
	        return RegistrationQueueManager.getInstance().setAbhyasiDetails(regId, name, abhyasiId, userType, deviceType);
        }
        catch(UparException ue){
        	throw ue;
        } catch (Exception e) {
			throw UparExceptionType.AIMS_Exception.getException();
		}
    }

    @Path("/registerDevice")
    @GET
    @Timed 
    public RegistrationResponse registerDevice(@QueryParam("registrationId") String regId, 
            @DefaultValue("android")@QueryParam("deviceType") String deviceType) {
        // retrieve the abhyasi id for this reg Id, 
        // if none exist, add a new row in the persistence storage
        // if one exists, delete and add this reg Id to it 
        
        String token = ((regId.length() > 32) ? regId.substring(0, 32):regId);        
        RegistrationResponse response = new RegistrationResponse();
        response.setAuthToken(token);
        Abhyasi abhyasi = new Abhyasi();
        abhyasi.setRegId(token);
        abhyasi.setDeviceType(DeviceType.valueOf(deviceType));
        RegistrationQueueManager.getInstance().register(token, abhyasi);
        System.out.println("regId : " + token );
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
    	sendBadge(PREFECT_TOPIC, input, count);
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
    
    public void sendBadge(String regId, UparInput input, int count) {
        ValidationUtil vu = new ValidationUtil();
        String msg = vu.validateAndReturnObject(input);
        
        GcmSender gcmSender = new GcmSender();
        gcmSender.sendBadge(regId, msg, count);
    }
    
    public void sendMessage(String regId, UparInput input) {
		ValidationUtil vu = new ValidationUtil();
	    String msg = vu.validateAndReturnObject(input);
		GcmSender gcmSender = new GcmSender();
		gcmSender.sendMessage(regId, msg);
	}
}
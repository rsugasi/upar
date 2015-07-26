package org.heartfulness.upar.queue;

import java.util.Arrays;

import org.heartfulness.upar.exception.UparException;
import org.heartfulness.upar.exception.UparExceptionType;
import org.heartfulness.upar.pojo.Abhyasi;
import org.heartfulness.upar.pojo.DeviceType;
import org.heartfulness.upar.pojo.UserType;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class RegistrationQueueManager {

    private CacheManager cm;
    private Cache cache;
    private static RegistrationQueueManager instance = new RegistrationQueueManager();
    
    private RegistrationQueueManager(){
        //1. Create a cache manager
        cm = CacheManager.newInstance(); 

        //2. Get a cache called "cache1", declared in ehcache.xml
        cache = cm.getCache("registrationQueue");
    }
    

    public static RegistrationQueueManager getInstance(){
        return instance;
    }
    
    public void register(String regId, Abhyasi abhyasi) {
        if(!cache.isKeyInCache(regId)){
            cache.put(new Element(regId, abhyasi));
        }
    }
    
    public void unregister(String regId, Abhyasi abhyasi){
        if(cache.isKeyInCache(regId) && cache.get(regId).getObjectValue().equals(abhyasi)){
            cache.remove(regId);
        }
    }
    
    public Abhyasi setAbhyasiDetails(String regId, String name, String abhyasiId, UserType userType, DeviceType deviceType) throws UparException{
        if(!cache.isKeyInCache(regId) && cache.get(regId) != null){
            Abhyasi abhyasi = (Abhyasi)cache.get(regId).getObjectValue();
            abhyasi.setAbhyasiId(abhyasiId);
            abhyasi.setName(name);
        	abhyasi.setUserType(userType);
        	abhyasi.setDeviceType(deviceType);
        	abhyasi.setNotification(userType.getNotification());
        	abhyasi.setTopics(Arrays.asList(userType.getTopics()));
        	abhyasi.getTopics().add(regId);
        	return abhyasi;
        }
        throw UparExceptionType.Abhyasi_Does_Not_Exist.getException();
    }

    public boolean registeredToGCM(String regId){
    	return cache.isKeyInCache(regId) && cache.get(regId) != null;
    }

//    public boolean registeredAbhyasi(String regId){
//    	return registeredToGCM(regId) && ((Abhyasi)cache.get(regId).getObjectValue());
//    }
}

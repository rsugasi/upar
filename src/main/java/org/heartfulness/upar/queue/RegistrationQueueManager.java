package org.heartfulness.upar.queue;

import or.heartfulness.upar.pojo.Abhyasi;
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
    
    public void setAbhyasiDetails(String regId, String name, String abhyasiId, String email, boolean prefect){
        if(!cache.isKeyInCache(regId)){
            Abhyasi abhyasi = (Abhyasi)cache.get(regId).getObjectValue();
            abhyasi.setAbhyasiId(abhyasiId);
            abhyasi.setEmail(email);
            abhyasi.setPrefect(prefect);
            abhyasi.setName(name);
        }
    }
}

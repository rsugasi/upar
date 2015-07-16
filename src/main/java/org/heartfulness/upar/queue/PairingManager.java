package org.heartfulness.upar.queue;

import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class PairingManager {
	private CacheManager cm;
	private Cache cache;
	private static PairingManager instance = new PairingManager();
	
	private PairingManager(){
		//1. Create a cache manager
		cm = CacheManager.newInstance(); 

		//2. Get a cache called "cache1", declared in ehcache.xml
		cache = cm.getCache("pairing");
	}
	
	public static PairingManager getInstance(){
		return instance;
	}
	
	public String pair(String prefectRegID, String abhyasiRegID){
		Pair pair = new Pair();
		pair.setAbhyasiRegID(abhyasiRegID);
		pair.setPrefectRegID(prefectRegID);
		String pairID = UUID.randomUUID().toString();
		cache.put(new Element(pairID, pair));
		return pairID;
	}
	
	public Pair getPair(String pairID){
		if(cache.isKeyInCache(pairID)){
			return (Pair) cache.get(pairID).getObjectValue();
		}
		return null;
	}
	
	public boolean closePair(String pairID){
		return cache.remove(pairID);
	}
}

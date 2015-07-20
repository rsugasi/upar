package org.heartfulness.upar.queue;

import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class AbhyasiQueueManager {
	private CacheManager cm;
	private ConcurrentLinkedQueue<String> queue;
	private Cache cache;
	private static AbhyasiQueueManager instance = new AbhyasiQueueManager();
	
	private AbhyasiQueueManager(){
		//1. Create a cache manager
		cm = CacheManager.newInstance(); 

		//2. Get a cache called "cache1", declared in ehcache.xml
		cache = cm.getCache("abhyasiQueue");
		
		queue = new ConcurrentLinkedQueue<String>();
	}
	
	public static AbhyasiQueueManager getInstance(){
		return instance;
	}

	public boolean add(String regId) {
		if(!cache.isKeyInCache(regId)){
			removeFromQueue(regId);
			queue.add(regId);
			cache.put(new Element(regId, regId));
			return true;
		}
		return false;
	}

	public synchronized String poll() {
		String regId = null;
		while(queue.peek() != null){
			regId = queue.poll();
			if(cache.isKeyInCache(regId)){
				cache.remove(regId);
				return regId;
			}
		}
		return null;
	}
	
	public void remove(String regId){
		cache.remove(regId);
		removeFromQueue(regId);
	}
	
	private void removeFromQueue(String regId){
		while(queue.remove(regId));
	}
	
	public Integer getAbhyasiCount(){
		return cache.getSize();
	}
}

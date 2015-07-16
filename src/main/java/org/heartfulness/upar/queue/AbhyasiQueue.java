package org.heartfulness.upar.queue;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AbhyasiQueue extends ConcurrentLinkedQueue<String>{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 4262151454379612928L;
	
	private static AbhyasiQueue instance = new AbhyasiQueue();
	
	private AbhyasiQueue(){
		
	}
	
	public static AbhyasiQueue getInstance(){
		return instance;
	}

}

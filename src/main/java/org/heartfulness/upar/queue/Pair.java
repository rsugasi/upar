package org.heartfulness.upar.queue;

import java.io.Serializable;

public class Pair implements Serializable{

	private String abhyasiRegID;
	private String prefectRegID;
	public String getAbhyasiRegID() {
		return abhyasiRegID;
	}
	public void setAbhyasiRegID(String abhyasiRegID) {
		this.abhyasiRegID = abhyasiRegID;
	}
	public String getPrefectRegID() {
		return prefectRegID;
	}
	public void setPrefectRegID(String prefectRegID) {
		this.prefectRegID = prefectRegID;
	}
	
	
}

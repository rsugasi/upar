package org.heartfulness.upar.pojo;

public enum UserType {

	ABHYASI("", new String[] {"global", "abhyasi"}), 
	SEEKER("", new String[] {"global"}), 
	PREFECT("yes", new String[] {"global", "prefect"});

	private String notification;
	private String[] topics;
	public String getNotification() {
		return notification;
	}
	public void setNotification(String notification) {
		this.notification = notification;
	}
	public String[] getTopics() {
		return topics;
	}
	public void setTopics(String[] topics) {
		this.topics = topics;
	}
	private UserType(String notification, String[] topics) {
		this.notification = notification;
		this.topics = topics;
	}
	
	
}

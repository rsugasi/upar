package or.heartfulness.upar.pojo;

public class RegistrationResponse {    

    private String authToken;
    private String name;
    private String type;
    private String notification;
    private String[] topics;
    
    public String getAuthToken() {
        return authToken;
    }
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String[] getTopics() {
        return topics;
    }
    public void setTopics(String[] topics) {
        this.topics = topics;
    }
    public String getNotification() {
        return notification;
    }
    public void setNotification(String notification) {
        this.notification = notification;
    }

}

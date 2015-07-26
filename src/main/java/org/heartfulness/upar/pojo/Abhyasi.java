package org.heartfulness.upar.pojo;

import java.io.Serializable;
import java.util.List;

public class Abhyasi implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String abhyasiId;
    private String regId;
    private String userType;
    private String deviceType;
    private String notification;
    private List<String> topics;
    
    public Abhyasi(){
    	
    }
    
    
    public String getName() {
        return name;
    }



    public void setName(String name) {
        this.name = name;
    }



    public String getAbhyasiId() {
        return abhyasiId;
    }



    public void setAbhyasiId(String abhyasiId) {
        this.abhyasiId = abhyasiId;
    }



    public String getRegId() {
        return regId;
    }



    public void setRegId(String regId) {
        this.regId = regId;
    }


    public boolean isPrefect() {
        return userType != null && UserType.PREFECT.equals(UserType.valueOf(userType));
    }

    
    public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType.name();
	}


	public String getDeviceType() {
        return deviceType;
    }

	public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
    	if(deviceType != null){
    		this.deviceType = deviceType.name();
    	}
    }

    public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}


	public List<String> getTopics() {
		return topics;
	}


	public void setTopics(List<String> topics) {
		this.topics = topics;
	}


	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((abhyasiId == null) ? 0 : abhyasiId.hashCode());
        result = prime * result
                + ((deviceType == null) ? 0 : deviceType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((regId == null) ? 0 : regId.hashCode());
        return result;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Abhyasi other = (Abhyasi) obj;
        if (abhyasiId == null) {
            if (other.abhyasiId != null)
                return false;
        } else if (!abhyasiId.equals(other.abhyasiId))
            return false;
        if (deviceType != other.deviceType)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (regId == null) {
            if (other.regId != null)
                return false;
        } else if (!regId.equals(other.regId))
            return false;
        return true;
    }

}

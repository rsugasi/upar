package or.heartfulness.upar.pojo;

import java.io.Serializable;

public class Abhyasi implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String abhyasiId;
    private String regId;
    private String email;
    private boolean prefect;
    private DeviceType deviceType;
    
        
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



    public String getEmail() {
        return email;
    }



    public void setEmail(String email) {
        this.email = email;
    }



    public boolean isPrefect() {
        return prefect;
    }



    public void setPrefect(boolean prefect) {
        this.prefect = prefect;
    }



    public DeviceType getDeviceType() {
        return deviceType;
    }



    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((abhyasiId == null) ? 0 : abhyasiId.hashCode());
        result = prime * result
                + ((deviceType == null) ? 0 : deviceType.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (prefect ? 1231 : 1237);
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
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (prefect != other.prefect)
            return false;
        if (regId == null) {
            if (other.regId != null)
                return false;
        } else if (!regId.equals(other.regId))
            return false;
        return true;
    }



    public enum DeviceType {
        android, ios
    }
}

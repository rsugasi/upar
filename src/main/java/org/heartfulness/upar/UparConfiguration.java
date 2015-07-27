package org.heartfulness.upar;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class UparConfiguration extends Configuration {
    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";
    
    @NotEmpty
    private String defaultTopic;
    
    
    private boolean isAIMSEnabled;

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }
    
    @JsonProperty
    public String getDefaultTopic() {
        return defaultTopic;
    }
    
    @JsonProperty
    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }
    
    @JsonProperty
    public boolean getIsAIMSEnabled() {
    	return isAIMSEnabled;
    }
    
    @JsonProperty
    public void setIsAIMSEnabled(boolean isAIMSEnabled) {
    	this.isAIMSEnabled = isAIMSEnabled;
    }
}

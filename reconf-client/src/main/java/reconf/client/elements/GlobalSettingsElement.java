package reconf.client.elements;

import javax.validation.*;
import javax.xml.bind.annotation.*;
import org.hibernate.validator.constraints.*;


public class GlobalSettingsElement {

    private ReloadPolicyElement reloadPolicy;
    private String server;

    @XmlElement(name="reload-policy") @Valid
    public ReloadPolicyElement getReloadPolicy() {
        return reloadPolicy;
    }
    public void setReloadPolicy(ReloadPolicyElement reloadPolicy) {
        this.reloadPolicy = reloadPolicy;
    }

    @XmlAttribute(name="server") @URL
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }
}

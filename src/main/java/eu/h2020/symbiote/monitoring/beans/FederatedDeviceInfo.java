package eu.h2020.symbiote.monitoring.beans;

import eu.h2020.symbiote.cloud.model.internal.ResourceSharingInformation;

public class FederatedDeviceInfo {

    private String symbioteId;
    private ResourceSharingInformation sharingInformation;
    private String type;

    public String getSymbioteId() {
        return symbioteId;
    }

    public void setSymbioteId(String symbioteId) {
        this.symbioteId = symbioteId;
    }

    public ResourceSharingInformation getSharingInformation() {
        return sharingInformation;
    }

    public void setSharingInformation(ResourceSharingInformation sharingInformation) {
        this.sharingInformation = sharingInformation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

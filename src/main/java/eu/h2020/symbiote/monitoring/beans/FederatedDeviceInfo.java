package eu.h2020.symbiote.monitoring.beans;

import eu.h2020.symbiote.cloud.model.internal.ResourceSharingInformation;

public class FederatedDeviceInfo {

    private ResourceSharingInformation sharingInformation;
    private String type;

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

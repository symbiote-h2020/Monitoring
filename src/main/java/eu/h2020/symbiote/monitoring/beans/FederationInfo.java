package eu.h2020.symbiote.monitoring.beans;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

public class FederationInfo {
  
  @Id
  private String federationId;
  
  private Map<String, FederatedDeviceInfo> resources = new HashMap<>();
  
  public String getFederationId() {
    return federationId;
  }
  
  public void setFederationId(String federationId) {
    this.federationId = federationId;
  }
  
  public Map<String, FederatedDeviceInfo> getResources() {
    return resources;
  }
  
  public void setResources(Map<String, FederatedDeviceInfo> resources) {
    this.resources = resources;
  }
}

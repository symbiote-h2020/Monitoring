package eu.h2020.symbiote.beans;

import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

public class FederationInfo {
  
  @Id
  private String federationId;
  
  private Map<String, TimedValue> resources = new HashMap<>();
  
  public String getFederationId() {
    return federationId;
  }
  
  public void setFederationId(String federationId) {
    this.federationId = federationId;
  }
  
  public Map<String, TimedValue> getResources() {
    return resources;
  }
  
  public void setResources(Map<String, TimedValue> resources) {
    this.resources = resources;
  }
}

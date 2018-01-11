package eu.h2020.symbiote.beans;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FederationInfo {
  
  @Id
  private String federationId;
  
  private Map<String, Date> resources = new HashMap<>();
  
  public String getFederationId() {
    return federationId;
  }
  
  public void setFederationId(String federationId) {
    this.federationId = federationId;
  }
  
  public Map<String, Date> getResources() {
    return resources;
  }
  
  public void setResources(Map<String, Date> resources) {
    this.resources = resources;
  }
}

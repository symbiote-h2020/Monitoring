package eu.h2020.symbiote.beans;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class FederationInfo {
  
  @Id
  private String federationId;
  private List<String> metrics = new ArrayList<>();
  private List<String> devices = new ArrayList<>();
  
  public String getFederationId() {
    return federationId;
  }
  
  public void setFederationId(String federationId) {
    this.federationId = federationId;
  }
  
  public List<String> getMetrics() {
    return metrics;
  }
  
  public void setMetrics(List<String> metrics) {
    this.metrics = metrics;
  }
  
  public List<String> getDevices() {
    return devices;
  }
  
  public void setDevices(List<String> devices) {
    this.devices = devices;
  }
}

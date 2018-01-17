package eu.h2020.symbiote.monitoring.beans;

import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudMonitoringResource {
  
  @Id
  private String resourceId;
  
  private Map<String, Map<String, List<TimedValue>>> deviceMetrics = new HashMap<>();
  
  public CloudMonitoringResource() {
  }
  
  public String getResource() {
    return resourceId;
  }
  
  public void setResource(String resource) {
    this.resourceId = resource;
  }
  
  public Map<String, Map<String, List<TimedValue>>> getMetrics() {
    return deviceMetrics;
  }
  
  public void setMetrics(Map<String, Map<String, List<TimedValue>>> metrics) {
    this.deviceMetrics = metrics;
  }
}
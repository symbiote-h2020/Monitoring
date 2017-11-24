package eu.h2020.symbiote.beans;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class CloudMonitoringResource {
  
  @Id
  private String id;
  
  private CloudResource resource;
  private List<MonitoringMetric> metrics = new ArrayList<>();
  
  public CloudMonitoringResource() {
  }
  
  public CloudMonitoringResource(CloudResource resource) {
    this.resource = resource;
  }
  
  public CloudResource getResource() {
    return resource;
  }
  
  public void setResource(CloudResource resource) {
    this.resource = resource;
  }
  
  public List<MonitoringMetric> getMetrics() {
    return metrics;
  }
  
  public void setMetrics(List<MonitoringMetric> metrics) {
    this.metrics = metrics;
  }
}

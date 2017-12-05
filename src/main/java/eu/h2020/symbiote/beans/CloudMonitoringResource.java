package eu.h2020.symbiote.beans;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class CloudMonitoringResource {
  
  @Id
  private String resourceId;
  
  private List<DeviceMetricList> deviceMetrics = new ArrayList<>();
  
  public CloudMonitoringResource() {
  }
  
  public String getResource() {
    return resourceId;
  }
  
  public void setResource(String resource) {
    this.resourceId = resource;
  }
  
  public List<DeviceMetricList> getMetrics() {
    return deviceMetrics;
  }
  
  public void setMetrics(List<DeviceMetricList> metrics) {
    this.deviceMetrics = metrics;
  }
}

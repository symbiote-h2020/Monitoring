package eu.h2020.symbiote.beans;

import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;

import org.springframework.data.annotation.Id;

public class MonitoringMetric {
  
  @Id
  private String id;
  
  private DeviceMetric metric;
  
  public MonitoringMetric() {
  }
  
  public MonitoringMetric(DeviceMetric metric) {
    this.metric = metric;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public DeviceMetric getMetric() {
    return metric;
  }
  
  public void setMetric(DeviceMetric metric) {
    this.metric = metric;
  }
}


package eu.h2020.symbiote.beans;

import eu.h2020.symbiote.cloud.monitoring.model.Metric;

public class MonitoringMetric {
  
  private Metric metric;
  private boolean processed = false;
  
  public MonitoringMetric(Metric metric) {
    this.metric = metric;
  }
  
  public MonitoringMetric() {
  }
  
  public Metric getMetric() {
    return metric;
  }
  
  public void setMetric(Metric metric) {
    this.metric = metric;
  }
  
  public boolean isProcessed() {
    return processed;
  }
  
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }
}


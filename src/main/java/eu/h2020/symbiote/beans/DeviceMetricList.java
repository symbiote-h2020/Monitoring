package eu.h2020.symbiote.beans;

import java.util.List;

public class DeviceMetricList {
  
  private String metric;
  
  private List<DayMetricList> metricValues;
  
  public String getMetric() {
    return metric;
  }
  
  public void setMetric(String metric) {
    this.metric = metric;
  }
  
  public List<DayMetricList> getMetricValues() {
    return metricValues;
  }
  
  public void setMetricValues(List<DayMetricList> metricValues) {
    this.metricValues = metricValues;
  }
}

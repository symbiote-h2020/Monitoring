package eu.h2020.symbiote.beans;

import java.util.List;

public class DayMetricList {
  
  private String day;
  
  private List<MetricValue> values;
  
  public String getDay() {
    return day;
  }
  
  public void setDay(String day) {
    this.day = day;
  }
  
  public List<MetricValue> getValues() {
    return values;
  }
  
  public void setValues(List<MetricValue> values) {
    this.values = values;
  }
}

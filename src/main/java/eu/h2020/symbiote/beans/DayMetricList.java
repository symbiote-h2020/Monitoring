package eu.h2020.symbiote.beans;

import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;

import java.util.List;

public class DayMetricList {
  
  private String day;
  
  private List<TimedValue> values;
  
  public String getDay() {
    return day;
  }
  
  public void setDay(String day) {
    this.day = day;
  }
  
  public List<TimedValue> getValues() {
    return values;
  }
  
  public void setValues(List<TimedValue> values) {
    this.values = values;
  }
}

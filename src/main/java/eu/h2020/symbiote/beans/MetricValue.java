package eu.h2020.symbiote.beans;

import java.util.Date;

public class MetricValue {
  
  private Date date;
  private String value;
  
  public Date getDate() {
    return date;
  }
  
  public void setDate(Date date) {
    this.date = date;
  }
  
  public String getValue() {
    return value;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
}

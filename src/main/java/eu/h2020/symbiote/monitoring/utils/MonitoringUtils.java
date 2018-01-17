package eu.h2020.symbiote.monitoring.utils;

import eu.h2020.symbiote.monitoring.beans.CloudMonitoringResource;
import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitoringUtils {
  
  public static String getDateWithoutTime(Date input) {
    return DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC")).format(input.toInstant());
  }
  
  public static Date getDate(String isoDateTime) {
    return Date.from(DateTimeFormatter.ISO_INSTANT.parse(isoDateTime, Instant::from));
  }
  
  public static void addToResource(Map<String, CloudMonitoringResource> resources, DeviceMetric metric) {
    
    CloudMonitoringResource resource = resources.get(metric.getDeviceId());
    if (resource == null) {
      resource = new CloudMonitoringResource();
      resource.setResource(metric.getDeviceId());
      resources.put(metric.getDeviceId(), resource);
    }
    
    Map<String, List<TimedValue>> metricValues = resource.getMetrics().get(metric.getTag());
    if (metricValues == null) {
      metricValues = new HashMap<>();
      resource.getMetrics().put(metric.getTag(), metricValues);
    }
    
    String day = getDateWithoutTime(metric.getDate());
    List<TimedValue> dayMetricValues = metricValues.get(day);
    if (dayMetricValues == null) {
      dayMetricValues = new ArrayList<>();
      metricValues.put(day, dayMetricValues);
    }
    
    TimedValue value = new TimedValue();
    value.setDate(metric.getDate());
    value.setValue(metric.getValue());
    
    dayMetricValues.add(value);
  }
  
  public static Map<String, CloudMonitoringResource> toResourceMap(List<DeviceMetric> metrics) {
    Map<String, CloudMonitoringResource> result = new HashMap<>();
    metrics.forEach(metric -> addToResource(result, metric));
    return result;
  }
  
}

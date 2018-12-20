/*
 * Copyright 2018 Atos
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.h2020.symbiote.monitoring.utils;

import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;
import eu.h2020.symbiote.monitoring.beans.CloudMonitoringResource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

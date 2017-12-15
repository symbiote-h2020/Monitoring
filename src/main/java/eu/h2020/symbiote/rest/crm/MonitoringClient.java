package eu.h2020.symbiote.rest.crm;

import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.constants.MonitoringConstants;

import feign.Headers;
import feign.QueryMap;
import feign.RequestLine;

import java.util.List;
import java.util.Map;

public interface MonitoringClient {
  
  @RequestLine("POST " + MonitoringConstants.METRICS_DATA)
  @Headers("Content-Type: application/json")
  List<DeviceMetric> postMetrics(List<DeviceMetric> metrics);
  
  @RequestLine("GET " + MonitoringConstants.METRICS_DATA)
  @Headers("Content-Type: application/json")
  List<DeviceMetric> getMetrics(@QueryMap Map<String, String> parameters);
}

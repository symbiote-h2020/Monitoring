package eu.h2020.symbiote.monitoring.crm;

import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.monitoring.constants.MonitoringConstants;

import feign.Headers;
import feign.Param;
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
  
  @RequestLine("GET " + MonitoringConstants.AGGREGATED_DATA)
  @Headers("Content-Type: application/json")
  List<AggregatedMetrics> getAggregatedMetrics(@QueryMap Map<String, String> parameters);
  
  @RequestLine("GET " + MonitoringConstants.SUMMARY_DATA+"?federation={federation}&metric={metric}")
  @Headers("Content-Type: application/json")
  Map<String, Double> getSummaryMetric(@Param("federation") String federationId,
                                       @Param("metric") String inputMetric);
}

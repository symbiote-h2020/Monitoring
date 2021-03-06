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

package eu.h2020.symbiote.monitoring.service;

import com.mongodb.bulk.BulkWriteResult;
import eu.h2020.symbiote.client.ClientConstants;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;
import eu.h2020.symbiote.cloud.monitoring.model.AggregationOperation;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.monitoring.beans.CloudMonitoringResource;
import eu.h2020.symbiote.monitoring.beans.FederationInfo;
import eu.h2020.symbiote.monitoring.constants.MonitoringConstants;
import eu.h2020.symbiote.monitoring.db.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manage the REST operations from Platform using MongoDB
 *
 * @author: Fernando Campos, Jose Antonio Sanchez
 * @version: 19/04/2017
 */
@RestController
@RequestMapping("/")
public class PlatformMonitoringRestService {
  
  private static final Log logger = LogFactory.getLog(PlatformMonitoringRestService.class);

  @Value("${spring.data.mongodb.host:localhost}")
  private String mongoUri;
  
  @Value("${monitoring.mongo.database:symbiote-cloud-monitoring-database}")
  private String mongoDatabase;
  
  @Autowired
  private ResourceMetricsRepository resourceMetricsRepository;
  
  @Autowired
  private MetricsRepository metricsRepository;
  
  @Autowired
  private CloudResourceRepository coreRepository;
  
  @Autowired
  private FederationInfoRepository federationInfoRepository;
  
  @Autowired
  private MongoTemplate template;
  
  @Autowired
  private MetricsProcessor processor;
  
  private MongoDbMonitoringBackend backend;
  
  @PostConstruct
  public void init() {
    backend = new MongoDbMonitoringBackend(mongoUri, mongoDatabase,
        template.getCollectionName(CloudMonitoringResource.class));
  }
  
  /**
   * Listen from Platform Host. Received device monitoring data
   */
  @RequestMapping(method = RequestMethod.POST, path = ClientConstants.METRICS_DATA, produces = "application/json", consumes = "application/json")
  public @ResponseBody
  List<DeviceMetric> saveMetrics(@RequestBody List<DeviceMetric> metrics) throws Throwable {
    
    
    List<DeviceMetric> coreMetrics = new ArrayList<>();
    Map<String, CloudResource> resourceCache = new HashMap<>();
    
    metrics.forEach(metric -> {
      String deviceId = metric.getDeviceId();
      CloudResource resource = resourceCache.get(deviceId);
      if (resource == null) {
        resource = coreRepository.findOne(deviceId);
        resourceCache.put(deviceId, resource);
      }
      
      if (resource != null && resource.getResource() != null) {
        coreMetrics.add(metric);
      }
      
    });
    
    
    BulkWriteResult insertResult;
    
    if (!coreMetrics.isEmpty()) {
      template.bulkOps(BulkOperations.BulkMode.UNORDERED, DeviceMetric.class).insert(coreMetrics)
          .execute();
    }
    
    return backend.saveMetrics(metrics);
  }
  
  private String getDateWithoutTime(Date input) {
    return DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC")).format(input.toInstant());
  }
  
  private Date getDate(String isoDateTime) {
    if (isoDateTime != null) {
      return Date.from(DateTimeFormatter.ISO_INSTANT.parse(isoDateTime, Instant::from));
    } else {
      return null;
    }
  }
  
  @RequestMapping(method = RequestMethod.GET, path = ClientConstants.METRICS_DATA, produces = "application/json")
  public @ResponseBody
  List<DeviceMetric> getMetrics(@RequestParam(value = "device", required = false) String device,
                                @RequestParam(value = "metric", required = false) String metric,
      /*
       * Bug https://github.com/JodaOrg/joda-time/issues/443 which I still consider a bug, prevents automatic deserialization.
       * If it's fixed in the future, we might rely on DateTimeFormat annotation.
       */
                                @RequestParam(value = "startDate", required = false) String startDateStr,
                                @RequestParam(value = "endDate", required = false) String endDateStr) throws Throwable {
    
    
    Date startDate = getDate(startDateStr);
    Date endDate = getDate(endDateStr);
    List<String> devices = (device == null)?null:Arrays.asList(device);
    List<String> metrics = (metric == null)?null:Arrays.asList(metric);
    return backend.getMetrics(devices, metrics, startDate, endDate);
  }
  
  @RequestMapping(method = RequestMethod.GET, path = ClientConstants.AGGREGATED_DATA, produces = "application/json")
  public @ResponseBody
  List<AggregatedMetrics> getAggregatedMetrics(@RequestParam(value = "device", required = false)
                                                   List<String> devices,
                                               @RequestParam(value = "metric", required = false)
                                                   List<String> metrics,
      /*
       * Bug https://github.com/JodaOrg/joda-time/issues/443 which I still consider a bug, prevents automatic deserialization.
       * If it's fixed in the future, we might rely on DateTimeFormat annotation.
       */
                                               @RequestParam(value = "startDate", required = false)
                                                   String startDateStr,
                                               @RequestParam(value = "endDate", required = false)
                                                   String endDateStr,
                                               @RequestParam(value = "operation", required = false)
                                                   List<String> operations,
                                               @RequestParam(value = "count", required = false)
                                                   List<String> counts) throws Throwable {
    
    
    Date startDate = getDate(startDateStr);
    Date endDate = getDate(endDateStr);
    List<AggregationOperation> ops = operations.stream()
                                         .map(op -> AggregationOperation.fromValue(op))
                                         .filter(op -> op != null).collect(Collectors.toList());
    
    return backend.getAggregatedMetrics(devices, metrics, startDate, endDate, ops, counts);
  }
  
  @RequestMapping(method = RequestMethod.GET, path = ClientConstants.SUMMARY_DATA, produces = "application/json")
  public ResponseEntity<?> getSummaryMetric(@RequestParam(value = "federation") String federationId,
                                                             @RequestParam(value = "metric") String inputMetric) {
    HttpStatus status = HttpStatus.OK;
    String error = "";
    if (!StringUtils.isBlank(federationId) && !StringUtils.isBlank(inputMetric)) {
      String tag = null;
      String type = null;
      String duration = null;
      
      String[] metricPath = inputMetric.split("\\.");
      int length = metricPath.length;
      
      switch (length) {
        case 1:
          tag = inputMetric;
          duration = MonitoringConstants.ALL_QUALIFIER;
          break;
        case 2:
          tag = metricPath[0];
          duration = metricPath[1];
          break;
        default:
          tag = metricPath[0];
          type = metricPath[1];
          duration = metricPath[2];
          break;
      }
      
      if (!StringUtils.isEmpty(tag)) {
        
        if (MonitoringConstants.ALL_QUALIFIER.equals(duration) || StringUtils.isNumeric(duration)) {
          
          FederationInfo federationInfo = federationInfoRepository.findOne(federationId);
          if (federationInfo != null) {
            return new ResponseEntity<>(
                getAggregatedMetric(inputMetric, federationInfo, tag, duration, type), status);
          } else {
            error = "Can't find information of federation " + federationId;
            status = HttpStatus.NOT_FOUND;
          }
          
        } else {
          error = "Non numeric or all duration " + duration;
          status = HttpStatus.BAD_REQUEST;
        }
        
      } else {
        error = "Received unknown metric " + inputMetric;
        status = HttpStatus.BAD_REQUEST;
      }
      
    } else {
      error = "Received blank metric: " + inputMetric + " or federation id: " + federationId;
      status = HttpStatus.BAD_REQUEST;
    }
    
    logger.error(error);
    
    return new ResponseEntity<>(error, status);
  }
  
  private Map<String, Double> getAggregatedMetric(String inputMetric, FederationInfo federationInfo, String tag, String duration, String type) {
    
    Map<String, Double> result = new HashMap<>();
    
    List<String> devices = null;
    if (type != null) {
      devices = federationInfo.getResources().entrySet().stream()
                    .filter(entry -> type.equals(entry.getValue().getType()))
                    .map(entry -> entry.getKey()).collect(Collectors.toList());
    } else {
      devices = federationInfo.getResources().keySet().stream().collect(Collectors.toList());
    }
    
    Date startDate = new Date();
    Date endDate = new Date();
    
    devices.forEach(device -> {
      
      Instant share = Instant.ofEpochMilli(federationInfo.getResources().get(device).getSharingInformation()
              .getSharingDate().getTime());
      
      if (!MonitoringConstants.ALL_QUALIFIER.equals(duration)) {
        int days = Integer.valueOf(duration);
        
        Instant start = Instant.now().atZone(ZoneId.of("UTC")).minusDays(days).toInstant();
        startDate.setTime((share.isBefore(start)) ? start.toEpochMilli() : share.toEpochMilli());
      } else {
        startDate.setTime(share.toEpochMilli());
      }
      
      List<AggregatedMetrics> metrics = backend.getAggregatedMetrics(
          Arrays.asList(device),
          Arrays.asList(tag), startDate, endDate,
          Arrays.asList(AggregationOperation.AVG), null);
      
      if ((metrics != null) && !metrics.isEmpty()) {
        result.put(device, metrics.get(0).getStatistics().get(AggregationOperation.AVG.toString()));
      }
      
    });
    
    return result;
    
  }
}

package eu.h2020.symbiote;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.beans.FederationInfo;
import eu.h2020.symbiote.beans.MonitoringMetric;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.CloudResourceRepository;
import eu.h2020.symbiote.db.FederationInfoRepository;
import eu.h2020.symbiote.db.ResourceMetricsRepository;
import eu.h2020.symbiote.rest.crm.MonitoringClient;
import eu.h2020.symbiote.service.MetricsProcessor;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {"eureka.client.enabled=false",
    "symbIoTe.aam.integration=false",
    "server.port=18033",
    "monitoring.mongo.database=metrics-test",
    "symbIoTe.coreaam.url=http://localhost:8083",
    "symbIoTe.crm.integration=false",
    "platform.id=TestPlatform",
    "symbiote.crm.url=http://localhost:8083",
    "symbIoTe.aam.integration=false",
    "symbIoTe.coreaam.url=http://localhost:8083"})
public class MetricsProcessorTests {
  
  public static final String SYMBIOTE_PREFIX = "symbiote_";
  public static final String MONITORING_URL = "http://localhost:18033";
  
  public static final Integer NUM_DEVICES = 10;
  public static final Integer NUM_DAYS = 10;
  public static final Integer NUM_METRICS_PER_DAY = 10;
  
  @Autowired
  private MetricsProcessor processor;
  
  @Autowired
  private MongoTemplate template;
  
  @Autowired
  private CloudResourceRepository resourceRepository;
  
  @Autowired
  private ResourceMetricsRepository resourceMetricsRepository;
  
  @Autowired
  private FederationInfoRepository federationInfoRepository;
  
  FederationInfo coreInfo = new FederationInfo();
  
  MonitoringClient client;
  
  ZonedDateTime lastDate;
  ZonedDateTime firstDate;
  
  DateTimeFormatter inputFormat = DateTimeFormatter.ISO_INSTANT;
  
  @Before
  public void setup() {
  
    template.dropCollection(CloudResource.class);
    template.dropCollection(CloudMonitoringResource.class);
    template.dropCollection(FederationInfo.class);
    template.dropCollection(MonitoringMetric.class);
    
    coreInfo.setFederationId(MonitoringConstants.CORE_FED_ID);
    coreInfo.getMetrics().add(MonitoringConstants.AVAILABILITY_TAG);
    coreInfo.getMetrics().add(MonitoringConstants.LOAD_TAG);
    
    lastDate = Instant.now().atZone(ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0).withNano(0);
    
    
    firstDate = lastDate.minusDays(NUM_DAYS);
    lastDate = firstDate.minusDays(1);
    
    List<DeviceMetric> metrics = new ArrayList<>();
    
    for (int i=0; i<NUM_DEVICES; i++) {
      CloudResource resource = TestUtils.createResource(Integer.toString(i));
      resourceRepository.save(resource);
      
      for (int j = 0 ; j < NUM_DAYS ; j++) {
  
        lastDate = lastDate.plusDays(1);
        
        for (int k = 0; k < NUM_METRICS_PER_DAY; k++) {
  
          lastDate = lastDate.plusMinutes(1);
          
          metrics.add(generateMetric(resource.getInternalId(), MonitoringConstants.AVAILABILITY_TAG, 2,
              Date.from(lastDate.toInstant())));
          metrics.add(generateMetric(resource.getInternalId(), MonitoringConstants.LOAD_TAG, 101,
              Date.from(lastDate.toInstant())));
        }
        
      }
      
      if ((i % 2) == 0) {
        coreInfo.getDevices().add(resource.getInternalId());
      }
    }
  
    federationInfoRepository.save(coreInfo);
    
    
    client = Feign.builder()
                 .encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                 .target(MonitoringClient.class, MONITORING_URL);
    
    client.postMetrics(metrics);
    
  }
  
  @Test
  public void testFilters() {
    
    int total = NUM_DEVICES * 2 * NUM_DAYS * NUM_METRICS_PER_DAY;
  
    Map<String, String> params = new HashMap<>();
    
    List<DeviceMetric> metrics = client.getMetrics(params);
    
    assert metrics != null;
    assert metrics.size() == total;
    
    params.put("device", "0");
    
    metrics = client.getMetrics(params);
    
    assert metrics.size() == total / NUM_DEVICES;
    metrics.forEach(metric -> {
      assert "0".equals(metric.getDeviceId());
    });
    
    params.remove("device");
    params.put("type", TestUtils.RESOURCE_TYPE);
    
    metrics = client.getMetrics(params);
    assert metrics.size() == total;
  
    params.put("type", "nonexistent");
    
    metrics = client.getMetrics(params);
    assert metrics.size() == 0;
    
    params.remove("type");
    
    params.put("metric", MonitoringConstants.AVAILABILITY_TAG);
    metrics = client.getMetrics(params);
    
    assert metrics.size() == total / 2;
    metrics.forEach(metric -> {
      assert MonitoringConstants.AVAILABILITY_TAG.equals(metric.getTag());
    });
    
    params.put("metric", "nonexistent");
    
    metrics = client.getMetrics(params);
    assert metrics.size() == 0;
    
    params.remove("metric");
  

    ZonedDateTime start = firstDate.plusDays(1);
    params.put("startDate", DateTimeFormatter.ISO_INSTANT.format(start));
    
    metrics = client.getMetrics(params);
    metrics.forEach(metric -> {
      ZonedDateTime metricDate = ZonedDateTime.ofInstant(metric.getDate().toInstant(), ZoneId.of("UTC"));
      assert  start.isBefore(metricDate) || start.isEqual(metricDate);
    });
    
    ZonedDateTime end = lastDate.minusDays(1);
    params.remove("startDate");
    params.put("endDate", DateTimeFormatter.ISO_INSTANT.format(end));
    
    metrics = client.getMetrics(params);
    metrics.forEach(metric -> {
      ZonedDateTime metricDate = ZonedDateTime.ofInstant(metric.getDate().toInstant(), ZoneId.of("UTC"));
      assert  end.isAfter(metricDate) || end.isEqual(metricDate);
    });
    
    params.put("device", "0");
    params.put("metric", MonitoringConstants.AVAILABILITY_TAG);
    params.put("startDate", DateTimeFormatter.ISO_INSTANT.format(start));
    params.put("endDate", DateTimeFormatter.ISO_INSTANT.format(end));
    
    metrics = client.getMetrics(params);
    metrics.forEach(metric -> {
      
      assert "0".equals(metric.getDeviceId());
      assert MonitoringConstants.AVAILABILITY_TAG.equals(metric.getTag());
      
      ZonedDateTime metricDate = ZonedDateTime.ofInstant(metric.getDate().toInstant(), ZoneId.of("UTC"));
      
      assert (start.isBefore(metricDate) || start.isEqual(metricDate))
                 && (end.isAfter(metricDate) || end.isEqual(metricDate));
      
    });
    
  }
  
  private DeviceMetric generateMetric(String deviceId, String tag, int maxValue, Date date) {
    DeviceMetric metric = new DeviceMetric();
    metric.setDeviceId(deviceId);
    metric.setDate(date);
    metric.setTag(tag);
    metric.setValue(Integer.toString(ThreadLocalRandom.current().nextInt(0,maxValue)));
    return metric;
  }
  
  
  
  
  
}

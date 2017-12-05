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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
  
  @Before
  public void setup() {
  
    template.dropCollection(CloudResource.class);
    template.dropCollection(CloudMonitoringResource.class);
    template.dropCollection(FederationInfo.class);
    template.dropCollection(MonitoringMetric.class);
    
    coreInfo.setFederationId(MonitoringConstants.CORE_FED_ID);
    coreInfo.getMetrics().add(MonitoringConstants.AVAILABILITY_TAG);
    coreInfo.getMetrics().add(MonitoringConstants.LOAD_TAG);
  
    Calendar date = Calendar.getInstance();
    date.add(Calendar.DAY_OF_YEAR, -1 - NUM_DAYS);
    
    List<DeviceMetric> metrics = new ArrayList<>();
    
    for (int i=0; i<NUM_DEVICES; i++) {
      CloudResource resource = TestUtils.createResource(Integer.toString(i));
      resourceRepository.save(resource);
      
      for (int j = 0 ; j < NUM_DAYS ; j++) {
        
        date.add(Calendar.DAY_OF_YEAR, 1);
        
        for (int k = 0; k < NUM_METRICS_PER_DAY; k++) {
          
          date.add(Calendar.MINUTE, 1);
          
          metrics.add(generateMetric(resource.getInternalId(), MonitoringConstants.AVAILABILITY_TAG, 1, date.getTime()));
          metrics.add(generateMetric(resource.getInternalId(), MonitoringConstants.LOAD_TAG, 100, date.getTime()));
          
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
  public void testCoreResources() {
  
    
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

package eu.h2020.symbiote;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.db.CloudResourceRepository;
import eu.h2020.symbiote.db.ResourceMetricsRepository;
import eu.h2020.symbiote.rest.crm.MonitoringClient;
import eu.h2020.symbiote.service.MetricsProcessor;
import eu.h2020.symbiote.utils.MonitoringTestUtils;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {"eureka.client.enabled=false",
    "symbIoTe.aam.integration=false",
    "server.port=18033",
    "monitoring.mongo.database=monitoring-test",
    "symbIoTe.coreaam.url=http://localhost:8083",
    "symbIoTe.crm.integration=false",
    "platform.id=TestPlatform",
    "symbiote.crm.url=http://localhost:8083",
    "symbIoTe.aam.integration=false",
    "symbIoTe.coreaam.url=http://localhost:8083"})
public class MetricsProcessorTests {
  
  private interface Benchmark<T> extends eu.h2020.symbiote.utils.Benchmark<T> {
  }
  
  public static final String MONITORING_URL = "http://localhost:18033";
  
  public static final Integer NUM_DEVICES = 2;
  public static final Integer NUM_TAGS = 2;
  public static final Integer NUM_DAYS = 7;
  public static final Integer NUM_METRICS_PER_DAY = 1000;
  
  private static final Log logger = LogFactory.getLog(MetricsProcessorTests.class);
  
  @Autowired
  private MetricsProcessor processor;
  
  @Autowired
  private MongoTemplate template;
  
  @Autowired
  private CloudResourceRepository resourceRepository;
  
  @Autowired
  private ResourceMetricsRepository resourceMetricsRepository;
  
  
  @Before
  public void setup() {
    
    template.getDb().dropDatabase();
  
    List<CloudResource> resources = new ArrayList<>();
    
    for (int i = 0; i < NUM_DEVICES; i++) {
      resources.add(TestUtils.createResource(MonitoringTestUtils.DEVICE_PF+i));
    }
    
    resourceRepository.save(resources);
    
    MonitoringTestUtils.GenerationResults metrics = MonitoringTestUtils
                                                        .generateMetrics(NUM_DEVICES + 2,
                                                            NUM_TAGS, NUM_DAYS, NUM_METRICS_PER_DAY);
  
    MonitoringClient client = Feign.builder()
                                  .encoder(new JacksonEncoder())
                                  .decoder(new JacksonDecoder()).target(MonitoringClient.class,
            MONITORING_URL);
    
    client.postMetrics(metrics.getMetrics());
  }
  
  @Test
  public void testProcessor() {
  
    CloudMonitoringPlatform data = processor.getDataToSend();
    
    assert data.getMetrics().size() == NUM_DEVICES;
    
    data.getMetrics().forEach(deviceMetrics -> {
      assert deviceMetrics.getId().startsWith(TestUtils.SYMBIOTE_PREFIX+MonitoringTestUtils.DEVICE_PF);
      
      int deviceId = Character.getNumericValue(
          deviceMetrics.getId().charAt(deviceMetrics.getId().length() -1));
      
      assert deviceId < NUM_DEVICES;
      
      assert deviceMetrics.getMetrics().size() == NUM_TAGS * NUM_DAYS * NUM_METRICS_PER_DAY;
      
    });
    
  }
  
  
  
  
  
  
  
  
}

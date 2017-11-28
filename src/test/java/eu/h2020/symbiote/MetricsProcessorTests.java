package eu.h2020.symbiote;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.beans.FederationInfo;
import eu.h2020.symbiote.beans.MonitoringMetric;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.Metric;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.FederationInfoRepository;
import eu.h2020.symbiote.db.ResourceRepository;
import eu.h2020.symbiote.service.MetricsProcessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(SpringRunner.class)
@SpringBootTest({"eureka.client.enabled=false", "symbIoTe.aam.integration=false",
"monitoring.mongo.database=metrics-test"})
public class MetricsProcessorTests {
  
  public static final String SYMBIOTE_PREFIX = "symbiote_";
  
  @Autowired
  private MetricsProcessor processor;
  
  @Autowired
  private MongoTemplate template;
  
  @Autowired
  private ResourceRepository resourceRepository;
  
  @Autowired
  private FederationInfoRepository federationInfoRepository;
  
  FederationInfo coreInfo = new FederationInfo();
  
  @Before
  public void setup() {
  
    template.dropCollection(CloudMonitoringResource.class);
    template.dropCollection(FederationInfo.class);
    
    coreInfo.setFederationId(MonitoringConstants.CORE_FED_ID);
    coreInfo.getMetrics().add(MonitoringConstants.AVAILABILITY_TAG);
    coreInfo.getMetrics().add(MonitoringConstants.LOAD_TAG);
    
    for (int i=0; i<10; i++) {
      CloudResource resource = TestUtils.createResource(Integer.toString(i));
      CloudMonitoringResource monResource = new CloudMonitoringResource(resource);
      
      for (int j = 0 ; j < 10 ; j++) {
  
        boolean processed = j % 2 == 0;
        
        monResource.getMetrics().add(
            generateMetric(MonitoringConstants.AVAILABILITY_TAG, 2, processed));
  
        monResource.getMetrics().add(
            generateMetric(MonitoringConstants.LOAD_TAG, 101, processed));
        
      }
      
      resourceRepository.save(monResource);
      
      
      
      if ((i % 2) == 0) {
        coreInfo.getDevices().add(resource.getInternalId());
      }
    }
  
    federationInfoRepository.save(coreInfo);
    
  }
  
  @Test
  public void testCoreResources() {
    List<CloudMonitoringResource> coreResources = processor.getCoreMetrics();
    
    assert coreResources.size() == 5;
    
    coreResources.forEach(resource -> {
      assert new Integer(resource.getResource().getInternalId()) % 2 == 0;
      
      assert resource.getMetrics().size() == 10;
      
      resource.getMetrics().forEach(metric -> {
        assert metric.isProcessed() == false;
        assert coreInfo.getMetrics().contains(metric.getMetric().getTag());
      });
      
    });
    
  }
  
  private MonitoringMetric generateMetric(String tag, int maxValue, boolean processed) {
    Metric metric = new Metric();
    metric.setDate(new Date());
    metric.setTag(tag);
    metric.setValue(Integer.toString(ThreadLocalRandom.current().nextInt(0,maxValue)));
  
    MonitoringMetric finalMetric = new MonitoringMetric(metric);
    finalMetric.setProcessed(processed);
    
    return finalMetric;
  }
  
  
  
  
  
}

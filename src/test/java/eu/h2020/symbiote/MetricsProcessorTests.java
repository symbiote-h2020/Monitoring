package eu.h2020.symbiote;

import eu.h2020.symbiote.beans.FederationInfo;
import eu.h2020.symbiote.db.CloudResourceRepository;
import eu.h2020.symbiote.db.FederationInfoRepository;
import eu.h2020.symbiote.db.ResourceMetricsRepository;
import eu.h2020.symbiote.rest.crm.MonitoringClient;
import eu.h2020.symbiote.service.MetricsProcessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
  
  private interface Benchmark<T> extends eu.h2020.symbiote.utils.Benchmark<T> {
  }
  
  public static final String SYMBIOTE_PREFIX = "symbiote_";
  public static final String MONITORING_URL = "http://localhost:18033";
  
  public static final Integer NUM_DEVICES = 2;
  public static final Integer NUM_DAYS = 3;
  public static final Integer NUM_METRICS_PER_DAY = 3;
  
  private static final Log logger = LogFactory.getLog(MetricsProcessorTests.class);
  
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
  
    
  }
  
  
  
  
  
  
  
  
  
  
}

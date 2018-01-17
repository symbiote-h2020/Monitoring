package eu.h2020.symbiote.monitoring.tests;

import eu.h2020.symbiote.monitoring.beans.FederationInfo;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;
import eu.h2020.symbiote.cloud.monitoring.model.AggregationOperation;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.FederationInfoRepository;
import eu.h2020.symbiote.monitoring.db.MongoDbMonitoringBackend;
import eu.h2020.symbiote.monitoring.crm.MonitoringClient;
import eu.h2020.symbiote.monitoring.tests.utils.MonitoringTestUtils;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class MonitoringRestServiceTests {
  
  public static final Integer NUM_DEVICES = 10;
  public static final Integer NUM_TAGS = 10;
  public static final Integer NUM_DAYS = 10;
  public static final Integer NUM_METRICS_PER_DAY = 10;
  
  @Autowired
  private FederationInfoRepository federationInfoRepository;
  
  @Autowired
  private CloudResourceRepository cloudResourceRepository;
  
  private MongoDbMonitoringBackend backend;
  private MonitoringTestUtils.GenerationResults genResults;
  private MonitoringClient client;
  
  private DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
  
  @Before
  public void setUp() {
  
    backend = new MongoDbMonitoringBackend(null, "monitoring-test", "cloudMonitoringResource");
    backend.getCollection().drop();
    
    genResults = MonitoringTestUtils.generateMetrics(
        NUM_DEVICES, NUM_TAGS, NUM_DAYS, NUM_METRICS_PER_DAY);
  
    
    client = Feign.builder().encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                 .target(MonitoringClient.class, "http://localhost:18033");
    
  }
  
  @Test
  public void testInsertion() {
  
    List<DeviceMetric> result = client.postMetrics(genResults.getMetrics());
    
    assert result.size() == genResults.getMetrics().size();
    
    assert result.size() == backend.getMetrics(null, null, null, null).size();
    
  }
  
  @Test
  public void testGet() {
    
    backend.getCollection().drop();
    
    List<DeviceMetric> result = client.postMetrics(genResults.getMetrics());
  
    Map<String, String> parameters = new HashMap<>();
    
    List<DeviceMetric> got = client.getMetrics(parameters);
    
    List<DeviceMetric> backGot = backend.getMetrics(null, null, null, null);
    
    assert result.size() == got.size();
    assert got.size() == backGot.size();
    
    String device = MonitoringTestUtils.DEVICE_PF+"0";
    String tag = MonitoringTestUtils.TAG_PF+"0";
  
    Instant startInstant = genResults.getFirstDate().plusDays(1).toInstant();
    Instant endInstant = genResults.getLastDate().minusDays(1).toInstant();
    
    Date startDate = Date.from(startInstant);
    Date endDate = Date.from(endInstant);
    
    parameters.put("device", device);
    parameters.put("metric", tag);
    parameters.put("startDate", formatter.format(startInstant));
    parameters.put("endDate", formatter.format(endInstant));
    
    got = client.getMetrics(parameters);
    backGot = backend.getMetrics(Arrays.asList(device), Arrays.asList(tag), startDate, endDate);
    
    assert got.size() == backGot.size();
    
    HashMap<String, String> opParams = new HashMap<>();
    opParams.put("operation", AggregationOperation.AVG.toString());
    opParams.put("count", "100");
    
    List<AggregatedMetrics> gotAggregated = client.getAggregatedMetrics(opParams);
    List<AggregatedMetrics> backAggregated = backend.getAggregatedMetrics(
        null, null, null, null,
        Arrays.asList(AggregationOperation.AVG), Arrays.asList("100"));
    
    assert gotAggregated.size() == backAggregated.size();
    
    parameters.putAll(opParams);
    
    gotAggregated = client.getAggregatedMetrics(parameters);
    backAggregated = backend.getAggregatedMetrics(
        Arrays.asList(device), Arrays.asList(tag), startDate, endDate,
        Arrays.asList(AggregationOperation.AVG), Arrays.asList("100"));
    
    assert gotAggregated.size() == 1 && backAggregated.size() == 1;
    
    compareAggregation(gotAggregated.get(0), backAggregated.get(0));
  }
  
  @Test
  public void testFederations() {
  
    backend.getCollection().drop();
    
    federationInfoRepository.deleteAll();
    cloudResourceRepository.deleteAll();
  
    FederationInfo info = new FederationInfo();
    info.setFederationId("fed");
    Map<String, String> devicesType = new HashMap<>();
    
    for (int i=0; i < NUM_DEVICES; i++) {
      String type = TestUtils.RESOURCE_TYPE+(i%3);
      CloudResource resource = TestUtils.createResource(MonitoringTestUtils.DEVICE_PF+i, type);
      cloudResourceRepository.save(resource);
      devicesType.put(resource.getInternalId(), resource.getParams().getType());
      
      if (i%2 == 0) {
        TimedValue val = new TimedValue();
        val.setDate(Date.from(genResults.getFirstDate().plusDays(i).toInstant()));
        val.setValue(type);
        info.getResources().put(resource.getInternalId(), val);
      }
    }
    
    federationInfoRepository.save(info);
    
    backend.saveMetrics(genResults.getMetrics());
    
    String metric = MonitoringTestUtils.TAG_PF+"0";
    String composed = metric + ".all";
    Map<String, Double> results = client.getSummaryMetric("fed", composed);
    
    assert info.getResources().keySet().size() == results.keySet().size();
    assert info.getResources().keySet().containsAll(results.keySet());
    validateSummary(results, info, null, metric);
    
    composed = metric + ".5";
    Date startDate = Date.from(Instant.now().atZone(ZoneId.of("UTC")).minusDays(5).toInstant());
    
    results = client.getSummaryMetric("fed", composed);
  
    assert info.getResources().keySet().size() == results.keySet().size();
    assert info.getResources().keySet().containsAll(results.keySet());
    validateSummary(results, info, startDate, metric);
    
    composed = metric +"."+TestUtils.RESOURCE_TYPE+"0.5";
  
    startDate = Date.from(Instant.now().atZone(ZoneId.of("UTC")).minusDays(5).toInstant());
    results = client.getSummaryMetric("fed", composed);
    
    List<String> devices = cloudResourceRepository.findAll().stream()
                               .filter(resource -> resource.getParams().getType()
                                                       .equals(TestUtils.RESOURCE_TYPE+"0") &&
                                                       info.getResources().keySet()
                                                           .contains(resource.getInternalId()))
                               .map(resource -> resource.getInternalId())
                               .collect(Collectors.toList());
  
    assert devices.size() == results.keySet().size();
    assert devices.containsAll(results.keySet());
    validateSummary(results, info, startDate, metric);
    
  }
  
  private void validateSummary(Map<String, Double> results, FederationInfo info, Date startDate, String metric) {
    for (String key : results.keySet()) {
      Date start = info.getResources().get(key).getDate();
      if (startDate != null && startDate.after(start)) {
          start = startDate;
      }
      List<AggregatedMetrics> metrics = backend.getAggregatedMetrics(
          Arrays.asList(key), Arrays.asList(metric), start, null,
          Arrays.asList(AggregationOperation.AVG), null
      );
      
      if (results.containsKey(key)) {
        assert results.get(key).equals(metrics.get(0).getStatistics().get(AggregationOperation.AVG.toString()));
      }
    }
  }
  
  private void compareAggregation(AggregatedMetrics got, AggregatedMetrics back) {
    
    assert got.getDeviceId().equals(back.getDeviceId());
    assert got.getTag().equals(back.getTag());
    assert got.getValues().size() == back.getValues().size();
    assert got.getStatistics()
               .get(AggregationOperation.AVG.toString()).equals(back.getStatistics()
                                                                .get(AggregationOperation.AVG.toString()));
    assert got.getCounts().get("100").equals(back.getCounts().get("100"));
  }
  
  
}
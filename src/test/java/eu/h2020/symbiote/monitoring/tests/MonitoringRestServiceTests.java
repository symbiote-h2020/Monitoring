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

package eu.h2020.symbiote.monitoring.tests;

import eu.h2020.symbiote.client.MonitoringClient;
import eu.h2020.symbiote.client.SymbioteComponentClientFactory;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.model.internal.ResourceSharingInformation;
import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;
import eu.h2020.symbiote.cloud.monitoring.model.AggregationOperation;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.model.cim.Actuator;
import eu.h2020.symbiote.model.cim.Sensor;
import eu.h2020.symbiote.monitoring.beans.CloudMonitoringResource;
import eu.h2020.symbiote.monitoring.beans.FederatedDeviceInfo;
import eu.h2020.symbiote.monitoring.beans.FederationInfo;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.FederationInfoRepository;
import eu.h2020.symbiote.monitoring.db.MongoDbMonitoringBackend;
import eu.h2020.symbiote.monitoring.tests.utils.MonitoringTestUtils;
import eu.h2020.symbiote.monitoring.tests.utils.TestUtils;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
        "server.port=18035",
        "symbIoTe.core.interface.url=http://localhost:18035/coreInterface/v1/"
})
//@SpringBootTest( webEnvironment = WebEnvironment.DEFINED_PORT, properties = {"eureka.client.enabled=false", "spring.cloud.sleuth.enabled=false", "platform.id=helloid", "server.port=18033", "symbIoTe.core.cloud.interface.url=http://localhost:18033/testiifnosec", "security.coreAAM.url=http://localhost:18033", "security.rabbitMQ.ip=localhost", "security.enabled=false", "symbIoTe.component.username=user", "symbIoTe.component.password=password"})
@Configuration
@ComponentScan
@TestPropertySource(
        locations = "classpath:test.properties")
public class MonitoringRestServiceTests {
  
  public static final Integer NUM_DEVICES = 10;
  public static final Integer NUM_TAGS = 10;
  public static final Integer NUM_DAYS = 10;
  public static final Integer NUM_METRICS_PER_DAY = 10;
  
  @Autowired
  private FederationInfoRepository federationInfoRepository;
  
  @Autowired
  private CloudResourceRepository cloudResourceRepository;

  @Autowired
  private MongoTemplate template;

  @Value("${monitoring.mongo.database}")
  private String mongoDatabase;
  
  private MongoDbMonitoringBackend backend;
  private MonitoringTestUtils.GenerationResults genResults;
  private MonitoringClient client;
  
  private DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

  public static final String SENSOR_TYPE = Sensor.class.getSimpleName();
  public static final String ACTUATOR_TYPE = Actuator.class.getSimpleName();
  
  @Before
  public void setUp() {

    template.getDb().dropDatabase();

    backend = new MongoDbMonitoringBackend(null, mongoDatabase,
            template.getCollectionName(CloudMonitoringResource.class));
    
    genResults = MonitoringTestUtils.generateMetrics(
        NUM_DEVICES, NUM_TAGS, NUM_DAYS, NUM_METRICS_PER_DAY);


    try {
      client = SymbioteComponentClientFactory.createClient("http://localhost:18035", MonitoringClient.class, null);
    } catch (SecurityHandlerException e) {
      e.printStackTrace();
    }

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

    boolean actuator = false;
    for (int i=0; i < NUM_DEVICES; i++) {
      String type = (actuator)?ACTUATOR_TYPE:SENSOR_TYPE;
      CloudResource resource = TestUtils.createResource(MonitoringTestUtils.DEVICE_PF+i, actuator);
      cloudResourceRepository.save(resource);
      
      if (i%2 == 0) {
        FederatedDeviceInfo resVal = new FederatedDeviceInfo();
        resVal.setSymbioteId(UUID.randomUUID().toString());
        resVal.setType(type);
        ResourceSharingInformation resSharingInfo = new ResourceSharingInformation();
        resSharingInfo.setSharingDate(Date.from(genResults.getFirstDate().plusDays(i).toInstant()));
        resSharingInfo.setBartering(false);
        resVal.setSharingInformation(resSharingInfo);
        info.getResources().put(resource.getInternalId(), resVal);
        actuator = !actuator;
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
    
    composed = metric +"."+ Actuator.class.getSimpleName() +".5";
  
    startDate = Date.from(Instant.now().atZone(ZoneId.of("UTC")).minusDays(5).toInstant());
    results = client.getSummaryMetric("fed", composed);
    
    List<String> devices = cloudResourceRepository.findAll().stream()
                               .filter(resource -> resource.getResource().getClass().getSimpleName()
                                                       .equals(ACTUATOR_TYPE) &&
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
      Date start = info.getResources().get(key).getSharingInformation().getSharingDate();
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

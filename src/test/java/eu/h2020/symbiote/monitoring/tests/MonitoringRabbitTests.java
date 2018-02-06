package eu.h2020.symbiote.monitoring.tests;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.cloud.model.FederatedResource;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.monitoring.beans.FederationInfo;
import eu.h2020.symbiote.monitoring.constants.MonitoringConstants;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.FederationInfoRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNotNull;

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
public class MonitoringRabbitTests {
  
  private static Logger logger = LoggerFactory.getLogger(MonitoringRabbitTests.class);
  
  @Autowired
  private CloudResourceRepository resourceRepo;
  
  @Autowired
  private FederationInfoRepository federationInfoRepository;
  
  @Autowired
  private RabbitTemplate rabbitTemplate;
  
  public static final int NUM_FEDERATIONS = 3;
  
  public static final int NUM_RESOURCES = 100;
  
  private ObjectMapper mapper = new ObjectMapper();
  
  @Before
  public void setup() throws IOException, TimeoutException {
    
    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    
    resourceRepo.deleteAll();
    federationInfoRepository.deleteAll();
  }
  
  
  @Test
  public void coreDevicesTest() throws Exception {
    
    List<CloudResource> toAdd = new ArrayList<>();
    
    for (int i = 0; i < NUM_RESOURCES; i++) {
      toAdd.add(getResource(Integer.toString(i)));
    }
    
    sendResourceMessage(MonitoringConstants.RESOURCE_REGISTRATION_KEY, toAdd);
    
    List<CloudResource> result = resourceRepo.findAll();
    
    assertNotNull(result);
    
    assert result.size() == NUM_RESOURCES;
    result.forEach(resource -> {
      assert Integer.valueOf(resource.getInternalId()) < NUM_RESOURCES;
    });
    
    List<String> toDelete = new ArrayList<>();
  
  
    for (int i = 0; i < NUM_RESOURCES; i++) {
      if (i % 2 == 0) {
        toDelete.add(Integer.toString(i));
      }
    }
  
    sendResourceMessage(MonitoringConstants.RESOURCE_UNREGISTRATION_KEY, toDelete);
  
    result = resourceRepo.findAll();
    
    assertNotNull(result);
    
    assert result.size() == NUM_RESOURCES / 2;
    
    result.forEach(resource -> {
      assert Integer.valueOf(resource.getInternalId()) % 2 == 1;
    });
  }
  
  private Map<String, FederatedResource> addToFederations(int resourceStart, int resourceEnd) throws Exception {
  
    Map<String, FederatedResource> result = new HashMap<>();
    
    for (int f = 0; f < NUM_FEDERATIONS; f++) {
      FederatedResource federation = new FederatedResource();
      federation.setSharingDate(new Date());
      federation.setIdFederation(Integer.toString(f));
    
      List<CloudResource> resources = new ArrayList<>();
    
      for (int r = resourceStart; r < resourceEnd; r ++) {
      
        if (r % 2 == f % 2) {
          resources.add(getResource(Integer.toString(r)));
        }
      
      }
    
      federation.setResources(resources);
      
      sendResourceMessage(MonitoringConstants.RESOURCE_SHARING_KEY, federation);
      
      result.put(federation.getIdFederation(), federation);
    }
    
    return result;
  }
  
  public void testFederationPartial(int resourceStart, int resourceEnd, int totalResources) throws Exception {
    Map<String, FederatedResource> created = addToFederations(resourceStart, resourceEnd);
  
    List<FederationInfo> federations = federationInfoRepository.findAll();
  
    assertNotNull(federations);
  
    assert federations.size() == NUM_FEDERATIONS;
  
    federations.forEach(federation -> {
      int fedId = Integer.valueOf(federation.getFederationId());
      FederatedResource fedInfo = created.get(federation.getFederationId());
    
      assert fedInfo != null;
      assert fedId < NUM_FEDERATIONS;
      assert federation.getResources().size() == totalResources/2;
    
      federation.getResources().forEach((resourceId, value) -> {
        int resId = Integer.valueOf(resourceId);
      
        assert resId % 2 == fedId % 2;
        assert value != null;
        assert value.getDate() != null;
        
        if (resId >= resourceStart && resId < resourceEnd) {
          assert value.getDate().equals(fedInfo.getSharingDate());
        }
      
      });
    
    });
  }
  
  @Test
  public void federationDevicesTest() throws Exception {
    
    //test federation creation with half of the resources
    testFederationPartial(0, NUM_RESOURCES/2, NUM_RESOURCES/2);
    
    //test federation update with the other half
    testFederationPartial(NUM_RESOURCES/2, NUM_RESOURCES, NUM_RESOURCES);
    
    for (int i = 0; i < NUM_FEDERATIONS; i++) {
      FederatedResource federation = new FederatedResource();
      federation.setIdFederation(Integer.toString(i));
      
      boolean delete = true;
      for (int j = 0; j < NUM_RESOURCES; j++) {
        if (i % 2 == j % 2) {
          if (delete) {
            federation.getResources().add(getResource(Integer.toString(j)));
          }
          delete = !delete;
        }
      }
      
      sendResourceMessage(MonitoringConstants.RESOURCE_UNSHARING_KEY, federation);
    }
  
    List<FederationInfo> federations = federationInfoRepository.findAll();
  
    assertNotNull(federations);
  
    assert federations.size() == NUM_FEDERATIONS;
  
    federations.forEach(federation -> {
      assert federation.getResources().size() == NUM_RESOURCES/4;
    });
    
  }
  
  
  private CloudResource getResource(String id) {
    return TestUtils.createResource(id);
  }
  
  void sendResourceMessage(String key, Object toSend) throws Exception {
  
    MessageProperties properties = new MessageProperties();
    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
    
    Message message = MessageBuilder.withBody(mapper.writeValueAsBytes(toSend))
                          .andProperties(properties).build();
    
    rabbitTemplate.convertAndSend(MonitoringConstants.EXCHANGE_NAME_RH, key, message);
    TimeUnit.SECONDS.sleep(1);
  }
  
}

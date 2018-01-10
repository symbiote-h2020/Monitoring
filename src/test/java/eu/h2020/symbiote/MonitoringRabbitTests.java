package eu.h2020.symbiote;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.CloudResourceRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
  private RabbitTemplate rabbitTemplate;
  
  
  private static String RESOURCE_ID = "resource_id";
  
  @Before
  public void setup() throws IOException, TimeoutException {
    
    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    
    resourceRepo.delete(RESOURCE_ID);
  }
  
  
  @Test
  public void coreDevicesTest() throws Exception {
    
    CloudResource resource = getResource(RESOURCE_ID);
    sendResourceMessage(MonitoringConstants.RESOURCE_REGISTRATION_KEY, Arrays.asList(resource));
    
    CloudResource result = resourceRepo.findOne(RESOURCE_ID);
    
    assertNotNull(result);
    
    assertEquals(resource.getResource().getInterworkingServiceURL(),
        result.getResource().getInterworkingServiceURL());
  
  
    sendResourceMessage(MonitoringConstants.RESOURCE_UNREGISTRATION_KEY, Arrays.asList(RESOURCE_ID));
  
    result = resourceRepo.findOne(RESOURCE_ID);
    
    assertNull(result);
  }
  
  
  private CloudResource getResource(String id) {
    return TestUtils.createResource(id);
  }
  
  void sendResourceMessage(String key, Object message) throws Exception {
    
    rabbitTemplate.convertAndSend(MonitoringConstants.EXCHANGE_NAME_RH, key, message);
    TimeUnit.SECONDS.sleep(1);
  }
  
}

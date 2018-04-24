package eu.h2020.symbiote.monitoring.tests.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.model.cim.Actuator;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.model.cim.Sensor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.concurrent.TimeUnit;

public class TestUtils {
  
  public static final String SYMBIOTE_PREFIX = "symbiote_";
  
  public static CloudResource createResource(String id, boolean actuator) {
    CloudResource resource = new CloudResource();
  
    resource.setInternalId(id);
  
    Resource r = (actuator)?new Actuator():new Sensor();
    r.setId(SYMBIOTE_PREFIX+id);
    r.setInterworkingServiceURL("http://tests.io/interworking/url");
    resource.setResource(r);
  
    return resource;
  
  }

  public static <T> void sendMessage(RabbitTemplate template, String exchangeName, String exchangeKey, T payload)
          throws JsonProcessingException, InterruptedException {
    MessageProperties properties = new MessageProperties();
    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

    Message message = MessageBuilder.withBody(new ObjectMapper().writeValueAsBytes(payload))
            .andProperties(properties).build();

    template.convertAndSend(exchangeName, exchangeKey, message);
    TimeUnit.SECONDS.sleep(1);
  }
  
}

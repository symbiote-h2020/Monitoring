package eu.h2020.symbiote.monitoring.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.monitoring.beans.FederationInfo;
import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;
import eu.h2020.symbiote.cloud.model.FederatedResource;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.monitoring.constants.MonitoringConstants;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.FederationInfoRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class PlatformMonitoringRabbitServerService {
  
  private static Log logger = LogFactory.getLog(PlatformMonitoringRabbitServerService.class);
  
  @Autowired
  CloudResourceRepository coreRepository;
  
  @Autowired
  FederationInfoRepository federationRepository;
  
  private ObjectMapper mapper = new ObjectMapper();
  
  /**
   * Spring AMQP Listener for resource registration requests. This method is invoked when Registration
   * Handler sends a resource registration request and it is responsible for forwarding the message
   * to the symbIoTe core. As soon as it receives a reply, it manually sends back the response
   * to the Registration Handler via the appropriate message queue by the use of the RestAPICallback.
   *
   * @param message List of resources to add process
   */
  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = MonitoringConstants.MONITORING_REGISTRATION_QUEUE_NAME, durable = "true",
          exclusive = "true", autoDelete = "true"),
      exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_RH, durable = "true"),
      key = MonitoringConstants.RESOURCE_REGISTRATION_KEY)
  )
  public void resourceRegistration(@Payload Message message) {
    List<CloudResource> resources = toList(message, new TypeReference<List<CloudResource>>() {});
    coreRepository.save(resources);
  }
  
  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = MonitoringConstants.MONITORING_UNREGISTRATION_QUEUE_NAME, durable = "true",
          exclusive = "true", autoDelete = "true"),
      exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_RH, durable = "true"),
      key = MonitoringConstants.RESOURCE_UNREGISTRATION_KEY)
  )
  public void resourceUnregistration(@Payload Message message) {
    List<String> resources = toList(message, new TypeReference<List<String>>() {});
    for (String resourceId : resources) {
      coreRepository.delete(resourceId);
    }

  }
  
  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = MonitoringConstants.MONITORING_SHARING_QUEUE_NAME, durable = "true",
          exclusive = "true", autoDelete = "true"),
      exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_RH, durable = "true"),
      key = MonitoringConstants.RESOURCE_SHARING_KEY)
  )
  public void resourceSharing(@Payload Message message) {
    FederatedResource resources = toObject(message, FederatedResource.class);
    if (resources != null && resources.getSharingDate() != null && resources.getResources() != null) {
      FederationInfo fedInfo = federationRepository.findOne(resources.getIdFederation());
  
      if (fedInfo == null) {
        fedInfo = new FederationInfo();
        fedInfo.setFederationId(resources.getIdFederation());
      }
  
      for (CloudResource resource : resources.getResources()) {
    
        TimedValue value = new TimedValue();
        value.setDate(resources.getSharingDate());
        if (resource.getParams() != null) {
          value.setValue(resource.getParams().getType());
        }
    
        fedInfo.getResources().put(resource.getInternalId(), value);
      }
  
      federationRepository.save(fedInfo);
    } else {
      logger.warn("Malformed resource sharing information received " + message.getBody());
    }
  }
  
  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = MonitoringConstants.MONITORING_UNSHARING_QUEUE_NAME, durable = "true",
          exclusive = "true", autoDelete = "true"),
      exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_RH, durable = "true"),
      key = MonitoringConstants.RESOURCE_UNSHARING_KEY)
  )
  public void resourceUnsharing(@Payload Message message) {
  
    FederatedResource resources = toObject(message, FederatedResource.class);
    if (resources != null && resources.getResources() != null) {
      FederationInfo fedInfo = federationRepository.findOne(resources.getIdFederation());
  
      if (fedInfo != null) {
        resources.getResources().forEach(resource -> {
          fedInfo.getResources().remove(resource.getInternalId());
        });
    
        federationRepository.save(fedInfo);
      }
    } else {
      logger.warn("Malformed resource sharing information received " + message.getBody());
    }
  }
  
  private <T> T toList(Message message, TypeReference<T> reference) {
    try {
      return mapper.readValue(message.getBody(), reference);
    } catch (IOException e) {
      logger.warn("Invalid JSON message received: " + message.getBody(), e);
    }
    
    return (T) new ArrayList<Object>();
  }
  
  private <T> T toObject(Message message, Class<T> clazz) {
    try {
      return mapper.readValue(message.getBody(), clazz);
    } catch (IOException e) {
      logger.warn("Invalid JSON message received: " + message.getBody(), e);
    }
    
    return null;
  }
}

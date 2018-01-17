package eu.h2020.symbiote.monitoring.service;

import eu.h2020.symbiote.monitoring.beans.FederationInfo;
import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;
import eu.h2020.symbiote.cloud.model.FederatedResource;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.monitoring.constants.MonitoringConstants;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.FederationInfoRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PlatformMonitoringRabbitServerService {
  
  private static Log log = LogFactory.getLog(PlatformMonitoringRabbitServerService.class);
  
  @Autowired
  CloudResourceRepository coreRepository;
  
  @Autowired
  FederationInfoRepository federationRepository;
  
  
  /**
   * Spring AMQP Listener for resource registration requests. This method is invoked when Registration
   * Handler sends a resource registration request and it is responsible for forwarding the message
   * to the symbIoTe core. As soon as it receives a reply, it manually sends back the response
   * to the Registration Handler via the appropriate message queue by the use of the RestAPICallback.
   *
   * @param resources List of resources to add process
   */
  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = MonitoringConstants.MONITORING_REGISTRATION_QUEUE_NAME, durable = "true",
          exclusive = "true", autoDelete = "true"),
      exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_RH, durable = "true"),
      key = MonitoringConstants.RESOURCE_REGISTRATION_KEY)
  )
  public void resourceRegistration(@Payload List<CloudResource> resources) {
    coreRepository.save(resources);
  }
  
  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = MonitoringConstants.MONITORING_UNREGISTRATION_QUEUE_NAME, durable = "true",
          exclusive = "true", autoDelete = "true"),
      exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_RH, durable = "true"),
      key = MonitoringConstants.RESOURCE_UNREGISTRATION_KEY)
  )
  public void resourceUnregistration(@Payload List<String> resources) {

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
  public void resourceSharing(@Payload FederatedResource resources) {
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
  }
  
  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = MonitoringConstants.MONITORING_UNSHARING_QUEUE_NAME, durable = "true",
          exclusive = "true", autoDelete = "true"),
      exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_RH, durable = "true"),
      key = MonitoringConstants.RESOURCE_UNSHARING_KEY)
  )
  public void resourceUnsharing(@Payload FederatedResource resources) {
    
    FederationInfo fedInfo = federationRepository.findOne(resources.getIdFederation());
    
    if (fedInfo != null) {
      resources.getResources().forEach(resource -> {
        fedInfo.getResources().remove(resource.getInternalId());
      });
      
      federationRepository.save(fedInfo);
    }
    
    
  }
}

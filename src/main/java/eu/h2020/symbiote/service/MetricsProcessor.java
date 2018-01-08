package eu.h2020.symbiote.service;

import eu.h2020.symbiote.AppConfig;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.db.CloudResourceRepository;
import eu.h2020.symbiote.db.MetricsRepository;
import eu.h2020.symbiote.rest.crm.CRMRestService;
import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.SymbioteAuthorizationClient;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;

import feign.Client;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * This class implements the rest interfaces. Initially created by jose
 *
 * @author: Elena Garrido, David Rojo; Fernando Campos
 * @version: 02/11/2017
 */
@Component
public class MetricsProcessor {
  private static final Log logger = LogFactory.getLog(MetricsProcessor.class);
  
  @Value("${symbIoTe.crm.integration:true}")
  private boolean pubCRM;
  
  @Value("${platform.id}")
  private String platformId;
  
  @Value("${symbiote.crm.url:}")
  private String crmUrl;
  
  @Value("${symbIoTe.aam.integration:true}")
  private boolean useSecurity;
  
  @Value("${symbIoTe.coreaam.url:}")
  private String coreAAMAddress;
  
  @Value("${symbIoTe.component.keystore.password:}")
  private String keystorePassword;
  
  @Value("${symbIoTe.component.keystore.path:}")
  private String keystorePath;
  
  @Value("${symbIoTe.component.clientId:}")
  private String clientId;
  
  @Value("${symbIoTe.localaam.url:}")
  private String localAAMAddress;
  
  @Value("${symbIoTe.component.username:}")
  private String username;
  
  @Value("${symbIoTe.component.password:}")
  private String password;
  
  @Autowired
  private CloudResourceRepository resourceRepository;
  
  @Autowired
  private MetricsRepository monitoringRepository;
  
  @Autowired
  private AppConfig config;
  
  @Autowired
  private MongoTemplate template;
  
  private CRMRestService jsonclient;
  
  @PostConstruct
  public void createClient() throws SecurityHandlerException {
    
    Feign.Builder builder = Feign.builder()
                                .decoder(new JacksonDecoder())
                                .encoder(new JacksonEncoder());
    if (useSecurity) {
      IComponentSecurityHandler secHandler = ComponentSecurityHandlerFactory
                                                 .getComponentSecurityHandler(
                                                     coreAAMAddress, keystorePath, keystorePassword,
                                                     clientId, localAAMAddress, false,
                                                     username, password
                                                 );
      
      Client client = new SymbioteAuthorizationClient(
                                                         secHandler, "crm", SecurityConstants.CORE_AAM_INSTANCE_ID,
                                                         new Client.Default(null, null));
      
      logger.info("Will use " + crmUrl + " to access to interworking interface");
      builder = builder.client(client);
    }
    
    jsonclient = builder.target(CRMRestService.class, crmUrl);
  }
  
  //@Scheduled(cron = "${symbiote.crm.publish.period}")
  public void publishMonitoringDataCrm() throws Exception{
  
  
    List<DeviceMetric> toSend = monitoringRepository.findAll();
  
    Map<String, CloudMonitoringDevice> resources = new HashMap<>();
    
  
    CloudMonitoringPlatform payload = new CloudMonitoringPlatform();
    payload.setPlatformId(platformId);
  
    toSend.forEach(metric -> {
  
      String resourceId = metric.getDeviceId();
      
      CloudMonitoringDevice monitoringDevice = resources.get(resourceId);
      if (monitoringDevice == null) {
        CloudResource resource = resourceRepository.findOne(resourceId);
        if (resource != null) {
          monitoringDevice = new CloudMonitoringDevice();
          monitoringDevice.setId(resourceId);
          monitoringDevice.setType(resource.getParams().getType());
          monitoringDevice.setMetrics(new ArrayList<>());
        }
      }
  
      if (monitoringDevice != null) {
        monitoringDevice.getMetrics().add(metric);
      }
      
    });
    
    payload.setMetrics(new ArrayList<>(resources.values()));
    
    jsonclient.doPost2Crm(platformId, payload);
  }
  
}

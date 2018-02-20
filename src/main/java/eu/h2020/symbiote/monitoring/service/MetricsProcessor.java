package eu.h2020.symbiote.monitoring.service;

import eu.h2020.symbiote.client.CRMRestService;
import eu.h2020.symbiote.client.SymbioteClientFactory;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.monitoring.MongoConfig;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.MetricsRepository;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private MongoConfig config;
  
  @Autowired
  private MongoTemplate template;
  
  private CRMRestService jsonclient;
  
  @PostConstruct
  private void createClient() throws SecurityHandlerException {

    SymbioteClientFactory.SecurityConfiguration securityConfiguration = (useSecurity)?new SymbioteClientFactory.SecurityConfiguration(keystorePath, keystorePassword, clientId, platformId,
            "crm", coreAAMAddress, username, password): null;
    jsonclient = SymbioteClientFactory.createClient(crmUrl, CRMRestService.class,securityConfiguration);
  }
  
  @Scheduled(cron = "${symbiote.crm.publish.period}")
  public void publishMonitoringDataCrm() throws Exception{
    jsonclient.doPost2Crm(platformId, getDataToSend());
    
    monitoringRepository.deleteAll();
  }
  
  public CloudMonitoringPlatform getDataToSend() {
    List<DeviceMetric> toSend = monitoringRepository.findAll();
  
    Map<String, CloudMonitoringDevice> resources = new HashMap<>();
  
  
    CloudMonitoringPlatform payload = new CloudMonitoringPlatform();
    payload.setPlatformId(platformId);
  
    toSend.forEach(metric -> {
    
      String resourceId = metric.getDeviceId();
    
      CloudMonitoringDevice monitoringDevice = resources.get(resourceId);
      if (monitoringDevice == null) {
        CloudResource resource = resourceRepository.findOne(resourceId);
        if (resource != null && resource.getResource() != null) {
          monitoringDevice = new CloudMonitoringDevice();
          monitoringDevice.setId(resource.getResource().getId());
          monitoringDevice.setMetrics(new ArrayList<>());
          resources.put(resourceId, monitoringDevice);
        }
      }
    
      if (monitoringDevice != null) {
        monitoringDevice.getMetrics().add(metric);
      }
    
    });
  
    payload.setMetrics(new ArrayList<>(resources.values()));
    
    return payload;
  }
  
}

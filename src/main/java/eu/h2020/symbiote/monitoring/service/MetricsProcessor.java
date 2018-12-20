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

package eu.h2020.symbiote.monitoring.service;

import eu.h2020.symbiote.client.CRMRestService;
import eu.h2020.symbiote.client.SymbioteComponentClientFactory;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.monitoring.MongoConfig;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.MetricsRepository;
import eu.h2020.symbiote.monitoring.utils.SecurityHandlerManager;
import eu.h2020.symbiote.security.commons.SecurityConstants;
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
  
  @Value("${symbIoTe.core.cloud.interface.url}")
  private String crmUrl;
  
  @Autowired
  private CloudResourceRepository resourceRepository;
  
  @Autowired
  private MetricsRepository monitoringRepository;
  
  @Autowired
  private MongoConfig config;
  
  @Autowired
  private MongoTemplate template;

  @Autowired
  private SecurityHandlerManager secHandlerManager;
  
  private CRMRestService jsonclient;
  
  @PostConstruct
  private void createClient() throws SecurityHandlerException {

    jsonclient = SymbioteComponentClientFactory.createClient(crmUrl, CRMRestService.class, "crm",
            SecurityConstants.CORE_AAM_INSTANCE_ID, secHandlerManager.getSecurityHandler());
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

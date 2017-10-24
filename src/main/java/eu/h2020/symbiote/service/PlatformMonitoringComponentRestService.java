package eu.h2020.symbiote.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import eu.h2020.symbiote.Icinga2Manager;
import  eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.rest.crm.CRMMessageHandler;

/**
 * This class implements the rest interfaces. Initially created by jose
 *
 * @author: Elena Garrido, David Rojo
 * @version: 30/01/2017
 */
@Component
public class PlatformMonitoringComponentRestService {
  private static final Log logger = LogFactory.getLog(PlatformMonitoringComponentRestService.class);
  
  @Autowired
  private CRMMessageHandler crmMessageHandler;
  
  @Autowired
  private PlatformMonitoringRestService platformManager;
  
  @Autowired
  private Icinga2Manager icigna2manager;

  //@Scheduled(cron = "${symbiote.crm.publish.period}")
  public void publishMonitoringData2Crm() throws Exception{
	  
	  logger.info("Polling...");
	  
	  CloudMonitoringPlatform platform = platformManager.getMonitoringInfo();
	  if (platform != null){
		  logger.info("Publishing monitoring info to CRM");
		  logger.info("Platform " + platform.getInternalId() + " has " + platform.getDevices().length + " devices");
		  for (int i = 0; i<platform.getDevices().length; i++){
			  logger.info("Device " + platform.getDevices()[i].getId());
		  }
		  //Send data to POST endpoint in CRM
		  String result = crmMessageHandler.doPost2Crm(platform);
		  logger.info("************** Result of post to crm = " + result);
		  logger.info("Publishing monitoring data for platform " + platform.getInternalId());
		  logger.info("Platform " + platform.getInternalId() + " has " + platform.getDevices().length + " devices");
		  for (int i = 0; i<platform.getDevices().length; i++){
			  logger.info("Device " + platform.getDevices()[i].getId());
			  logger.info("load: " + platform.getDevices()[i].getLoad());
			  logger.info("availability: " + platform.getDevices()[i].getAvailability());
			  logger.info("timestamp: " + platform.getDevices()[i].getTimemetric());		  
		  }

	  }	 
  }
  


}

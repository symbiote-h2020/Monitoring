package eu.h2020.symbiote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.constants.MonitoringConstants;

/**
 * Manage the REST operations from Platform using MongoDB 
 * @author: Fernando Campos
 * @version: 19/04/2017
 */
@RestController
@RequestMapping("/")
public class PlatformManager {

	 private static final Log logger = LogFactory.getLog(PlatformManager.class);
	 
	 
	 @Value("${platform.id}")
	 private String platformId;
	 
	 /**
	  * Listen from Platform Host
	  */
	 @RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.SUBSCRIBE_MONITORING_DATA,  produces = "application/json", consumes = "application/json")
	 public @ResponseBody String  MonitorRestServer(@PathVariable("platformId") String platformId, @RequestBody CloudMonitoringPlatform platform) {
		  logger.info("*********************************************************");
		  logger.info("Listening monitoring data from platform " + platformId);
		  logger.info("Platform " + platform.getInternalId() + " has " + platform.getDevices().length + " devices");
		  for (int i = 0; i<platform.getDevices().length; i++){
			  logger.info("Device " + platform.getDevices()[i].getId());
			  logger.info("load: " + platform.getDevices()[i].getLoad());
			  logger.info("availability: " + platform.getDevices()[i].getAvailability());
			  logger.info("timestamp: " + platform.getDevices()[i].getTimestamp());		  
		  }
		  return "received";
	  }
	 

}

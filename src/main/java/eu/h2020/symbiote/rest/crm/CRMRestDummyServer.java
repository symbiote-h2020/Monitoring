package eu.h2020.symbiote.rest.crm;


import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.cloud.monitoring.model.Metric;
import eu.h2020.symbiote.constants.MonitoringConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author: Elena Garrido, David Rojo
 * @version: 12/02/2017
 */
@RestController
//@WebAppConfiguration
@RequestMapping("/")
public class CRMRestDummyServer {
  private static final Log logger = LogFactory.getLog(CRMRestDummyServer.class);
  
  
  @RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.PUBLISH_MONITORING_DATA,  produces = "application/json", consumes = "application/json")
  public @ResponseBody String  publishMonitoringData(@PathVariable("platformId") String platformId, @RequestBody CloudMonitoringPlatform platform) {
	  logger.info("*********************************************************");
	  logger.info("Publishing monitoring data for platform " + platformId);
	  logger.info("Platform " + platform.getPlatformId() + " has " + platform.getMetrics().size() + " devices");
	  for (CloudMonitoringDevice device : platform.getMetrics()){
		  logger.info("Device " + device.getId());
		  logger.info("Metrics: " + device.getMetrics().size());
		  for (Metric metric : device.getMetrics()) {
		  	logger.info("Tag: " + metric.getTag());
				logger.info("Value: " + metric.getValue());
				logger.info("Date: " + metric.getDate());
			}
	  }
	  return "received";
  }
  
}


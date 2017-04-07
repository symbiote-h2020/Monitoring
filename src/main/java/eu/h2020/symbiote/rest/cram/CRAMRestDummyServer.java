package eu.h2020.symbiote.rest.cram;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringPlatform;


/*
 * @author: Elena Garrido
 * @version: 12/02/2017
 */
@RestController
//@WebAppConfiguration
@RequestMapping("/")
public class CRAMRestDummyServer {
  private static final Log logger = LogFactory.getLog(CRAMRestDummyServer.class);
  
  
  @RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.PUBLISH_MONITORING_DATA,  produces = "application/json", consumes = "application/json")
  public @ResponseBody String  publishMonitoringData(@PathVariable("platformId") String platformId, @RequestBody CloudMonitoringPlatform platform) {
	  logger.info("*********************************************************");
	  logger.info("Publishing monitoring data for platform " + platformId);
	  logger.info("Platform " + platform.getInternalId() + " has " + platform.getDevices().length + " devices");
	  for (int i = 0; i<platform.getDevices().length; i++){
		  logger.info("Device " + platform.getDevices()[i].getId());
		  logger.info("load: " + platform.getDevices()[i].getLoad());
		  logger.info("availability: " + platform.getDevices()[i].getAvailability());
		  logger.info("timestamp: " + platform.getDevices()[i].getTimestamp());		  
	  }
	  return "received";
  }

  
//  @RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.DO_CREATE_RESOURCES,  produces = "application/json", consumes = "application/json")
//  public @ResponseBody List<CloudResource>  createResources(@PathVariable(MonitoringConstants.PLATFORM_ID) String platformId, @RequestBody List<CloudResource> resources) {
//	  logger.info("User trying to createResources platformId"+platformId);
//      //List<CloudResource> resources = gson.fromJson(new String(message.getBody()),  new TypeToken<ArrayList<CloudResource>>(){}.getType());
//
//      List<CloudResource> result = resources.stream().map(resource -> { resource.setId("symbiote"+resource.getName()); return resource;})
//      .collect(Collectors.toList());
//
//	  return result;
//  }
//  
//  @RequestMapping(method = RequestMethod.PUT, path = MonitoringConstants.DO_UPDATE_RESOURCES,  produces = "application/json", consumes = "application/json")
//  public @ResponseBody List<CloudResource>  updateResources(@PathVariable(MonitoringConstants.PLATFORM_ID) String platformId, @RequestBody List<CloudResource> resources) {
//	  logger.info("User trying to ypdateResources platformId"+platformId);
//      //List<CloudResource> resources = gson.fromJson(new String(message.getBody()),  new TypeToken<ArrayList<CloudResource>>(){}.getType());
//
//      List<CloudResource> result = resources.stream().map(resource -> { resource.setId("symbiote"+resource.getName()); return resource;})
//      .collect(Collectors.toList());
//
//	  return result;
//  }
//
//  @RequestMapping(method = RequestMethod.DELETE, path = MonitoringConstants.DO_REMOVE_RESOURCES,  produces = "application/json", consumes = "application/json")
//  public @ResponseBody List<String>  removeResources(@PathVariable(MonitoringConstants.PLATFORM_ID) String platformId, @RequestBody List<String> resources) {
//	  logger.info("User trying to ypdateResources platformId"+platformId);
//	  return resources;
//  }

  
}


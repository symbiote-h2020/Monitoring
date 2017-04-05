package eu.h2020.symbiote.service;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.h2020.symbiote.Icinga2Manager;
import eu.h2020.symbiote.beans.HostBean;
import eu.h2020.symbiote.beans.ServiceBean;
import eu.h2020.symbiote.constants.MonitoringConstants;


/*
 * @author: Elena Garrido
 * @version: 12/02/2017
 */
@RestController
@WebAppConfiguration
@RequestMapping("/testiif")
public class CRAMRestDummyServer {
  private static final Log logger = LogFactory.getLog(CRAMRestDummyServer.class);
  
  @Autowired
  private Icinga2Manager icinga2Manager;
  
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

//  @Scheduled(cron = "${symbiote.crm.publish.period}")
  @RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.PUBLISH_MONITORING_DATA,  produces = "application/json", consumes = "application/json")
//  public @ResponseBody List<ServiceBean>  publishMonitoringData(@PathVariable(MonitoringConstants.PLATFORM_ID) String platformId) {
  public @ResponseBody List<ServiceBean>  publishMonitoringData() {
//	  logger.info("Publishing monitoring data for platform " + platformId);
	  List<HostBean> hosts = (List<HostBean>) icinga2Manager.getHosts();
	  List<ServiceBean> services = null;
	  
	  for (HostBean host : hosts){
		  logger.info("Number of hosts: " + hosts.size());
		  services = (List<ServiceBean>) icinga2Manager.getServicesFromHost(host.getName());
		  logger.info("Number of services in host " + host.getName() + " : " + services.size());
		  int i = 0;
		  for (ServiceBean service : services){
			  logger.info("Publishing info about service " + service.getDisplay_name() + " from host " + host.getName());
			  //TODO take in account the mongoDB format, publish only the devices that exists in mongoDB
			  i++;			  
		  }
		  
		  //object o  --> rest endpoint
		  
	  }
	  return services;
  }

}


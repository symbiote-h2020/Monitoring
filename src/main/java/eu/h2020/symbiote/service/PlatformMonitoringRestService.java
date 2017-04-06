package eu.h2020.symbiote.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.h2020.symbiote.Icinga2Manager;
import eu.h2020.symbiote.beans.HostBean;
import eu.h2020.symbiote.beans.ServiceBean;
import eu.h2020.symbiote.icinga2.datamodel.JsonDeleteMessageIcingaResult;
import eu.h2020.symbiote.icinga2.datamodel.JsonUpdatedObjectMessageResult;
import eu.h2020.symbiote.rest.cram.CRAMMessageHandler;
import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringPlatform;

/**
 * This class implements the rest interfaces. Initially created by jose
 *
 * @author: Elena Garrido, David Rojo
 * @version: 30/01/2017
 */
@RestController
public class PlatformMonitoringRestService {
  private static final Log logger = LogFactory.getLog(PlatformMonitoringRestService.class);

//  @Autowired
//  private PlatformMonitoringManager monitoringManager;
  
  @Autowired
  private CRAMMessageHandler cramMessageHandler;
  
  @Autowired
  private Icinga2Manager icinga2Manager;

  @RequestMapping(method = RequestMethod.GET, path = "/hosts")
  public List<HostBean> getHosts() {
	  List<HostBean>result = (List<HostBean>) icinga2Manager.getHosts();
	  return result;
  }

  @RequestMapping(method = RequestMethod.GET, path = "/host/{hostname:.+}")
  @ResponseBody
  public HostBean getHost(@PathVariable("hostname") String hostname) {
	  HostBean result = icinga2Manager.getHost(hostname);
	  return result;
  }

  @RequestMapping(method = RequestMethod.GET, path = "/host/{hostname:.+}/services")
  @ResponseBody
  public List<ServiceBean> getServicesFromHost(@PathVariable("hostname") String hostname) {
	  List<ServiceBean> result = (List<ServiceBean>) icinga2Manager.getServicesFromHost(hostname);
	  return result;
  }
  
  @RequestMapping(method = RequestMethod.GET, path = "/host/{hostname:.+}/service/{service:.+}")
  @ResponseBody
  public ServiceBean getServiceFromHost(@PathVariable("hostname") String hostname, @PathVariable("service") String service) {
	  ServiceBean result = icinga2Manager.getServiceFromHost(hostname, service);
	  return result;
  }

//  @RequestMapping(method = RequestMethod.PUT, path = "/hostgroups/{hostgroup:.+}")
//  @ResponseBody
//  public HostGroupBean addHostGroup(@PathVariable("hostgroup") String hostgroup){
//	  HostGroupBean result = null;
//	  //TODO to be implemented
//	  return result;
//  }
  
  @RequestMapping(method = RequestMethod.DELETE, path = "/host/{hostname:.+}")
  @ResponseBody
  public JsonDeleteMessageIcingaResult deleteHost(@PathVariable("hostname") String hostname) {
	  JsonDeleteMessageIcingaResult result = icinga2Manager.deleteHost(hostname);
	  return result;
  }

  @RequestMapping(method = RequestMethod.DELETE, path = "/host/{hostname:.+}/service/{service:.+}")
  @ResponseBody
  public JsonDeleteMessageIcingaResult deleteServiceFromHost(@PathVariable("hostname") String hostname, @PathVariable("service") String service) {
	  JsonDeleteMessageIcingaResult result = icinga2Manager.deleteServiceFromHost(hostname, service);
	  return result;
  }
  
  @RequestMapping(method = RequestMethod.POST, path = "/host/{hostname:.+}/address/{address:.+}")
  @ResponseBody
  public JsonUpdatedObjectMessageResult updateHostAddress(@PathVariable("hostname") String hostname, @PathVariable("address") String address) {
	  JsonUpdatedObjectMessageResult result = icinga2Manager.updateHostAddress(hostname, address);
	  return result;
  }
  
//  @Scheduled(cron = "${symbiote.crm.publish.period}")
  public void publishMonitoringInfo2Crm(){
	  List<HostBean> hosts = (List<HostBean>) icinga2Manager.getHosts();

	  for (HostBean host : hosts){
		  logger.info("Number of hosts: " + hosts.size());
		  List<ServiceBean> services = (List<ServiceBean>) icinga2Manager.getServicesFromHost(host.getName());
		  ServiceBean[] message = new ServiceBean[services.size()];
		  logger.info("Number of services in host " + host.getName() + " : " + services.size());
		  int i = 0;
		  for (ServiceBean service : services){
			  message[i] = service;
			  logger.info("Publishing info about service " + message[i].getDisplay_name() + " from host " + host.getName());
			  //TODO publish to cmr only monitoring data
			  i++;			  
		  }
	  }
	  
  }
  
  @Scheduled(cron = "${symbiote.crm.publish.period}")
  public void publishMonitoringData2Cram(){
	  List<CloudMonitoringPlatform> platforms = icinga2Manager.getMonitoringInfo();
	  if (platforms != null && !platforms.isEmpty()){
		  logger.info("Publishing monitoring info to CRAM");
		  logger.info("Number of platforms in the system: " + platforms.size());
		  for (CloudMonitoringPlatform platform : platforms){
			  logger.info("Platform " + platform.getInternalId() + " has " + platform.getDevices().length + " devices");
			  for (int i = 0; i<platform.getDevices().length; i++){
				  logger.info("Device " + platform.getDevices()[i].getId());
			  }
			  //Send data to POST endpoint in CRAM
			  cramMessageHandler.doPostAlCram(platform);
		  }
	  }	 
  }

  


}

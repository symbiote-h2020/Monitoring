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
import eu.h2020.symbiote.rest.crm.CRMMessageHandler;
import  eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;

/**
 * This class implements the rest interfaces. Initially created by jose
 *
 * @author: Elena Garrido, David Rojo
 * @version: 30/01/2017
 */
@RestController
public class PlatformMonitoringRestService {
  private static final Log logger = LogFactory.getLog(PlatformMonitoringRestService.class);
  
  @Autowired
  private CRMMessageHandler crmMessageHandler;
  
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
  
  
  @Scheduled(cron = "${symbIoTe.crm.publish.period}")
  public void publishMonitoringData2Crm(){
	  CloudMonitoringPlatform platform = icinga2Manager.getMonitoringInfo();
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
			  logger.info("timestamp: " + platform.getDevices()[i].getTimestamp());		  
		  }

	  }	 
  }
  
  @RequestMapping(method = RequestMethod.GET, path = "/host/{hostname:.+}/ip")
  @ResponseBody
  public String getServiceFromHost(@PathVariable("hostname") String hostname) {
	  String result = icinga2Manager.getIpAddressByHostname(hostname);
	  return result;
  }


}

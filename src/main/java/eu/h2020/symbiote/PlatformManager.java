package eu.h2020.symbiote;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.h2020.symbiote.beans.ServiceBean;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.MonitoringRepository;
import eu.h2020.symbiote.db.ResourceRepository;
import eu.h2020.symbiote.icinga2.datamodel.ModelConverter;
import eu.h2020.symbiote.icinga2.utils.Icinga2Utils;

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
	 
	 @Autowired
	 private ResourceRepository resourceRepository;
	 
	 @Autowired
	 private MonitoringRepository monitoringRepository;
	 
	 /**
	  * Listen from Platform Host
	  */
	 @RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.SUBSCRIBE_MONITORING_DATA,  produces = "application/json", consumes = "application/json")
	 public @ResponseBody String  MonitorRestServer(@PathVariable("platformId") String platformId, @RequestBody CloudMonitoringPlatform platform) {

		  
		 	logger.info("*********************************************************");
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
			logger.info("*********************************************************");
		  logger.info("Listening monitoring data from platform " + platformId);
		  logger.info("Platform " + platform.getInternalId() + " has " + platform.getDevices().length + " devices");
		  logger.info("Platform timestamp: " + platform.getTimestamp());
		  logger.info("DEVICES:");
		  for (int i = 0; i<platform.getDevices().length; i++){
			  logger.info("Device " + platform.getDevices()[i].getId());
			  //logger.info("load: " + platform.getDevices()[i].getLoad());
			  //logger.info("availability: " + platform.getDevices()[i].getAvailability());
			  logger.info("Device timestamp: " + platform.getDevices()[i].getTimestamp());	
			  logger.info("METRICS:");
			  for (int m = 0; m<platform.getDevices()[i].getMetrics().length; m++){
				  logger.info("tag: " + platform.getDevices()[i].getMetrics()[m].getTag());				  
				  logger.info("value: " + platform.getDevices()[i].getMetrics()[m].getValue());
			  }
	
		  }
		  
		  		  // save CloudMonitoringPlatform in internalRepository
		  logger.info("Adding CloudMonitoringPlatform to database");
		  		  
		  CloudMonitoringPlatform res = addOrUpdateInInternalRepository(platform);
		  logger.info("added: " + res);
		  
		  return "received";
	  }
	 

		
	/**
	  * Add or Update CloudResource document from MongoDB.
	  */	 
	 public List<CloudResource>  addOrUpdateInInternalRepository(List<CloudResource>  resources){
		 logger.info("Adding CloudResource to database");
		 return resources.stream().map(resource -> {
			  CloudResource existingResource = resourceRepository.getByInternalId(resource.getInternalId());
		      if (existingResource != null) {
		    	  logger.info("update will be done");
		      }
		      return resourceRepository.save(resource);
		 })
	     .collect(Collectors.toList());
	  }
	 
	/**
	  * Delete CloudResource document from MongoDB.
	  */	
	  public List<CloudResource> deleteInInternalRepository(List<String> resourceIds){
		  List<CloudResource>  result = new ArrayList<CloudResource>();
		  for (String resourceId:resourceIds){
			  CloudResource existingResource = resourceRepository.getByInternalId(resourceId);
		      if (existingResource != null) {
		    	  result.add(existingResource);
		    	  resourceRepository.delete(resourceId);
		      }
		  }
		  return result;
	  }
	
	/**
	  * Get all CloudResource document from MongoDB.
	  */		  
	  public List<CloudResource> getResources() {
		  return resourceRepository.findAll();
	  }
	

	/**
	 * The getResource method retrieves \a ResourceBean identified by \a resourceId 
	 * from the mondodb database and will return it.
	 * @param resourceId from the resource to be retrieved from the database
	 * @return the ResourceBean
	 */  
	public CloudResource getResource(String resourceId) {
		if (!"".equals(resourceId)) {
			return resourceRepository.getByInternalId(resourceId);
		}
		return null;
	}
	
	
	/**
	  * Add or Update CloudMonitoringPlatform document from MongoDB.
	  */	 
	 public CloudMonitoringPlatform addOrUpdateInInternalRepository(CloudMonitoringPlatform resource){
		 	logger.info("Adding CloudMonitoringPlatform to database");
		    return monitoringRepository.save(resource);

	  }
	
	 
	 /**
	  * Get Monitoring information
	  */
	public CloudMonitoringPlatform getMonitoringInfo(){
		List<CloudResource> resources = resourceRepository.findAll();
		CloudMonitoringPlatform platform = null;
		if (resources != null){
			platform = new CloudMonitoringPlatform();
			CloudMonitoringDevice[] devices = new CloudMonitoringDevice[resources.size()];
			for (int i=0;i<resources.size();i++){
				CloudMonitoringDevice device = this.getMonitoringInfoFromDevice(resources.get(i));
				devices[i] = device;
			}
			platform.setInternalId(platformId);	
			platform.setDevices(devices);			 
		}
		return platform;
	}

	 /**
	  * Get Monitoring information from device
	  */
	private CloudMonitoringDevice getMonitoringInfoFromDevice(CloudResource resource){
		CloudMonitoringDevice monitoringDevice = null;
		
		String deviceId = resource.getInternalId();
		
		//monitoringRepository
		
/*		
		String hostname = this.getHostnameByIpAddress(resource.getCloudMonitoringHost());
		if (hostname == null || hostname.equalsIgnoreCase("")){
			 //Verify that the host is registered in Icinga2 server
			 logger.warn("The platform with ip address " + resource.getCloudMonitoringHost() + " is not registered in Monitoring environment. Icinga agent must be installed");
			 return null;
		 }
		 else {
			 	ServiceBean service = null;
				Boolean exception = false;
				String targetUrl = url + "/objects/services/";
				logger.info("URL build: " + targetUrl);
				
				try {
					icinga2client.setUrl(targetUrl);
					icinga2client.setMethod("POST");
					icinga2client.setCustomHeaders("Accept: application/json,-,X-HTTP-Method-Override: GET");
					icinga2client.setContent("{\"joins\": [\"host.name\", \"host.address\"], \"filter\": \"match(\\\"" + hostname + "\\\",host.name) && match(\\\"" + resource.getInternalId() + "\\\",service.name)\", \"attrs\": [\"display_name\",\"active\",\"check_interval\",\"check_command\",\"last_check\",\"last_check_result\"] }");
					System.out.println("BODY REQUEST: " + icinga2client.getContent());
					icinga2client.execute();
					if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
						String response = icinga2client.getContentResponse();
						logger.info("getMonitoringInfoFromDevice PAYLOAD: " + response);		
						System.out.println();
						service = ModelConverter.jsonServiceToObject(response);
						monitoringDevice = new CloudMonitoringDevice();
						monitoringDevice.setTimestamp(service.getLast_check());
						monitoringDevice.setId(resource.getInternalId());
						monitoringDevice.setAvailability(Icinga2Utils.getAvailability(service.getLast_check_result().getOutput()));
						monitoringDevice.setLoad(Icinga2Utils.getLoad(service.getLast_check_result().getOutput()));
					}
					else {
						logger.warn("Execution failed of POST method to: " + targetUrl);
						logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
								icinga2client.getStatusMessage());
						exception = true;
					}

				} catch(Exception e) {
					logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
					exception = true;
				}
				
				if(exception) return null;

		 }
	*/
		return monitoringDevice;
	}
	
	
}

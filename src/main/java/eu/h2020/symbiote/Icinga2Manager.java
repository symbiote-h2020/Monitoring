package eu.h2020.symbiote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.beans.HostBean;
import eu.h2020.symbiote.beans.HostGroupBean;
import eu.h2020.symbiote.beans.ServiceBean;
import eu.h2020.symbiote.db.ResourceRepository;
import eu.h2020.symbiote.icinga2.datamodel.JsonCreateServiceOkResult;
import eu.h2020.symbiote.icinga2.datamodel.JsonDeleteMessageIcingaResult;
import eu.h2020.symbiote.icinga2.datamodel.JsonUpdatedObjectMessageResult;
import eu.h2020.symbiote.icinga2.datamodel.ModelConverter;
import eu.h2020.symbiote.icinga2.utils.Icinga2Utils;
import eu.h2020.symbiote.rest.RestProxy;
import eu.h2020.symbiotelibraries.cloud.model.CloudResource;
import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringPlatform;

@Component
public class Icinga2Manager {

	 private static final Log logger = LogFactory.getLog(Icinga2Manager.class);
	 private RestProxy icinga2client = new RestProxy();
	 
	 //TODO define details
	 private RestProxy crmClient = new RestProxy();
	 
	 @Value("${symbiote.icinga2.api.url}")
	 private String url;

	 @Value("${symbiote.icinga2.api.user}")
	 private String user;

	 @Value("${symbiote.icinga2.api.password}")
	 private String password; 
	 
	 
	 @Autowired
	  private ResourceRepository resourceRepository;
		
	 @PostConstruct
	 private void init() {
		 icinga2client.setBasicAuthenticationUser(user);
		 icinga2client.setBasicAuthenticationPassword(password);
		 icinga2client.setEnableSSL(true);
		 icinga2client.setDisableSSLValidation(true);
	 }
	 
	 public String getURL() {
		 return url;
	 }

	 public void setURL(String url) {
		 this.url = url;
	 }
		
	 public Collection<HostBean> getHosts(){
		Collection<HostBean> collection  = null;
		Boolean exception = false;
		String targetUrl = url + "/objects/hosts?attrs=name&attrs=address&attrs=active&attrs=last_check&attrs=groups";
		logger.info("URL build: " + targetUrl);
		 
		 try {
				icinga2client.setUrl(targetUrl);
				icinga2client.setMethod("GET");
				icinga2client.execute();
				if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
					String response = icinga2client.getContentResponse();
					logger.info("PAYLOAD: " + response);		
					System.out.println();
					collection = ModelConverter.jsonHostsToObject(response);
				}
				else {
					logger.warn("Execution of GET method to: " + targetUrl + " failed: " + icinga2client.getStatusResponse());
					exception = true;
				}

			} catch(Exception e) {
				logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
				exception = true;
			}
			
			if(exception) return null;
			return collection;
	 }
	 
	 public HostBean getHost(String hostname){
			HostBean host  = null;
			Boolean exception = false;
			String targetUrl = url + "/objects/hosts?host=" + hostname + "&attrs=name&attrs=address&attrs=active&attrs=last_check&attrs=groups";
//			String targetUrl = url + "/objects/hosts";
			logger.info("URL build: " + targetUrl);
			logger.info("The hostname of the IP address 127.0.0.1 is " + this.getHostnameByIpAddress("127.0.0.1"));
			try {
				icinga2client.setUrl(targetUrl);
				icinga2client.setMethod("GET");
				icinga2client.execute();
				if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
					String response = icinga2client.getContentResponse();
					logger.info("PAYLOAD: " + response);		
					System.out.println();
					host = ModelConverter.jsonHostToObject(response);
				}
				else {
					logger.warn("Execution failed of GET method to: " + targetUrl);
					logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
							icinga2client.getStatusMessage());
					exception = true;
				}

			} catch(Exception e) {
				logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
				exception = true;
			}
			
			if(exception) return null;
			return host;
	 }
	 
	 
	 public Collection<ServiceBean> getServicesFromHost(String hostname){
		 Collection<ServiceBean> collection  = null;
			Boolean exception = false;
			String targetUrl = url + "/objects/services";
			logger.info("URL build: " + targetUrl);
			
			try {
				icinga2client.setUrl(targetUrl);
				icinga2client.setMethod("POST");
				icinga2client.setCustomHeaders("Accept: application/json,-,X-HTTP-Method-Override: GET");
				icinga2client.setContent("{\"joins\": [\"host.name\", \"host.address\"], \"filter\": \"match(\\\"" + hostname + "\\\",host.name)\", \"attrs\": [\"display_name\",\"active\",\"check_interval\",\"check_command\",\"last_check\",\"last_check_result\"] }");
				System.out.println("BODY REQUEST: " + icinga2client.getContent());
				icinga2client.execute();
				if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
					String response = icinga2client.getContentResponse();
					logger.info("PAYLOAD: " + response);		
					System.out.println();
					collection = ModelConverter.jsonServicesToObject(response);
				}
				else {
					logger.warn("Execution failed of GET method to: " + targetUrl);
					logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
							icinga2client.getStatusMessage());
					exception = true;
				}

			} catch(Exception e) {
				logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
				exception = true;
			}
			
			if(exception) return null;
			return collection;
	 }
	 
	 
	 public ServiceBean getServiceFromHost(String hostname, String servicename){
		 ServiceBean service  = null;
			Boolean exception = false;
			String targetUrl = url + "/objects/services/";
//			String targetUrl = url + "/objects/hosts";
			logger.info("URL build: " + targetUrl);
			
			try {
				icinga2client.setUrl(targetUrl);
				icinga2client.setMethod("POST");
				icinga2client.setCustomHeaders("Accept: application/json,-,X-HTTP-Method-Override: GET");
				icinga2client.setContent("{\"joins\": [\"host.name\", \"host.address\"], \"filter\": \"match(\\\"" + hostname + "\\\",host.name) && match(\\\"" + servicename + "\\\",service.name)\", \"attrs\": [\"display_name\",\"active\",\"check_interval\",\"check_command\",\"last_check\",\"last_check_result\"] }");
				icinga2client.execute();
				if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
					String response = icinga2client.getContentResponse();
					logger.info("PAYLOAD: " + response);		
					System.out.println();
					service = ModelConverter.jsonServiceToObject(response);
				}
				else {
					logger.warn("Execution failed of GET method to: " + targetUrl);
					logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
							icinga2client.getStatusMessage());
					exception = true;
				}

			} catch(Exception e) {
				logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
				exception = true;
			}
			
			if(exception) return null;
			return service;
	 }
	 
	 
	 private HostGroupBean getHostGroup(String hostgroupname){
		 //TODO pending to revise
		 HostGroupBean hostgroup  = null;
			Boolean exception = false;
			String targetUrl = url + "/objects/services/";
//			String targetUrl = url + "/objects/hosts";
			logger.info("URL build: " + targetUrl);
			
			try {
				icinga2client.setUrl(targetUrl);
				icinga2client.setMethod("POST");
				icinga2client.setCustomHeaders("Accept: application/json,-,X-HTTP-Method-Override: GET");
//				icinga2client.setContent("{\"joins\": [\"host.name\", \"host.address\"], \"filter\": \"match(\\\"" + hostname + "\\\",host.name) && match(\\\"" + servicename + "\\\",service.name)\", \"attrs\": [\"display_name\",\"active\",\"check_interval\",\"check_command\",\"last_check\",\"last_check_result\"] }");
				icinga2client.execute();
				if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
					String response = icinga2client.getContentResponse();
					logger.info("PAYLOAD: " + response);		
					System.out.println();
//					hostgroup = ModelConverter.jsonServiceToObject(response);
				}
				else {
					logger.warn("Execution failed of GET method to: " + targetUrl);
					logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
							icinga2client.getStatusMessage());
					exception = true;
				}

			} catch(Exception e) {
				logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
				exception = true;
			}
			
			if(exception) return null;
			return hostgroup;
	 }
	 
	 
	 public JsonDeleteMessageIcingaResult deleteHost(String hostname){
		 JsonDeleteMessageIcingaResult jsonMessage  = null;
			Boolean exception = false;
			String targetUrl = url + "/objects/hosts/" + hostname + "?cascade=1";
			logger.info("URL build: " + targetUrl);
			
			try {
				icinga2client.setUrl(targetUrl);
				icinga2client.setMethod("POST");
				icinga2client.setCustomHeaders("Accept: application/json,-,X-HTTP-Method-Override: DELETE");
				icinga2client.execute();
				if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
					String response = icinga2client.getContentResponse();
					logger.info("PAYLOAD: " + response);		
					System.out.println();
					jsonMessage = ModelConverter.jsonDeleteMessageToObject(response);
				}
				else {
					logger.warn("Execution failed of GET method to: " + targetUrl);
					logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
							icinga2client.getStatusMessage());
					exception = true;
				}

			} catch(Exception e) {
				logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
				exception = true;
			}
			
			if(exception) return null;
			return jsonMessage;
	 }
	 
	 
	 public JsonDeleteMessageIcingaResult deleteServiceFromHost(String hostname, String servicename){
		 JsonDeleteMessageIcingaResult jsonMessage  = null;
		 Boolean exception = false;
		 String targetUrl = url + "/objects/services/" + hostname + "!" + servicename;
		 logger.info("URL build: " + targetUrl);

		 try {
			 icinga2client.setUrl(targetUrl);
			 icinga2client.setMethod("POST");
			 icinga2client.setCustomHeaders("Accept: application/json,-,X-HTTP-Method-Override: DELETE");
			 icinga2client.execute();
			 if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
				 String response = icinga2client.getContentResponse();
				 logger.info("PAYLOAD: " + response);		
				 System.out.println();
				 jsonMessage = ModelConverter.jsonDeleteMessageToObject(response);
			 }
			 else {
				 logger.warn("Execution failed of GET method to: " + targetUrl);
				 logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
						 icinga2client.getStatusMessage());
				 exception = true;
			 }

		 } catch(Exception e) {
			 logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
			 exception = true;
		 }

		 if(exception) return null;
		 return jsonMessage;
	 }

	public JsonUpdatedObjectMessageResult updateHostAddress(String hostname, String address) {
		
		JsonUpdatedObjectMessageResult jsonMessage  = null;
		Boolean exception = false;
		String targetUrl = url + "/objects/hosts/" + hostname;
		logger.info("URL build: " + targetUrl);

		try {
			icinga2client.setUrl(targetUrl);
			icinga2client.setMethod("POST");
			icinga2client.setCustomHeaders("Accept: application/json");
			icinga2client.setContent("{\"attrs\": {\"address\" : \"" + address + "\"} }");
			icinga2client.execute();
			if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
				String response = icinga2client.getContentResponse();
				logger.info("PAYLOAD: " + response);		
				System.out.println();
				jsonMessage = ModelConverter.jsonUpdateMessageToObject(response);
			}
			else {
				logger.warn("Execution failed of GET method to: " + targetUrl);
				logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
						icinga2client.getStatusMessage());
				exception = true;
			}

		} catch(Exception e) {
			logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}

		if(exception) return null;
		return jsonMessage;
	}
	 	 
	 private List<CloudResource>  addOrUpdateInInternalRepository(List<CloudResource>  resources){
		 return resources.stream().map(resource -> {
			  CloudResource existingResource = resourceRepository.getByInternalId(resource.getInternalId());
		      if (existingResource != null) {
		    	  logger.info("update will be done");
		      }
		      return resourceRepository.save(resource);
		 })
	     .collect(Collectors.toList());
	  }

	  private List<CloudResource> deleteInInternalRepository(List<String> resourceIds){
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
	  
	public List<CloudResource> getResources() {
		return resourceRepository.findAll();
	}
	

	//! Get a resource.
	/*!
	 * The getResource method retrieves \a ResourceBean identified by \a resourceId 
	 * from the mondodb database and will return it.
	 *
	 * \param resourceId id from the resource to be retrieved from the database
	 * \return \a getResource returns the \a ResourceBean, 
	 */
	public CloudResource getResource(String resourceId) {
		if (!"".equals(resourceId)) {
			return resourceRepository.getByInternalId(resourceId);
		}
		return null;
	}
	
	 private String getHostnameByIpAddress(String ipAddress){
		 String hostname = "";
		 
		 Boolean exception = false;
			String targetUrl = url + "/objects/hosts";
			logger.info("URL build: " + targetUrl);
			
			try {
				icinga2client.setUrl(targetUrl);
				icinga2client.setMethod("POST");
				icinga2client.setCustomHeaders("Accept: application/json,-,X-HTTP-Method-Override: GET");
				icinga2client.setContent("{\"joins\": [\"host.name\", \"host.address\"], \"filter\": \"match(\\\"" + ipAddress + "\\\",host.address)\", \"attrs\": [\"display_name\"] }");
				System.out.println("BODY REQUEST: " + icinga2client.getContent());
				icinga2client.execute();
				if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
					String response = icinga2client.getContentResponse();
					logger.info("PAYLOAD: " + response);		
					System.out.println();
					hostname = ModelConverter.jsonHostByIpToString(response);
				}
				else {
					logger.warn("Execution failed of GET method to: " + targetUrl);
					logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
							icinga2client.getStatusMessage());
					exception = true;
				}

			} catch(Exception e) {
				logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
				exception = true;
			}
			
			if(exception) return null;
		 
		 
		 return hostname;
	 }

	public List<CloudResource> addResources(List<CloudResource> resources) {
		logger.info("Adding devices to database");
		List<CloudResource> result  = addOrUpdateInInternalRepository(resources);
		logger.info("Adding " + resources.size() + " devices to icinga2");
		List<CloudResource> resourcesAdded = this.createServices(resources);
		logger.info("Added " + resourcesAdded.size() + " devices to icinga2");
	    //rapresourceRegistrationMessageHandler.sendResourcesRegistrationMessage(result);
	    return result;
	}
	
	private List<CloudResource> createServices(List<CloudResource> resources){
		List <CloudResource> result = new ArrayList<CloudResource>();
		for (CloudResource resource : resources){
			JsonCreateServiceOkResult response = createService(resource);
			if (response != null && response.getCode() == 200.0){
				//if the resource is successfully added in Icinga, it is added in the response
				//if any problem exists in the creation of the resource, the resource will not be part of the response
				result.add(resource);
			}
		}
		return result;
	}
	
	private JsonCreateServiceOkResult createService(CloudResource resource){
		 JsonCreateServiceOkResult okResponse  = null;
		 String hostname = this.getHostnameByIpAddress(resource.getHost());
		 if (hostname == null || hostname.equalsIgnoreCase("")){
			 //Verify that the host is registered in Icinga2 server
			 logger.warn("The platform with ip address " + resource.getHost() + " is not registered in Monitoring environment. Icinga agent must be installed");
			 return null;
		 }
		 else {
			 //Verify that no other service with this name exists
			 ServiceBean service = this.getServiceFromHost(hostname, resource.getName());
			if (service != null){
				logger.warn("There is a device registered in platform " + resource.getInternalId() + " with this name. Please select another name");
				return null;
			}
			else {
				Boolean exception = false;
				String targetUrl = url + "/objects/services/" + hostname + "!" + resource.getName();
				logger.info("URL build: " + targetUrl);
				try {
					icinga2client.setUrl(targetUrl);
					icinga2client.setMethod("PUT");
					icinga2client.setCustomHeaders("Accept: application/json");
					//{ "templates": [ "generic-service" ], "attrs": { "display_name": "check_iot", "check_command" : "checkIot", "vars.IOT_SYMBIOTEID": "symbioteID_1", "vars.IOT_DEVICE_NAME": "device_name1", "vars.IOT_IPADDRESS": "X.X.X.X", "host_name": "api_dummy_host_2" } }'
					icinga2client.setContent("{ \"templates\": [ \"generic-service\" ], \"attrs\": "
							+ "{ \"display_name\": \"" + resource.getName() + "\","
							+ " \"check_command\" : \"checkIot\","
							+ " \"vars.IOT_SYMBIOTEID\": \"" + resource.getParams().getSymbiote_id() + "\","
							+ " \"vars.IOT_DEVICE_NAME\": \"" + resource.getParams().getDevice_name() + "\","
							+ " \"vars.IOT_IPADDRESS\": \"" + resource.getParams().getIp_address() +"\","
							+ " \"host_name\": \"" + hostname + "\" } }");
					System.out.println("BODY REQUEST: " + icinga2client.getContent());
					icinga2client.execute();
					if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
						String response = icinga2client.getContentResponse();
						logger.info("PAYLOAD: " + response);		
						System.out.println();
						okResponse = ModelConverter.jsonServicesOkToObject(response);
					}
					else {
						logger.warn("Execution failed of GET method to: " + targetUrl);
						logger.warn("HTTP STATUS: " + icinga2client.getStatusResponse() + " - " + 
								icinga2client.getStatusMessage());
						exception = true;
					}

				} catch(Exception e) {
					logger.warn("Error trying to parse JSON response from Icinga2: " + targetUrl + " Exception: " + e.getMessage());
					exception = true;
				}
				if(exception) return null;
				return okResponse;
			}
			
		 }		
	}
	
	
	public List<CloudMonitoringPlatform> getMonitoringInfo(){
		List<CloudResource> resources = resourceRepository.findAll();
		List<CloudMonitoringPlatform> result = null;
		if (resources != null){
			Map<String, ArrayList<CloudMonitoringDevice>> map = new HashMap<String, ArrayList<CloudMonitoringDevice>>();
			for (CloudResource resource : resources){
				CloudMonitoringDevice device = this.getMonitoringInfoFromDevice(resource);
				if (map.isEmpty() || map.get(resource.getInternalId()) == null){
					ArrayList<CloudMonitoringDevice> devices = new ArrayList<>();
					devices.add(device);
					map.put(resource.getInternalId(), devices);
				}
				else {
					//the platform exists in the map, add the new device
					map.get(resource.getInternalId()).add(device);
				}
			}

			//Convert the map to list of cloudMonitoringPlatform object to return it
			if (!map.isEmpty()){
				result = new ArrayList<CloudMonitoringPlatform>();
				Iterator it = map.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry)it.next();
					CloudMonitoringPlatform platform = new CloudMonitoringPlatform();
					platform.setInternalId((String) pair.getKey());
					List<CloudMonitoringDevice> list = (List<CloudMonitoringDevice>)pair.getValue();
					CloudMonitoringDevice[] devicesArray = new CloudMonitoringDevice[list.size()];
					for (int i=0;i<devicesArray.length;i++){
						devicesArray[i] = list.get(i);
					}
					platform.setDevices(devicesArray);
					it.remove(); // avoids a ConcurrentModificationException
					result.add(platform);
				}	
			}
			 
		}
		return result;
	}
	
	
	private CloudMonitoringDevice getMonitoringInfoFromDevice(CloudResource resource){
		CloudMonitoringDevice monitoringDevice = null;
		
		String hostname = this.getHostnameByIpAddress(resource.getHost());
		if (hostname == null || hostname.equalsIgnoreCase("")){
			 //Verify that the host is registered in Icinga2 server
			 logger.warn("The platform with ip address " + resource.getHost() + " is not registered in Monitoring environment. Icinga agent must be installed");
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
					icinga2client.setContent("{\"joins\": [\"host.name\", \"host.address\"], \"filter\": \"match(\\\"" + hostname + "\\\",host.name) && match(\\\"" + resource.getName() + "\\\",service.name)\", \"attrs\": [\"display_name\",\"active\",\"check_interval\",\"check_command\",\"last_check\",\"last_check_result\"] }");
					System.out.println("BODY REQUEST: " + icinga2client.getContent());
					icinga2client.execute();
					if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
						String response = icinga2client.getContentResponse();
						logger.info("PAYLOAD: " + response);		
						System.out.println();
						service = ModelConverter.jsonServiceToObject(response);
						monitoringDevice = new CloudMonitoringDevice();
						monitoringDevice.setTimestamp(service.getLast_check());
						monitoringDevice.setId(resource.getId());
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
				return monitoringDevice;
		 }
				
	}
	
	
		 

}

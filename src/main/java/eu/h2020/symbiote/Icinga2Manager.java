package eu.h2020.symbiote;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.beans.CheckCommandBean;
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
import eu.h2020.symbiotelibraries.cloud.model.current.CloudResource;
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
	 
	 @Value("${platform.id}")
	 private String platformId; 
	 
	 
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
				//icinga2client.
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
		 String targetUrl = url + "/objects/services/" + hostname + "!" + servicename + "?cascade=1";
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
		
		logger.info("Adding " + resources.size() + " devices to icinga2");
		List<CloudResource> resourcesAdded = this.createServices(resources);
		logger.info("Added " + resourcesAdded.size() + " devices to icinga2");
		//add to database only the devices created in icinga2
		logger.info("Adding devices to database");
		List<CloudResource> result  = addOrUpdateInInternalRepository(resourcesAdded);
				
		//List<CloudResource> result  = addOrUpdateInInternalRepository(resources);
		
		
	    return result;
	}
	
	public List<CloudResource> deleteResources(List<String> resources) {
		logger.info("Deleting " + resources.size() + " devices to icinga2");
		
		Iterator<String> it = resources.listIterator();
		List<String>  listIdDeleted = new ArrayList<String>();
		try {
			while( it.hasNext() ) {
			String internalId = (String) it.next();
			CloudResource c = this.resourceRepository.getByInternalId(internalId);
			String host = this.getHostnameByIpAddress(c.getHost());
			String service  = c.getInternalId();
			JsonDeleteMessageIcingaResult res = this.deleteServiceFromHost(host, service);
			if( res.getCode() == 200.0 ) 
			{
				logger.info("Deleted service:" + service  +" from host:"+host + "to icinga2");
				listIdDeleted.add(service);
				System.out.println("****************************listIdDeleted:"+listIdDeleted);
			
			}
			else
			{
				logger.info("Cant Deleted service:" + service  +" from host:"+host + "to icinga2");
				logger.info("Code:" + res.getCode());
				logger.info("Status:" + res.getStatus());
			}
			System.out.println("****************************listIdDeleted:"+listIdDeleted);
		}
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
		logger.info("Deleting devices to database: "+listIdDeleted);
		List<CloudResource> result  = deleteInInternalRepository(listIdDeleted);		
		
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
			 ServiceBean service = this.getServiceFromHost(hostname, resource.getInternalId());
			if (service != null){
				logger.warn("There is a device registered in platform " + resource.getInternalId() + " with this name. Please select another name");
				return null;
			}
			else {
				//If the checkCommand do not exists, is created before the creation of the service
				if (!this.existCheckCommandForHost(hostname)){
					createCheckCommand(hostname);
				}
				Boolean exception = false;
				String targetUrl = url + "/objects/services/" + hostname + "!" + resource.getInternalId();
				logger.info("URL build: " + targetUrl);
				try {
					icinga2client.setUrl(targetUrl);
					icinga2client.setMethod("PUT");
					icinga2client.setCustomHeaders("Accept: application/json");
					//{ "templates": [ "generic-service" ], "attrs": { "display_name": "check_iot", "check_command" : "checkIot", "vars.IOT_SYMBIOTEID": "symbioteID_1", "vars.IOT_DEVICE_NAME": "device_name1", "vars.IOT_IPADDRESS": "X.X.X.X", "host_name": "api_dummy_host_2" } }'
					icinga2client.setContent("{ \"templates\": [ \"generic-service\" ], \"attrs\": "
							+ "{ \"display_name\": \"" + resource.getInternalId() + "\","
							+ " \"check_command\" : \"checkIot_" + hostname + "\","
							+ " \"vars.IOT_INTERNAL_ID\": \"" + resource.getParams().getInternalId() + "\","
							+ " \"vars.IOT_DEVICE_NAME\": \"" + resource.getParams().getDevice_name() + "\","
							+ " \"vars.IOT_IPADDRESS\": \"" + resource.getParams().getIp_address() +"\","
							+ " \"command_endpoint\": \"" + hostname + "\" } }");
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
					icinga2client.setContent("{\"joins\": [\"host.name\", \"host.address\"], \"filter\": \"match(\\\"" + hostname + "\\\",host.name) && match(\\\"" + resource.getInternalId() + "\\\",service.name)\", \"attrs\": [\"display_name\",\"active\",\"check_interval\",\"check_command\",\"last_check\",\"last_check_result\"] }");
					System.out.println("BODY REQUEST: " + icinga2client.getContent());
					icinga2client.execute();
					if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
						String response = icinga2client.getContentResponse();
						logger.info("PAYLOAD: " + response);		
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
				return monitoringDevice;
		 }
	}
	
	
	
	 private CheckCommandBean getCheckCommand(String checkCommandName){
		 CheckCommandBean checkCmd  = null;
			Boolean exception = false;
			String targetUrl = url + "/objects/checkcommands/" + checkCommandName;
			logger.info("URL build: " + targetUrl);
			try {
				icinga2client.setUrl(targetUrl);
				icinga2client.setMethod("GET");
				icinga2client.execute();
				if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
					String response = icinga2client.getContentResponse();
					logger.info("PAYLOAD: " + response);		
					System.out.println();
					checkCmd = ModelConverter.jsonCheckCommandToObject(response);
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
			return checkCmd;
	 }
	
	
	 private boolean existCheckCommandForHost(String hostname){
		 CheckCommandBean checkCmd = getCheckCommand("checkIot_" + hostname);
		 if (checkCmd == null){
			 return false;
		 }
		 return true;
	 }
	 
	 
	 private JsonCreateServiceOkResult createCheckCommand(String hostname){
		 JsonCreateServiceOkResult okResponse  = null;
		//Verify that no other checkCommand exists
			if (this.existCheckCommandForHost(hostname)){
				logger.warn("Symbiote checkCommand exits on hostname " + hostname);
				return null;
			}
			else {
				Boolean exception = false;
				String targetUrl = url + "/objects/checkcommands/checkIot_" + hostname;
				logger.info("URL build: " + targetUrl);
				try {
					icinga2client.setUrl(targetUrl);
					icinga2client.setMethod("PUT");
					icinga2client.setCustomHeaders("Accept: application/json");
					// { "templates": [ "plugin-check-command" ], "attrs": { "command": [ "/usr/lib/nagios/plugins/check_symbiote_iot.sh"], "zone": "zabbix.atos.net", "arguments": { "-s": "$IOT_INTERNALID$", "-d": "$IOT_DEVICE_NAME$", "-i": "$IOT_IPADDRESS$" } } }
					icinga2client.setContent(
							"{ \"templates\": [ \"plugin-check-command\" ], \"attrs\": { "
							+ "\"command\": [ \"/usr/lib/nagios/plugins/check_symbiote_iot.sh\"], "
							+ "\"zone\": \"" + hostname + "\", "
							+ "\"arguments\": { \"-s\": \"$IOT_INTERNALID$\", \"-d\": \"$IOT_DEVICE_NAME$\", \"-i\": \"$IOT_IPADDRESS$\" } } }");
					System.out.println("BODY REQUEST: " + icinga2client.getContent());
					icinga2client.execute();
					if (icinga2client.getStatusResponse() == HttpStatus.SC_OK){
						String response = icinga2client.getContentResponse();
						logger.info("PAYLOAD: " + response);		
						System.out.println();
						//the OK response is the same for CheckCommand class, so I use the same code
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

package eu.h2020.symbiote;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.beans.HostBean;
import eu.h2020.symbiote.beans.HostGroupBean;
import eu.h2020.symbiote.beans.ServiceBean;
import eu.h2020.symbiote.icinga2.datamodel.JsonDeleteMessageIcingaResult;
import eu.h2020.symbiote.icinga2.datamodel.JsonUpdatedObjectMessageResult;
import eu.h2020.symbiote.icinga2.datamodel.ModelConverter;
import eu.h2020.symbiote.rest.RestProxy;

@Component
public class Icinga2Manager {

	 private static final Log logger = LogFactory.getLog(Icinga2Manager.class);
	 private RestProxy icinga2client = new RestProxy();
	 
	 @Value("${symbiote.icinga2.api.url}")
	 private String url;

	 @Value("${symbiote.icinga2.api.user}")
	 private String user;

	 @Value("${symbiote.icinga2.api.password}")
	 private String password; 
	 
		
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
		// TODO Auto-generated method stub
		
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
	 	 
}

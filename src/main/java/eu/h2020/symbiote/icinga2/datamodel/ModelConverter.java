package eu.h2020.symbiote.icinga2.datamodel;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.h2020.symbiote.beans.CheckCommandBean;
import eu.h2020.symbiote.beans.HostBean;
import eu.h2020.symbiote.beans.ServiceBean;
import eu.h2020.symbiote.icinga2.datamodel.host.ip.JsonHostByIpResponse;
import eu.h2020.symbiote.icinga2.datamodel.host.ip.JsonIpByHostResponse;

public class ModelConverter {
	 private static final Log logger = LogFactory.getLog(ModelConverter.class);
	 private static Gson gson = new GsonBuilder().create();
	 
	 /**
		 * Converts a Collection object to its String XML representation
		 * @param collection object to be converted
		 * @return XML representation
		 */
		public static String objectCollectionToXML(Collection collection) {	
			return toXML(Collection.class, collection);
		}
		
		/**
		 * Converts a Collection object to its String JSON representation
		 * @param collection object to be converted
		 * @return JSON representation
		 */
		public static String objectCollectionToJSON(Collection collection) {	
			return toJSON(Collection.class, collection);
		}
		
		/**
		 * Converts an XML to a Collection object
		 * @param xml Representation of an Collection of Applications
		 * @return the Collection object or null if the xml is mal-formatted
		 */
		public static Collection xmlCollectionToObject(String xml) {
			return toObject(Collection.class, xml);
		}
		
		/**
		 * Converts an JSON to a Collection object
		 * @param json Representation of an Collection of Applications
		 * @return the Collection object or null if the json is mal-formatted
		 */
		public static Collection jsonCollectionToObject(String xml) {
			return fromJSONToObject(Collection.class, xml);
		}
		
		private static <T> String toXML(Class<T> clazz, T t) {
		    try {
		        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		        Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				marshaller.marshal(t, out);
				String output = out.toString();
				logger.debug("Converting object to XML: ");
				logger.debug(output);
				
				return output;
			} catch(Exception exception) {
				logger.info("Error converting object to XML: " + exception.getMessage());
				return null;
			}      
		}
		
		private static <T> String toJSON(Class<T> clazz, T t) {
		    try {
				JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {clazz}, null);
				Marshaller marshaller = jc.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
				marshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				marshaller.marshal(t, out);
				String output = out.toString();
				logger.debug("Converting object to JSON: ");
				logger.debug(output);
				
				return output;
			} catch(Exception exception) {
				logger.info("Error converting object to XML: " + exception.getMessage());
				return null;
			}      
		}
		
		private static <T> T toObject(Class<T> clazz, String xml) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Object obj = jaxbUnmarshaller.unmarshal(new StringReader(xml));
				
				return clazz.cast(obj);
			} catch(Exception exception) {
				logger.info("Error parsing XML: " + exception.getMessage());
				return null;
			}    
		}
		
		private static <T> T fromJSONToObject(Class<T> clazz, String json) {
			try {
		        // Create a JaxBContext
				JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {clazz}, null);
		        
		        // Create the Unmarshaller Object using the JaxB Context
		        Unmarshaller unmarshaller = jc.createUnmarshaller();
		        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
		        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
		        
		        StreamSource jsonSource = new StreamSource(new StringReader(json));  
		        T object = unmarshaller.unmarshal(jsonSource, clazz).getValue();
				
				return object;
			} catch(Exception exception) {
				logger.info("Error parsing JSON: " + exception.getMessage());
				return null;
			}    
		}

		public static HostBean jsonHostToObject(String jsonString) {
			JsonHostsResponse host =  gson.fromJson(jsonString, JsonHostsResponse.class);
			if (host.getResults().length == 1){
				return host.getResults()[0].getAttrs();
			}
			return null;
		}
		
		public static Collection<HostBean> jsonHostsToObject(String jsonString){
			JsonHostsResponse hosts =  gson.fromJson(jsonString, JsonHostsResponse.class);
			List<HostBean> hostsList = new ArrayList<>();
			for (int i = 0; i<hosts.getResults().length;i++){
				hostsList.add(hosts.getResults()[i].getAttrs());
			}
			return hostsList;
		}
		
		public static Collection<ServiceBean> jsonServicesToObject(String jsonString){
			JsonServicesResponse services =  gson.fromJson(jsonString, JsonServicesResponse.class);
			List<ServiceBean> servicesList = new ArrayList<>();
			for (int i = 0; i<services.getResults().length;i++){
				servicesList.add(services.getResults()[i].getAttrs());
			}
			return servicesList;
		}
		
		public static ServiceBean jsonServiceToObject(String jsonString) {
			JsonServicesResponse services =  gson.fromJson(jsonString, JsonServicesResponse.class);
			if (services.getResults().length == 1){
				return services.getResults()[0].getAttrs();
			}
			return null;
		}
		
		public static JsonDeleteMessageIcingaResult jsonDeleteMessageToObject(String jsonString){
			JsonDeleteErrorIcingaResponse error = null;
			JsonDeleteMessageIcingaResponse message = null;
			boolean isDeleteOk = true;
			try {
				error = gson.fromJson(jsonString, JsonDeleteErrorIcingaResponse.class);
				if (error.getResults()[0].getErrors() != null){
					//the object has value in errors attribute
					isDeleteOk = false;
				}
			}
			catch (Exception e){
				logger.error("Error converting object to JSON: " + e.getMessage());
				return null;
			}
			
			if (isDeleteOk){
				message = gson.fromJson(jsonString, JsonDeleteMessageIcingaResponse.class);
				return message.getResults()[0];
			}
			else {
				return error.getResults()[0];
			}
			
			
		}

		public static JsonUpdatedObjectMessageResult jsonUpdateMessageToObject(String jsonString) {
			JsonUpdatedObjectMessageResponse response =  gson.fromJson(jsonString, JsonUpdatedObjectMessageResponse.class);
			if (response.getResults().length == 1){
				return response.getResults()[0];
			}
			return null;
		}
		
		public static String jsonHostByIpToString(String jsonString){
			JsonHostByIpResponse hostByIp =  gson.fromJson(jsonString, JsonHostByIpResponse.class);
			String hostname = "";
			try {
				if (hostByIp.getResults() != null && hostByIp.getResults().length ==1){
					return hostByIp.getResults()[0].getAttrs().getDisplay_name();
				}
			}
			catch (Exception e){
				return "";
			}
			return hostname;
		}

		public static JsonCreateServiceOkResult jsonServicesOkToObject(String jsonString) {
			JsonCreateServiceOkResponse response =  gson.fromJson(jsonString, JsonCreateServiceOkResponse.class);
			return response.getResults()[0];
		}

		public static CheckCommandBean jsonCheckCommandToObject(String jsonString) {
			JsonCheckCommandsResponse checkCommands =  gson.fromJson(jsonString, JsonCheckCommandsResponse.class);
			if (checkCommands.getResults().length == 1){
				return checkCommands.getResults()[0].getAttrs();
			}
			return null;
		}
		
		public static String jsonIpByHostToString(String jsonString){
			JsonIpByHostResponse hostByIp =  gson.fromJson(jsonString, JsonIpByHostResponse.class);
			String ipAddress = "";
			try {
				if (hostByIp.getResults() != null && hostByIp.getResults().length ==1){
					return hostByIp.getResults()[0].getAttrs().getAddress();
				}
			}
			catch (Exception e){
				return "";
			}
			return ipAddress;
		}
		

}

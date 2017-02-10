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

import eu.h2020.symbiote.beans.HostBean;

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

}

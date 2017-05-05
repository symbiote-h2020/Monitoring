package eu.h2020.symbiote.rest.crm;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import feign.Feign;
import feign.FeignException;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
@Component
//@SpringBootTest( webEnvironment = WebEnvironment.DEFINED_PORT, properties = {"symbiote.crm.url=http://localhost:8080"})
public  class CRMMessageHandler {
	
	private static final Log logger = LogFactory.getLog(CRMMessageHandler.class);

	private CRMRestService jsonclient;

	@Value("${symbiote.crm.url}")
	private String url;

    
	public void setService(CRMRestService service){
		jsonclient = service;
	}
	
    @PostConstruct
	public void createClient() {
		logger.info("Will use "+ url +" to access to CRM");
		jsonclient = Feign.builder().decoder(new JacksonDecoder()).encoder(new JacksonEncoder()).target(CRMRestService.class, url);
	}
		
	 
    /**
	  * Send Monitoring information to CRM
	  * @author: David Rojo, Fernando Campos
	  * @version: 25/04/2017
	  */
    public String doPost2Crm(CloudMonitoringPlatform platform)  {
		String result = "not send";
    	try{
			logger.info("Monitoring trying to publish data for platform "+ platform.getInternalId() + " containing " + 
					platform.getDevices().length + " devices");
			result = jsonclient.doPost2Crm(platform.getInternalId(), platform);
    	}catch(FeignException t)
    	{
    		logger.error("Error accessing to CRM server at " + url);
    		//logger.error("LocalizedMessage: " + t.getLocalizedMessage());
    		logger.error("Message: " + t.getMessage());
    	}catch(Throwable t){
			logger.error("Error accessing to CRM server at " + url, t);
		}
    	return result;
	}

}


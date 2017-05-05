package eu.h2020.symbiote.rest.crm;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.security.SecurityHandler;
import eu.h2020.symbiote.security.exceptions.sh.SecurityHandlerDisabledException;
import eu.h2020.symbiote.security.token.Token;

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

	 @Value("${symbiote.sh.password}")
	 private String secHandlerPsw;
	 
	 @Value("${symbiote.sh.user}")
	 private String secHandlerUser;
	 
	 @Value("${symbiote.rabbitmq.host.ip}")
	 private String rabbitMQHostIP;
	 
	 @Value("${symbiote.coreaam.url}")
	 private String coreAAMUrl;
	 
	 private SecurityHandler securityHandler;
    
	public void setService(CRMRestService service){
		jsonclient = service;
	}
	
    @PostConstruct
	public void createClient() {
		logger.info("Will use "+ url +" to access to CRM");
		jsonclient = Feign.builder().decoder(new JacksonDecoder()).encoder(new JacksonEncoder()).target(CRMRestService.class, url);
		securityHandler = new SecurityHandler(coreAAMUrl, rabbitMQHostIP, false);
	}
		
	 
    /**
	  * Send Monitoring information to CRM
	  */
    public String doPost2Crm(CloudMonitoringPlatform platform)  {
		String result = "not send";
    	try{
			logger.info("Monitoring trying to publish data for platform "+ platform.getInternalId() + " containing " + 
					platform.getDevices().length + " devices");
			Token token = securityHandler.requestCoreToken(secHandlerUser, secHandlerPsw);
			result = jsonclient.doPost2Crm(platform.getInternalId(), platform, token.getToken());
    	}
    	catch (SecurityHandlerDisabledException e){
    		logger.error("Message: " + e.getMessage());
    	}
    	catch(FeignException t) {
    		logger.error("Error accessing to CRM server at " + url);
    		//logger.error("LocalizedMessage: " + t.getLocalizedMessage());
    		logger.error("Message: " + t.getMessage());
    	}
    	catch(Throwable t){
			logger.error("Error accessing to CRM server at " + url, t);
		}
    	return result;
	}

}


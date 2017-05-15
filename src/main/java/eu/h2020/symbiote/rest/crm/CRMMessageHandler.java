package eu.h2020.symbiote.rest.crm;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.security.SecurityHandler;
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

	@Value("${security.user}")
	private String secHandlerUser;

	@Value("${security.password}")
	private String secHandlerPsw;
	 
    @Value("${rabbit.host}")
    private String rabbitMQHostIP;

    @Value("${rabbit.username}")
    private String rabbitMQUsername;  

    @Value("${rabbit.password}")
    private String rabbitMQPassword;
	 
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
		securityHandler = new SecurityHandler(coreAAMUrl, rabbitMQHostIP, rabbitMQUsername, rabbitMQPassword);
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
    
    /**
     * METHOD FOR TESTING PURPOSES, the method receive dummy token and send the monitoring information
     * using this dummy token
     * 
     * @param platform
     * @return
     */
    public String doPost2CrmTest(CloudMonitoringPlatform platform, String token)  {
		String result = "not send";
    	try{
			logger.info("Monitoring trying to publish data for platform "+ platform.getInternalId() + " containing " + 
					platform.getDevices().length + " devices");
			result = jsonclient.doPost2Crm(platform.getInternalId(), platform, token);
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


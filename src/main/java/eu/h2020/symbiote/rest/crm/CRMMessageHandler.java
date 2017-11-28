package eu.h2020.symbiote.rest.crm;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.SymbioteAuthorizationClient;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import feign.Client;
import feign.Feign;
import feign.FeignException;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
@Component
//@SpringBootTest( webEnvironment = WebEnvironment.DEFINED_PORT, properties = {"symbiote.crm.url=http://localhost:8080"})
public  class CRMMessageHandler {
	
	private static final Log logger = LogFactory.getLog(CRMMessageHandler.class);

	private CRMRestService jsonclient;

	
	//localAAMAddress
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
	
    //coreAAMAddress
	@Value("${symbiote.coreaam.url}")
	private String coreAAMUrl;

	
	@Value("${symbiote.keystorepath}")
	private String keystorePath;

	@Value("${symbiote.keystorepassword}")
	private String keystorePassword;

	@Value("${symbiote.clientid}")
	private String clientId;

	@Value("${symbiote.username}")
	private String username;

	@Value("${symbiote.password}")
	private String password;

	@Value("${symbiote.servicecomponentid}")
	private String serviceComponentIdentifier;
	
	@Value("${platform.id}")
	private String servicePlatformIdentifier;
	
	@Value("${symbIoTe.aam.integration}")
	private boolean useSecurity;
	
	
    
	
	public void setService(CRMRestService service){
		jsonclient = service;
	}
	
    @PostConstruct
	public void createClient() throws SecurityHandlerException {
    	
        Feign.Builder builder = Feign.builder()
                .decoder(new JacksonDecoder())
                .encoder(new JacksonEncoder());
    	if (useSecurity) {
    	IComponentSecurityHandler secHandler = ComponentSecurityHandlerFactory
                .getComponentSecurityHandler(
                		coreAAMUrl, keystorePath, keystorePassword,
                    clientId, url, false,
                    username, password  );
    	
    	Client client = new SymbioteAuthorizationClient(
    		    secHandler, serviceComponentIdentifier,servicePlatformIdentifier, new Client.Default(null, null));

		logger.info("Will use "+ url +" to access to CRM");
		builder = builder.client(client);
		//jsonclient = Feign.builder().decoder(new JacksonDecoder()).encoder(new JacksonEncoder()).client(client).target(CRMRestService.class, url);
        }
    	jsonclient = builder.target(CRMRestService.class, url);
	}
		
	 
    /**
	  * Send Monitoring information to CRM
	  */
    public String doPost2Crm(CloudMonitoringPlatform platform)  {
		String result = "not send";
    	try{
			logger.info("Monitoring trying to publish data for platform "+ platform.getPlatformId() + " containing " +
					platform.getDevices().length + " devices");
			result = jsonclient.doPost2Crm(platform.getPlatformId(), platform);
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
			logger.info("Monitoring trying to publish data for platform "+ platform.getPlatformId() + " containing " +
					platform.getDevices().length + " devices");
			result = jsonclient.doPost2Crm(platform.getPlatformId(), platform);
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


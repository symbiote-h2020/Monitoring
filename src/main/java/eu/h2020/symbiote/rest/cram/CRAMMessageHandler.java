package eu.h2020.symbiote.rest.cram;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.stereotype.Component;

import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringPlatform;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

@SpringBootTest( webEnvironment = WebEnvironment.DEFINED_PORT, properties = {"symbiote.cram.url=http://localhost:8080"})
public  class CRAMMessageHandler {
	
	private static final Log logger = LogFactory.getLog(CRAMMessageHandler.class);

	private CRAMRestService jsonclient;

	@Value("${symbiote.cram.url}")
	private String url;

    public CRAMMessageHandler() {
		jsonclient = Feign.builder().decoder(new GsonDecoder()).encoder(new GsonEncoder()).target(CRAMRestService.class, url);
    }
		
	
    public void doPostAlCram(CloudMonitoringPlatform platform)  {
		try{
			logger.info("Monitoring trying to publish data for platform "+ platform.getInternalId() + " containing " + 
					platform.getDevices().length + " devices");
			jsonclient.doPostAlCram(platform.getInternalId(), platform);
		}catch(Throwable t){
			logger.error("Error accessing to CRAM server at " + url, t);
		}
	}

}


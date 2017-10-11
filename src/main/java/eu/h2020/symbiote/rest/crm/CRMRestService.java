package eu.h2020.symbiote.rest.crm;




import org.springframework.web.bind.annotation.RequestBody;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.core.cci.ResourceRegistryRequest;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CRMRestService {
	
	@RequestLine("POST crm/monitoring/{platformId}/devices/status")
    public String doPost2Crm(
    		@Param("platformId") String platformId, 
    		CloudMonitoringPlatform platform);


}


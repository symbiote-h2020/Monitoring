package eu.h2020.symbiote.monitoring.crm;




import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;

import feign.Param;
import feign.RequestLine;

public interface CRMRestService {
	
	@RequestLine("POST crm/monitoring/{platformId}/devices/status")
    public String doPost2Crm(
    		@Param("platformId") String platformId, 
    		CloudMonitoringPlatform platform);


}


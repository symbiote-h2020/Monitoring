package eu.h2020.symbiote.rest.crm;




import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringPlatform;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CRMRestService {
	
	@RequestLine("POST crm/monitoring/{platformId}/devices/status")
	@Headers({"Content-Type: application/json", "X-Auth-Token: {token}"})	
    public String doPost2Crm(
    		@Param("platformId") String platformId, 
    		CloudMonitoringPlatform platform, 
    		@Param("token") String token);

	

}


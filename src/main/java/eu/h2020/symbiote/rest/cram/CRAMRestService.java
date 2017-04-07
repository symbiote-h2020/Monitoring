package eu.h2020.symbiote.rest.cram;




import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringPlatform;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CRAMRestService {
	
	@RequestLine("POST crm/monitoring/{platformId}/devices/status")
	@Headers("Content-Type: application/json")
    public String doPostAlCram(@Param("platformId") String platformId, CloudMonitoringPlatform platform);

	

}


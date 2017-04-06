package eu.h2020.symbiote.rest.cram;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringPlatform;
import feign.Headers;
import feign.RequestLine;

public interface CRAMRestService {
	
	@RequestLine("POST crm/monitoring/{platformId}/devices/status")
	@Headers("Content-Type: application/json")
    public void doPostAlCram(@PathVariable(MonitoringConstants.PLATFORM_ID) String platformId, @RequestBody CloudMonitoringPlatform platform);

	

}


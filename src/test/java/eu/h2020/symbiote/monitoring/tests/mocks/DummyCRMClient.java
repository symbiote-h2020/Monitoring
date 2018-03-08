package eu.h2020.symbiote.monitoring.tests.mocks;

import eu.h2020.symbiote.client.ClientConstants;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coreInterface/v1")
public class DummyCRMClient {

    @RequestMapping(method = RequestMethod.POST, path = ClientConstants.PUBLISH_MONITORING_DATA,
            consumes = "application/json")
    public void doPost2Crm(
            @PathVariable("platformId") String platformId, CloudMonitoringPlatform platformData) {
        assert platformData != null;
    }

}

package eu.h2020.symbiote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import eu.h2020.symbiote.rest.crm.CRMMessageHandler;
import eu.h2020.symbiote.rest.crm.CRMRestService;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;

public class MonitoringPublishTests {

	private CRMRestService endpoint = Mockito.mock(CRMRestService.class);
	private static final Log logger = LogFactory.getLog(MonitoringPublishTests.class);

	@Before
	public void prepare() {
		Mockito.when(endpoint.doPost2Crm(Matchers.anyString(), Mockito.any(CloudMonitoringPlatform.class), Matchers.anyString()))
		.thenReturn("Monitoring message received in CRM");
	}

	@Test
	public void testPost2Crm() {
		logger.info("testPost2Crm starts");
		CRMMessageHandler client = new CRMMessageHandler();
		client.setService(endpoint);
		
		String message = client.doPost2Crm(getTestPlatform());
		logger.info("TEST RESULT --> Message from CRM: " + message);
		assert message != null;
		assert message.equalsIgnoreCase("Monitoring message received in CRM");
	}
	
	public CloudMonitoringPlatform getTestPlatform(){
		CloudMonitoringPlatform cmp = new CloudMonitoringPlatform();
		cmp.setInternalId("symbiotePlatform");
		
		CloudMonitoringDevice device1 = new CloudMonitoringDevice();
		device1.setAvailability(1);
		device1.setLoad(23);
		device1.setId("device1");
		device1.setTimestamp("1491407490.48");
		
		CloudMonitoringDevice device2 = new CloudMonitoringDevice();
		device2.setAvailability(1);
		device2.setLoad(50);
		device2.setId("device1");
		device2.setTimestamp("1491654321.48");
		
		CloudMonitoringDevice[] devices = new CloudMonitoringDevice[2];
		devices[0] = device1;
		devices[1] = device2;
		cmp.setDevices(devices);
		
		return cmp; 
	}
}

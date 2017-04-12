package eu.h2020.symbiote;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import eu.h2020.symbiote.rest.cram.CRAMMessageHandler;
import eu.h2020.symbiote.rest.cram.CRAMRestService;
import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiotelibraries.cloud.monitoring.model.CloudMonitoringPlatform;

public class MonitoringPublishTests {

	private CRAMRestService endpoint = Mockito.mock(CRAMRestService.class);

	@Before
	public void prepare() {
		Mockito.when(endpoint.doPostAlCram(Matchers.anyString(), Mockito.any(CloudMonitoringPlatform.class)))
		.thenReturn("Monitoring message received in CRAM");
	}

	@Test
	public void testPost2Cram() {
		CRAMMessageHandler client = new CRAMMessageHandler();
		client.setService(endpoint);
		
		String message = client.doPostAlCram(getTestPlatform());
		System.out.println("TEST RESULT --> Message from CRAM: " + message);
		assert message != null;
		assert message.equalsIgnoreCase("Monitoring message received in CRAM");
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

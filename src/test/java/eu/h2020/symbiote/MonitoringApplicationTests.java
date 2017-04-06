package eu.h2020.symbiote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.core.model.Location;
import eu.h2020.symbiote.rabbitmq.RHResourceMessageHandler;
import eu.h2020.symbiotelibraries.cloud.model.CloudResource;
import eu.h2020.symbiotelibraries.cloud.model.CloudResourceParams;


@RunWith(SpringRunner.class)
@SpringBootTest({"eureka.client.enabled=false"})
public class MonitoringApplicationTests {
	//symbiote.rabbitmq.host.ip
	//urlformcram localhost
	@Autowired Icinga2Manager icinga2Manager;
	@Autowired
	 private RHResourceMessageHandler rhResourceRegistrationMessageHandler;
	@Test
	public void contextLoads() {
	}

	
	@Test
	public void createResource(){
		//create resource and add it to a list
		CloudResource resource = getTestResource();
		List<CloudResource> resources = new ArrayList<CloudResource>();
		resources.add(resource);	
		
//		icinga2Manager.addResources(resources);
		//send the message using RabbitMQ
//		rhResourceRegistrationMessageHandler.sendResourcesRegistrationMessage(resources);
	}
		
	
	private CloudResource getTestResource(){
		   CloudResource resource = new CloudResource();
		   
			Location location = new Location();
			location.setAltitude(500.0);
			location.setDescription("my_location");
			location.setLatitude(45.0);
			location.setLongitude(34.3);
			location.setName("my_location_name");
			resource.setInternalId("platformId1");
			resource.setId("symbioteId1");
			resource.setHost("127.0.0.1");
			resource.setLocation(location);
			resource.setDescription("my resource description");
			resource.setName("symbiote_device1");
			CloudResourceParams params = new CloudResourceParams();
			params.setDevice_name(resource.getName());
			params.setIp_address(resource.getHost());
			params.setSymbiote_id(resource.getId());
			resource.setParams(params);
			
			resource.setObservedProperties(Arrays.asList(new String[]{"temperature", "humidity"}));
			resource.setOwner("me");
			resource.setResourceURL("http://localhost:4545/myresourceurl");
		   return resource; 
	   }
}
package eu.h2020.symbiote;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.core.model.Location;
import eu.h2020.symbiote.core.model.resources.Resource;
import eu.h2020.symbiote.rabbitmq.RHResourceMessageHandler;
import eu.h2020.symbiotelibraries.cloud.model.current.CloudResource;
import eu.h2020.symbiotelibraries.cloud.model.current.CloudResourceParams;




@RunWith(SpringRunner.class)
@SpringBootTest({"eureka.client.enabled=false"})
public class MonitoringApplicationTests {
	//symbiote.rabbitmq.host.ip
	//urlformcram localhost
//	@Autowired Icinga2Manager icinga2Manager;
	@Autowired
	 private RHResourceMessageHandler rhResourceRegistrationMessageHandler;

	

	
	@Test
	public void createResource(){
		//create resource and add it to a list
		CloudResource resource = getTestResource();
		List<CloudResource> resources = new ArrayList<CloudResource>();
		resources.add(resource);	

		//send the message using RabbitMQ
		rhResourceRegistrationMessageHandler.sendResourcesRegistrationMessage(resources);
	}
		
	
	private CloudResource getTestResource(){
		   CloudResource resource = new CloudResource();
		   
		   resource.setInternalId("internalId1");
		   resource.setHost("127.0.0.1");
		   resource.setName("symbiote_device1");
		   
		   CloudResourceParams params = new CloudResourceParams();
		   params.setIp_address(resource.getHost());
		   params.setInternalId(resource.getInternalId());
		   params.setDevice_name(resource.getName());
		   resource.setParams(params);
		   
		   Resource r = new Resource();
		   r.setId("symbioteId1");
		   r.setInterworkingServiceURL("http://tests.io/interworking/url");
		   List<String> comments = new ArrayList<String>();
		   comments.add("comment1");
		   comments.add("comment2");
		   r.setComments(comments);
		   List<String> labels = new ArrayList<String>();
		   labels.add("label1");
		   labels.add("label2");
		   r.setLabels(labels);
		   resource.setResource(r);			

		   return resource; 
	   }
}
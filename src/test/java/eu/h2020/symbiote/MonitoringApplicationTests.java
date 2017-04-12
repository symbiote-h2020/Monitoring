package eu.h2020.symbiote;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.core.model.resources.Resource;
import eu.h2020.symbiote.rabbitmq.RHResourceMessageHandler;
import eu.h2020.symbiote.rest.RestProxy;
import eu.h2020.symbiotelibraries.cloud.model.current.CloudResource;
import eu.h2020.symbiotelibraries.cloud.model.current.CloudResourceParams;




@RunWith(SpringRunner.class)
@SpringBootTest({"eureka.client.enabled=false"})
public class MonitoringApplicationTests {

	protected static final Logger LOGGER = LoggerFactory.getLogger(MonitoringApplicationTests.class);
	//symbiote.rabbitmq.host.ip
	//urlformcram localhost
//	@Autowired Icinga2Manager icinga2Manager;
	 @Autowired
	 private RHResourceMessageHandler rhResourceRegistrationMessageHandler;
	 private CloudResource cre_resource;
	 private CloudResource upd_resource;	
	 private CloudResource del_resource;
		
	
    // Execute the Setup method before the test.     
	@Before     
	public void setUp() throws Exception { 
		
		//CREATE
		
		
		
		//UPDATE
		upd_resource = getTestResource("Upd");
		System.out.println("Creating resource with InternalId=" + upd_resource.getInternalId());
		List<CloudResource> resources = new ArrayList<CloudResource>();
		resources.add(upd_resource);	
		//send the message using RabbitMQ
		rhResourceRegistrationMessageHandler.sendResourcesRegistrationMessage(resources);

		LOGGER.info("********************************************************************");
		LOGGER.info("****** Verify CREATE:" + upd_resource.getInternalId() +"************");
		LOGGER.info("********************************************************************");

		int t=20000;
		System.out.println("Sleeping: "+ t/1000 + "segs.");
		Thread.sleep(t);
		System.out.println("Sleeping END");
		
		//DELETE
		
		
	} 

	@Test
	public void updateResource(){
      // test update
		String id= upd_resource.getInternalId();
		System.out.println("Updating resource with InternalId=" + id);
		
		LOGGER.info("ORIGINAL: "+ upd_resource.getResource().getComments().get(0));
		LOGGER.info("ORIGINAL: "+ upd_resource.getResource().getComments().get(1));
	  // data to update 
		Resource r = new Resource();
		r.setId("symbioteId1");
		r.setInterworkingServiceURL("http://tests.io/interworking/url");
		List<String> comments = new ArrayList<String>();
			comments.add("UPDATED-comment1");
			comments.add("UPDATED-comment2");
		r.setComments(comments);
		List<String> labels = new ArrayList<String>();
			labels.add("label1");
			labels.add("label2");
		r.setLabels(labels);
		
		// Update
		CloudResource upd_res = getTestResource();
		List<CloudResource> resources = new ArrayList<CloudResource>();
		upd_res.setResource(r);
		upd_res.setInternalId(id);
		resources.add(upd_res);	
		LOGGER.info("UPDATED: "+ upd_res.getResource().getComments().get(0));
		LOGGER.info("UPDATED: "+ upd_res.getResource().getComments().get(1));
		//send the message using RabbitMQ
		rhResourceRegistrationMessageHandler.sendResourcesUpdateMessage(resources);
		LOGGER.info("********************************************************************");
		LOGGER.info("****** Verify UPDATED:" + upd_resource.getInternalId() +"************");
		LOGGER.info("********************************************************************");
		int t=20000;
		System.out.println("Sleeping: "+ t/1000 + "segs.");
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Sleeping END");
		
	}
	

	
    //@Test
	public void createResource(){
		//create resource and add it to a list
		cre_resource = getTestResource();
		List<CloudResource> resources = new ArrayList<CloudResource>();

		resources.add(cre_resource);	

		//send the message using RabbitMQ
		rhResourceRegistrationMessageHandler.sendResourcesRegistrationMessage(resources);
	}
	
	//@Test
	public void deleteResource(){
      // test delete
	 String id="Upd-459111a5-e0e2-417a-9fd2-2ab6e9fc0546";
	 List<String> resources = new ArrayList<String>();
	 resources.add(id);
	 
	//send the message using RabbitMQ
	 
	 rhResourceRegistrationMessageHandler.sendResourcesUnregistrationMessage(resources);
	 /*
	 try {
		Thread.sleep(900000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
	*/
	}	
	
	
	@After
	public void after(){
      // test update
		String id= upd_resource.getInternalId();
		System.out.println("Deleting resource with InternalId=" + id);
	// delete resource
	 
	 List<String> resources = new ArrayList<String>();
	 resources.add(id);
	 
	//send the message using RabbitMQ
	 
	 rhResourceRegistrationMessageHandler.sendResourcesUnregistrationMessage(resources);
		LOGGER.info("********************************************************************");
		LOGGER.info("****** Verify DELETED:" + upd_resource.getInternalId() +"************");
		LOGGER.info("********************************************************************");
//		 try {
//			Thread.sleep(900000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	 
	}	
	
	
	private CloudResource getTestResource(){
		return getTestResource("Test");
	}
	private CloudResource getTestResource(String prefix){
		
			CloudResource resource = new CloudResource();
		   
		   //String id="IdUpdateTest";
		   String id = prefix+"-"+java.util.UUID.randomUUID().toString();
		   resource.setInternalId(id);
		   //resource.setHost("127.0.0.1");
		   resource.setHost("62.14.219.137");
		   		   
		   CloudResourceParams params = new CloudResourceParams();
		   params.setIp_address(resource.getHost());
		   params.setInternalId(resource.getInternalId());
		   params.setDevice_name(resource.getInternalId());
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
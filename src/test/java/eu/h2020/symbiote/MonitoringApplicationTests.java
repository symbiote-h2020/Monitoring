package eu.h2020.symbiote;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.core.model.resources.Resource;
import eu.h2020.symbiote.db.ResourceRepository;
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
	//@Autowired Icinga2Manager icinga2Manager;

	 @Autowired
	 private  RHResourceMessageHandler rhResourceRegistrationMessageHandler;
	 @Autowired
	 private ResourceRepository resourceRepo;
	 
	 private  CloudResource cre_resource;
	 private  CloudResource upd_resource;	
	 private  CloudResource del_resource;
	 
	 private int tdelaym = 10000;
	 
	 @Test
	public void createResource(){
	    	
	    	// CREATE TEST
			//create resource and add it to a list
	    	cre_resource = getTestResource("cre");
			List<CloudResource> resources = new ArrayList<CloudResource>();

			resources.add(cre_resource);	

			//send the message using RabbitMQ
			rhResourceRegistrationMessageHandler.sendResourcesRegistrationMessage(resources);
			LOGGER.info("********************************************************************");
			LOGGER.info("****** Verify CREATE:" + cre_resource.getInternalId() +"************");
			LOGGER.info("********************************************************************");
			delay(tdelaym);
			

			CloudResource result = resourceRepo.findOne(cre_resource.getInternalId());

			assertEquals(cre_resource.getResource().getInterworkingServiceURL(), result.getResource().getInterworkingServiceURL());   
		
			// AFTER CREATE
			String id_cre= cre_resource.getInternalId();
			System.out.println("Deleting resource with InternalId=" + id_cre);
			List<String> resources_cre = new ArrayList<String>();
			resources_cre.add(id_cre);
			rhResourceRegistrationMessageHandler.sendResourcesUnregistrationMessage(resources_cre);
			LOGGER.info("********************************************************************");
			LOGGER.info("****** Verify DELETED TO CREATED:" + id_cre +"************");
			LOGGER.info("********************************************************************");
			delay(tdelaym);
	}

	@Test
	public void updateResource(){
		
		//BEFORE UPDATE
		upd_resource = getTestResource("Upd");
		System.out.println("Creating resource with InternalId=" + upd_resource.getInternalId());
		List<CloudResource> resources = new ArrayList<CloudResource>();
		resources.add(upd_resource);	
		//send the message using RabbitMQ
		rhResourceRegistrationMessageHandler.sendResourcesRegistrationMessage(resources);

		LOGGER.info("********************************************************************");
		LOGGER.info("****** Verify CREATE to UPDATE:" + upd_resource.getInternalId() +"************");
		LOGGER.info("********************************************************************");
		delay(tdelaym);
		
		
		// UPDATE TEST
		String newValue = "http://localhost";
		String id= upd_resource.getInternalId();
		System.out.println("Updating resource with InternalId=" + id);
		
		LOGGER.info("ORIGINAL: "+ upd_resource.getResource().getComments().get(0));
		LOGGER.info("ORIGINAL: "+ upd_resource.getResource().getComments().get(1));
		LOGGER.info("ORIGINAL: "+ upd_resource.getResource().getInterworkingServiceURL());
	  // data to update 
		Resource r = new Resource();
		r.setId("symbioteId1");
		r.setInterworkingServiceURL(newValue);
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
		List<CloudResource> resources_upd = new ArrayList<CloudResource>();
		upd_res.setResource(r);
		upd_res.setInternalId(id);
		resources_upd.add(upd_res);	
		LOGGER.info("UPDATED: "+ upd_res.getResource().getComments().get(0));
		LOGGER.info("UPDATED: "+ upd_res.getResource().getComments().get(1));
		LOGGER.info("UPDATED: "+ upd_res.getResource().getInterworkingServiceURL());
		//send the message using RabbitMQ
		rhResourceRegistrationMessageHandler.sendResourcesUpdateMessage(resources_upd);
		LOGGER.info("********************************************************************");
		LOGGER.info("****** Verify UPDATED:" + upd_resource.getInternalId() +"************");
		LOGGER.info("********************************************************************");
		delay(tdelaym);
		
		CloudResource result = resourceRepo.findOne(upd_resource.getInternalId());

		assertEquals(result.getResource().getInterworkingServiceURL(), newValue);

		// AFTER UPDATE	
	    // test update
			String id_del= upd_resource.getInternalId();
			System.out.println("Deleting resource with InternalId=" + id_del);
		// delete resource
		 List<String> resources_del = new ArrayList<String>();
		 resources_del.add(id_del);
		 
		//send the message using RabbitMQ
		 
		 rhResourceRegistrationMessageHandler.sendResourcesUnregistrationMessage(resources_del);
			LOGGER.info("********************************************************************");
			LOGGER.info("****** Verify DELETED TO UPDATED:" + upd_resource.getInternalId() +"************");
			LOGGER.info("********************************************************************");
		delay(tdelaym);
//			 try {
//				Thread.sleep(900000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	//
		
	}
	

	

	
	@Test
	public void deleteResource(){

		//BEFORE DELETE
		//create resource and add it to a list
		del_resource = getTestResource("del");
		List<CloudResource> resources = new ArrayList<CloudResource>();
		resources.add(del_resource);	
		
		//send the message using RabbitMQ
		rhResourceRegistrationMessageHandler.sendResourcesRegistrationMessage(resources);
		LOGGER.info("********************************************************************");
		LOGGER.info("****** Verify CREATE to DELETE:" + del_resource.getInternalId() +"************");
		LOGGER.info("********************************************************************");
		delay(tdelaym);	
		
		// test delete
		String id=del_resource.getInternalId();
		//String id = "Upd-3d4a4086-052c-46ea-8333-6ba164789d02";
		List<String> resources_del = new ArrayList<String>();
		resources_del.add(id);
	 
		//send the message using RabbitMQ
		rhResourceRegistrationMessageHandler.sendResourcesUnregistrationMessage(resources_del);
		LOGGER.info("********************************************************************");
		LOGGER.info("****** Verify DELETE:" + id +"************");
		LOGGER.info("********************************************************************");
		delay(tdelaym);
		

		CloudResource result = resourceRepo.findOne(id);
		assertEquals(null, result);   
		
		
	 /*
	 try {
		Thread.sleep(900000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
	*/
	}	
	
	
	private CloudResource getTestResource(){
		return getTestResource("Test");
	}
	private static CloudResource getTestResource(String prefix){
		
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

	
private static void delay(int timems) {
	int t=timems;
	System.out.println("Sleeping: "+ t/1000 + "segs.");
	try {
		Thread.sleep(t);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println("Sleeping END");
}
	
}
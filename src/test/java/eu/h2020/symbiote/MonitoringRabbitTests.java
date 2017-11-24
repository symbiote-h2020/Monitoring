package eu.h2020.symbiote;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.ResourceRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest({"eureka.client.enabled=false", "symbIoTe.aam.integration=false"})
public class MonitoringRabbitTests {
	
	private static Logger logger = LoggerFactory.getLogger(MonitoringRabbitTests.class);
	
	@Autowired
    private ResourceRepository resourceRepo;

	@Autowired
    private RabbitTemplate rabbitTemplate;
	
	private Random rand;
	
	private static String INTERNAL_ID_ADD = "internalId_add_rabbit";
	private static String INTERNAL_ID_DELETE = "internalId_delete_rabbit";
	private static String INTERNAL_ID_UPDATE = "internalId_update_rabbit";
	
	private CloudResource add_item;
	private CloudResource delete_item;
	private String idForDelete;
	private CloudResource update_item;
	private String oldValue = "http://tests.io/interworking/url";
	private String newValue = "http://localhost";
	
	@Before
    public void setup() throws IOException, TimeoutException {
        rand = new Random();
        
        
     		//CREATE
    		add_item = getResourceForAdd();
    		
    		//DELETE
    		delete_item = getResourceForDelete();
    		//resourceRepo.save(delete_item);
    		
    		idForDelete = delete_item.getInternalId();
    		
    		//UPDATE
    		update_item = getResourceForUpdate();
    		//resourceRepo.save(update_item);
    		
    		// Sleep to make sure that the platform has been saved to the repo before querying
            try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				logger.error("ERROR pausing in before method on testing");
				e.printStackTrace();
			} 		
    }
	
	
	@Test
	public void createDeviceTest() throws Exception {

		List<CloudResource> resources = new ArrayList<CloudResource>();
		resources.add(add_item);

		ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(resources);
        
		sendResourceMessage(MonitoringConstants.EXCHANGE_NAME_REGISTRATION_TEST, 
				MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME_TEST, message.getBytes("UTF-8"));

		// Sleep to make sure that the platform has been saved to the repo before querying
		TimeUnit.SECONDS.sleep(3);

		CloudMonitoringResource result = resourceRepo.findOne(add_item.getInternalId());

		assertEquals(add_item.getResource().getInterworkingServiceURL(), 
				result.getResource().getResource().getInterworkingServiceURL());

		resourceRepo.delete(add_item.getInternalId());
	}
		

	@Test
	public void deleteDeviceTest() throws Exception {

		List<String> resourceIds = new ArrayList<String>();
		resourceIds.add(idForDelete);

		ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(resourceIds);
        
		sendResourceMessage(MonitoringConstants.EXCHANGE_NAME_UNREGISTRATION_TEST, 
				MonitoringConstants.RESOURCE_UNREGISTRATION_QUEUE_NAME_TEST, message.getBytes("UTF-8"));

		// Sleep to make sure that the platform has been deleted for the repo before querying
		TimeUnit.SECONDS.sleep(3);

		CloudMonitoringResource result = resourceRepo.findOne(delete_item.getInternalId());

		assertEquals(null, result);   
	}

	@Test
	public void updateDeviceTest() throws Exception {
		update_item.getResource().setInterworkingServiceURL(newValue);
		
		List<CloudResource> resources = new ArrayList<CloudResource>();
		resources.add(update_item);
		
		ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(resources);
        
		sendResourceMessage(MonitoringConstants.EXCHANGE_NAME_UPDATED_TEST, 
				MonitoringConstants.RESOURCE_UPDATED_QUEUE_NAME_TEST, message.getBytes("UTF-8"));

		// Sleep to make sure that the platform has been updated for the repo before querying
		TimeUnit.SECONDS.sleep(3);

		CloudMonitoringResource result = resourceRepo.findOne(update_item.getInternalId());

		assertEquals(result.getResource().getResource().getInterworkingServiceURL(), newValue);
		
		//resourceRepo.delete(update_item);
	}

	
	private CloudResource getResourceForAdd(){
		return TestUtils.createResource(INTERNAL_ID_ADD);
	}
	
	void sendResourceMessage (String exchange, String key, byte[] message) throws Exception {

		rabbitTemplate.convertAndSend(exchange, key, message,
				m -> {
					m.getMessageProperties().setContentType("application/json");
					m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
					return m;
				});
	}
	
	private CloudResource getResourceForDelete(){
		return TestUtils.createResource(INTERNAL_ID_DELETE);
		
	}
	
	private CloudResource getResourceForUpdate(){
		return TestUtils.createResource(INTERNAL_ID_UPDATE);
	}

}

package eu.h2020.symbiote;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.core.model.resources.Resource;
import eu.h2020.symbiote.db.ResourceRepository;
import eu.h2020.symbiote.rabbitmq.RHResourceMessageHandler;
import eu.h2020.symbiotelibraries.cloud.model.current.CloudResource;
import eu.h2020.symbiotelibraries.cloud.model.current.CloudResourceParams;

@RunWith(SpringRunner.class)
@SpringBootTest({"eureka.client.enabled=false"})
public class MonitoringRabbitTests {
	
	private static Logger logger = LoggerFactory.getLogger(MonitoringRabbitTests.class);
	
	@Autowired
    private ResourceRepository resourceRepo;

	@Autowired
    private RabbitTemplate rabbitTemplate;
	
	private Random rand;
	
	@Before
    public void setup() throws IOException, TimeoutException {
        rand = new Random();
    }
	
	
	@Test
	public void createDeviceTest() throws Exception {

		CloudResource resource = getTestResource();
		List<CloudResource> resources = new ArrayList<CloudResource>();
		resources.add(resource);

		ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(resources);
        
		sendResourceMessage(RHResourceMessageHandler.EXCHANGE_NAME_REGISTRATION_TEST, 
				RHResourceMessageHandler.RESOURCE_REGISTRATION_QUEUE_NAME_TEST, message.getBytes("UTF-8"));

		// Sleep to make sure that the platform has been saved to the repo before querying
		TimeUnit.SECONDS.sleep(3);

		CloudResource result = resourceRepo.findOne(resource.getInternalId());

		assertEquals(resource.getResource().getInterworkingServiceURL(), 
				result.getResource().getInterworkingServiceURL());   

		resourceRepo.delete(resource.getInternalId());
	}
	
	
	private CloudResource getTestResource(){
		CloudResource resource = new CloudResource();

		resource.setInternalId("internalIdRabbit");
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
	
	void sendResourceMessage (String exchange, String key, byte[] message) throws Exception {

		rabbitTemplate.convertAndSend(exchange, key, message,
				m -> {
					m.getMessageProperties().setContentType("application/json");
					m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
					return m;
				});
	}

}

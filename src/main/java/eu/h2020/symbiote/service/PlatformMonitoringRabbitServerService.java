package eu.h2020.symbiote.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.constants.MonitoringConstants;


@Service
public class PlatformMonitoringRabbitServerService {

    private static Log log = LogFactory.getLog(PlatformMonitoringRabbitServerService.class);

	@Autowired 
	PlatformMonitoringComponentRestService platformManager;
	/**
	 * Spring AMQP Listener for resource registration requests. This method is invoked when Registration
	 * Handler sends a resource registration request and it is responsible for forwarding the message
	 * to the symbIoTe core. As soon as it receives a reply, it manually sends back the response
	 * to the Registration Handler via the appropriate message queue by the use of the RestAPICallback.
	 * 
	 * @param jsonObject A jsonObject containing the resource description
	 * @param headers The AMQP headers
	 */
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME, durable = "false", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_REGISTRATION, ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
			key = MonitoringConstants.RESOURCE_REGISTRATION_ROUTING_KEY)
			)
	public void resourceRegistration(Message message, @Headers() Map<String, String> headers) {
		          Gson gson = new Gson();
		          ArrayList<CloudResource> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<CloudResource>>() {}.getType());
		          log.info("Received Resource Registration message");
		          if (resources != null){
		        	  platformManager.addOrUpdateInInternalRepository(resources);
		          }
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_UNREGISTRATION_QUEUE_NAME, durable = "false", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_UNREGISTRATION, ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
			key = MonitoringConstants.RESOURCE_UNREGISTRATION_ROUTING_KEY)
			)
	public void resourceUnregistration(Message message, @Headers() Map<String, String> headers) {
		          log.info("Received Resource Unregistration message");
		          Gson gson = new Gson();
		          ArrayList<String> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<String>>() {}.getType());
		          platformManager.deleteInInternalRepository(resources);
		          		          
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_UPDATED_QUEUE_NAME, durable = "false", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_UPDATED, ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
			key = MonitoringConstants.RESOURCE_UPDATED_ROUTING_KEY)
			)
	public void resourceUpdate(Message message, @Headers() Map<String, String> headers) {
		          log.info("Received Resource Update message");
		          Gson gson = new Gson();
		          ArrayList<CloudResource> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<CloudResource>>() {}.getType());
		          platformManager.addOrUpdateInInternalRepository(resources);
		          
	}
	
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME_TEST, durable = "true", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_REGISTRATION_TEST, ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT),
			key = MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME_TEST)
			)
	public void resourceRegistrationTest(Message message, @Headers() Map<String, String> headers) {
		          Gson gson = new Gson();
		          ArrayList<CloudResource> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<CloudResource>>() {}.getType());
		          
		          List<CloudResource> added = platformManager.addOrUpdateInInternalRepository(resources);
		          if (added != null && added.size()>0){
		        	  System.out.println("TEST: added " + added.size() + " devices to database");  
		          }
		          else {
		        	  System.out.println("TEST: added 0 devices to database");
		          }
	}
	
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_UNREGISTRATION_QUEUE_NAME_TEST, durable = "true", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_UNREGISTRATION_TEST, ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT),
			key = MonitoringConstants.RESOURCE_UNREGISTRATION_QUEUE_NAME_TEST)
			)
	public void resourceUnregistrationTest(Message message, @Headers() Map<String, String> headers) {
		          System.out.println(message.getBody());
		          
		          Gson gson = new Gson();
		          ArrayList<String> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<String>>() {}.getType());
		          //		          platformManager.deleteResources(resources);    
		          List<CloudResource> deleted = platformManager.deleteInInternalRepository(resources);
		          if (deleted != null && deleted.size()>0){
		        	  System.out.println("TEST: deleted " + deleted.size() + " devices from database");  
		          }
		          else {
		        	  System.out.println("TEST: deleted 0 devices to database");
		          }
	}
	
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_UPDATED_QUEUE_NAME_TEST, durable = "true", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_UPDATED_TEST, ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT),
			key = MonitoringConstants.RESOURCE_UPDATED_QUEUE_NAME_TEST)
			)
	public void resourceUpdateTest(Message message, @Headers() Map<String, String> headers) {
		          Gson gson = new Gson();
		          ArrayList<CloudResource> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<CloudResource>>() {}.getType());
		          //		          platformManager.updateResources(resources);
		          List<CloudResource> updated = platformManager.addOrUpdateInInternalRepository(resources);
		          if (updated != null && updated.size()>0){
		        	  System.out.println("TEST: updated " + updated.size() + " devices from database");  
		          }
		          else {
		        	  System.out.println("TEST: updated 0 devices to database");
		          }
		          
	}
	
	
}

package eu.h2020.symbiote.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import eu.h2020.symbiote.Icinga2Manager;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiotelibraries.cloud.model.current.CloudResource;


@Service
public class PlatformMonitoringRabbitServerService {


	@Autowired Icinga2Manager icinga2Manager;
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
			value = @Queue(value = MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME, durable = "true", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_REGISTRATION, ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT),
			key = MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME)
			)
	public void resourceRegistration(Message message, @Headers() Map<String, String> headers) {
		          Gson gson = new Gson();
		          ArrayList<CloudResource> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<CloudResource>>() {}.getType());
//		          System.out.println("resourceBean "+resourceBean + " - "+ message.getBody());
		          if (resources != null){
		        	  icinga2Manager.addResources(resources);
		          }
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_UNREGISTRATION_QUEUE_NAME, durable = "true", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_UNREGISTRATION, ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT),
			key = MonitoringConstants.RESOURCE_UNREGISTRATION_QUEUE_NAME)
			)
	public void resourceUnregistration(Message message, @Headers() Map<String, String> headers) {
		          System.out.println(message.getBody());
		          
		          Gson gson = new Gson();
		          ArrayList<String> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<String>>() {}.getType());
		          icinga2Manager.deleteResources(resources);
		          		          
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_UPDATED_QUEUE_NAME, durable = "true", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_UPDATED, ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT),
			key = MonitoringConstants.RESOURCE_UPDATED_QUEUE_NAME)
			)
	public void resourceUpdate(Message message, @Headers() Map<String, String> headers) {
		          Gson gson = new Gson();
		          ArrayList<CloudResource> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<CloudResource>>() {}.getType());
		          icinga2Manager.updateResources(resources);
		          
	}
	
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME_TEST, durable = "true", autoDelete = "false", exclusive = "false"),
			exchange = @Exchange(value = MonitoringConstants.EXCHANGE_NAME_REGISTRATION_TEST, ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT),
			key = MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME_TEST)
			)
	public void resourceRegistrationTest(Message message, @Headers() Map<String, String> headers) {
		          Gson gson = new Gson();
		          ArrayList<CloudResource> resources = gson.fromJson(new String(message.getBody()), new TypeToken<ArrayList<CloudResource>>() {}.getType());
		          
		          List<CloudResource> added = icinga2Manager.addOrUpdateInInternalRepository(resources);
		          if (added != null && added.size()>0){
		        	  System.out.println("TEST: added " + added.size() + " devices to database");  
		          }
		          else {
		        	  System.out.println("TEST: added 0 devices to database");
		          }
//		          icinga2Manager.addResources(resources);
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
		          //		          icinga2Manager.deleteResources(resources);    
		          List<CloudResource> deleted = icinga2Manager.deleteInInternalRepository(resources);
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
		          //		          icinga2Manager.updateResources(resources);
		          List<CloudResource> updated = icinga2Manager.addOrUpdateInInternalRepository(resources);
		          if (updated != null && updated.size()>0){
		        	  System.out.println("TEST: updated " + updated.size() + " devices from database");  
		          }
		          else {
		        	  System.out.println("TEST: updated 0 devices to database");
		          }
		          
	}
	
	
}

package eu.h2020.symbiote.rabbitmq;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import eu.h2020.symbiotelibraries.cloud.model.current.CloudResource;






/**
 * This class is to communicate with RabbitMQ. Initially created by Elena
 *
 * @author: Elena Garrido
 * @version: 20/01/2017

 */
/**! \class RabbitMQFanoutMessageHandlerResourceList
 * \brief This class extends from the \class GenericRabbitMQRoutingMessageHandler and will be able to write and read a \class List of \class CloudResource
 * from the rabbitMQ routing queues 
 **/

class RabbitMQFanoutMessageHandlerResourceList extends GenericRabbitMQFanoutMessageHandler<List<CloudResource>> {

    public RabbitMQFanoutMessageHandlerResourceList(String exchangeName, String queueName) {
		super(exchangeName, queueName,  new TypeToken<ArrayList<CloudResource>>(){}.getType());
	}
}

package eu.h2020.symbiote.rabbitmq;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;



/**
 * This class is to communicate with RabbitMQ. Initially created by Elena
 *
 * @author: Elena Garrido
 * @version: 20/01/2017

 */
/**! \class RabbitMQFanoutMessageHandlerStringList
 * \brief This class extends from the \class GenericRabbitMQRoutingMessageHandler and will be able to write and read a \class List of \class String
 * from the rabbitMQ routing queues 
 **/

class RabbitMQFanoutMessageHandlerStringList extends GenericRabbitMQFanoutMessageHandler<List<String>> {

    public RabbitMQFanoutMessageHandlerStringList(String exchangeName, String queueName) {
		super(exchangeName, queueName,  new TypeToken<ArrayList<String>>(){}.getType());
	}
}

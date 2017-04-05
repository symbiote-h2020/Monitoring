package eu.h2020.symbiote.rabbitmq;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


/**
 * This class is to communicate with RabbitMQ. Initially created by mateuszl and updated by Elena
 *
 * @author: mateuszl, Elena Garrido
 * @version: 18/01/2017

 */
public class GenericRabbitMQFanoutMessageHandler <T>{

    private static Log logger = LogFactory.getLog( GenericRabbitMQFanoutMessageHandler.class );

    @Value("${symbiote.rabbitmq.host.ip}")
    String rabbitMQHostIP;

 	Type type;
    String exchangeName;
    String queueName;

    public GenericRabbitMQFanoutMessageHandler(String exchangeName, String queueName,  Type type){
    	this.exchangeName = exchangeName;
    	this.queueName = queueName; 
    	this.type = type;
    	
    }
    /**
     * Method for sending a message to specified 'queue' on RabbitMQ server. Object is converted to Json.
     *
     * @param object
     * @throws Exception
     */
    public void sendMessage(T object) throws Exception {
        logger.info("START OF sendMessage to queue"+queueName);
        Gson gson = new Gson();
        String objectInJson = gson.toJson(object);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQHostIP);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
   	 	channel.exchangeDeclare(exchangeName,"fanout",false,false, false, null);
        //channel.exchangeDeclare(exchangeName, "direct");
        //channel.queueDeclare(queueName, false, false, false, null);

        String message = objectInJson;
        channel.basicPublish(exchangeName, queueName, null, message.getBytes("UTF-8"));

        channel.close();
        connection.close();
        logger.info("END OF sendMessage to queue: "+queueName);

    }
}

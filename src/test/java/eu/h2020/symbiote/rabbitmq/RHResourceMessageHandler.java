package eu.h2020.symbiote.rabbitmq;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;




/**

 * This class invoke the RAbbitMQMessageHandler using the right message queue depending on the operation that is being
 * done with the data
 * @author: Elena Garrido
 * @version: 18/01/2017

 */

/**! \class RAPResourceMessageHandler 
 * \brief This class invokes the \class RabbitMQFanoutMessageHandlerResourceList or the \class RabbitMQFanoutMessageHandlerStringList  depending on the operation that is being
 * done with the data. 
 **/
@Component
public class RHResourceMessageHandler {

    private static Log logger = LogFactory.getLog(RHResourceMessageHandler.class);
    
	@Autowired
	private ApplicationContext applicationContext;

    public void sendResourcesRegistrationMessage(List<CloudResource> resources) {
        try {
            logger.info("Sending request for registration for " + resources.size() + " resources");
            RabbitMQFanoutMessageHandlerResourceList rabbitMQMessageHandler = new RabbitMQFanoutMessageHandlerResourceList(MonitoringConstants.EXCHANGE_NAME_REGISTRATION, MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME);
        	applicationContext.getAutowireCapableBeanFactory().autowireBean(rabbitMQMessageHandler);
        	rabbitMQMessageHandler.sendMessage(resources);
        } catch (Exception e) {
            logger.error("Fatal error sending data to EXCHANGE_NAME: " + MonitoringConstants.EXCHANGE_NAME_REGISTRATION+", RESOURCE_REGISTRATION_QUEUE_NAME:"+MonitoringConstants.RESOURCE_REGISTRATION_QUEUE_NAME, e);
        }
    }

    public void sendResourcesUnregistrationMessage(List<String> resourceIds) {
        try {
            logger.info("Sending request for unregistration of " + resourceIds.size() + " items");
            RabbitMQFanoutMessageHandlerStringList rabbitMQMessageHandler = new RabbitMQFanoutMessageHandlerStringList(MonitoringConstants.EXCHANGE_NAME_UNREGISTRATION, MonitoringConstants.RESOURCE_UNREGISTRATION_QUEUE_NAME);
        	applicationContext.getAutowireCapableBeanFactory().autowireBean(rabbitMQMessageHandler);
        	rabbitMQMessageHandler.sendMessage(resourceIds);
        } catch (Exception e) {
            logger.error("Fatal error sending data to EXCHANGE_NAME: "+ MonitoringConstants.EXCHANGE_NAME_UNREGISTRATION +", RESOURCE_UNREGISTRATION_QUEUE_NAME:"+MonitoringConstants.RESOURCE_UNREGISTRATION_QUEUE_NAME, e);
        }
    }

    public void sendResourcesUpdateMessage(List<CloudResource> resources) {
        try {
            logger.info("Sending request for update for " + resources.size() + " resources");
            RabbitMQFanoutMessageHandlerResourceList rabbitMQMessageHandler = new RabbitMQFanoutMessageHandlerResourceList(MonitoringConstants.EXCHANGE_NAME_UPDATED, MonitoringConstants.RESOURCE_UPDATED_QUEUE_NAME);
        	applicationContext.getAutowireCapableBeanFactory().autowireBean(rabbitMQMessageHandler);
        	rabbitMQMessageHandler.sendMessage(resources);
        } catch (Exception e) {
            logger.error("Fatal error sending data to EXCHANGE_NAME: " + MonitoringConstants.EXCHANGE_NAME_UPDATED+", RESOURCE_UPDATED_QUEUE_NAME:"+MonitoringConstants.RESOURCE_UPDATED_QUEUE_NAME, e);
        }
    }

}

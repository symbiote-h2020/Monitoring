package eu.h2020.symbiote.monitoring;

import eu.h2020.symbiote.monitoring.constants.MonitoringConstants;
import eu.h2020.symbiote.util.RabbitConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;



/**
 * Monitor Application main class 
 * Created by mateuszl on 22.09.2016.
 * @author: David Rojo, Fernando Campos, Jose Antonio Sanchez
 * @version: 14/03/2018
 */
@EnableDiscoveryClient    //when Eureka available

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
@EnableRabbit
public class MonitoringApplication {
  
  private static Log log = LogFactory.getLog(MonitoringApplication.class);

  @Value("${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}")
  private String rhExchangeName;

  @Value("${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}")
  private boolean rhDurable;

  @Value("${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}")
  private boolean rhAutoDelete;

  @Value("${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}")
  private String rapExchangeName;

  @Value("${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}")
  private boolean rapDurable;

  @Value("${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}")
  private boolean rapAutoDelete;
  
  @Bean
  public DirectExchange rhExchange() {
    return new DirectExchange(rhExchangeName, rhDurable, rhAutoDelete);
  }

  @Bean
  public DirectExchange rapExchange() {
    return new DirectExchange(rapExchangeName, rapDurable, rapAutoDelete);
  }

  @Bean
  public Queue registrationQueue() {
    return new Queue(MonitoringConstants.MONITORING_REGISTRATION_QUEUE_NAME,true, false, true);
  }
  
  @Bean
  public Queue unegistrationQueue() {
    return new Queue(MonitoringConstants.MONITORING_UNREGISTRATION_QUEUE_NAME,true, false, true);
  }
  
  @Bean
  public Queue sharingQueue() {
    return new Queue(MonitoringConstants.MONITORING_SHARING_QUEUE_NAME,true, false, true);
  }
  
  @Bean
  public Queue unSharingQueue() {
    return new Queue(MonitoringConstants.MONITORING_UNSHARING_QUEUE_NAME,true, false, true);
  }

  @Bean
  public Queue removingQueue() {
    return new Queue(MonitoringConstants.MONITORING_UNREGISTRATION_LOCAL_QUEUE_NAME,true, false, true);
  }

  @Bean
  public Queue rapAccessQueue() {
    return new Queue(MonitoringConstants.MONITORING_RESOURCE_ACCESS_QUEUE_NAME,true, false, true);
  }

	public static void main(String[] args) {
		
		SpringApplication.run(MonitoringApplication.class, args);

        try {
            // Subscribe to RabbitMQ messages
        } catch (Exception e) {
            log.error("Error occured during subscribing from Monitoring", e);
        }
    }
}

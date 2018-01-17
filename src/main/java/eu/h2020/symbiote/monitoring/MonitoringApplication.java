package eu.h2020.symbiote.monitoring;

import eu.h2020.symbiote.monitoring.constants.MonitoringConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;



/**
 * Monitor Application main class 
 * Created by mateuszl on 22.09.2016.
 * @author: David Rojo, Fernando Campos
 * @version: 19/04/2017
 */
//@EnableDiscoveryClient    //when Eureka available

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
@EnableRabbit
public class MonitoringApplication {
  
  private static Log log = LogFactory.getLog(MonitoringApplication.class);
  
  @Bean
  public DirectExchange appExchange() {
    return new DirectExchange(MonitoringConstants.EXCHANGE_NAME_RH, true, false);
  }
  
  @Bean
  public Queue registrationQueue() {
    return new Queue(MonitoringConstants.MONITORING_REGISTRATION_QUEUE_NAME,true, true, true);
  }
  
  @Bean
  public Queue unegistrationQueue() {
    return new Queue(MonitoringConstants.MONITORING_UNREGISTRATION_QUEUE_NAME,true, true, true);
  }
  
  @Bean
  public Queue sharingQueue() {
    return new Queue(MonitoringConstants.MONITORING_SHARING_QUEUE_NAME,true, true, true);
  }
  
  @Bean
  public Queue unSharingQueue() {
    return new Queue(MonitoringConstants.MONITORING_UNSHARING_QUEUE_NAME,true, true, true);
  }
  
  @Bean
  public Jackson2JsonMessageConverter converter() {
    return new Jackson2JsonMessageConverter();
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

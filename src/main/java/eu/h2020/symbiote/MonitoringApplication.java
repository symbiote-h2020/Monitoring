package eu.h2020.symbiote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;


/**
 * Created by mateuszl on 22.09.2016.
 */
//@EnableDiscoveryClient    //when Eureka available
@EnableAutoConfiguration
@SpringBootApplication
public class MonitoringApplication {

	private static Log log = LogFactory.getLog(MonitoringApplication.class);

	public static void main(String[] args) {
		
		SpringApplication.run(MonitoringApplication.class, args);

        try {
            // Subscribe to RabbitMQ messages
        } catch (Exception e) {
            log.error("Error occured during subscribing from Monitoring", e);
        }
    }

    public static <T> T createFeignClient(Class<T> client, String baseUrl) {
        return Feign.builder().
                encoder(new GsonEncoder()).decoder(new GsonDecoder()).
                target(client,baseUrl);
    }
    


}

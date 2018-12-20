/*
 * Copyright 2018 Atos
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.h2020.symbiote.monitoring;

import eu.h2020.symbiote.util.RabbitConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.DirectExchange;
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

  public static void main(String[] args) {
    WaitForPort.waitForServices(WaitForPort.findProperty("SPRING_BOOT_WAIT_FOR_SERVICES"));
    SpringApplication.run(MonitoringApplication.class, args);

    try {
      // Subscribe to RabbitMQ messages
    } catch (Exception e) {
      log.error("Error occured during subscribing from Monitoring", e);
    }
  }
}

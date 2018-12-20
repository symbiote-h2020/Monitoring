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

package eu.h2020.symbiote.monitoring.tests.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.h2020.symbiote.client.MonitoringClient;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.model.internal.FederationInfoBean;
import eu.h2020.symbiote.cloud.model.internal.ResourceSharingInformation;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.model.mim.QoSMetric;
import eu.h2020.symbiote.monitoring.tests.utils.MonitoringTestUtils;
import eu.h2020.symbiote.monitoring.tests.utils.TestUtils;
import eu.h2020.symbiote.util.RabbitConstants;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class IntegrationTest {
  
  public static void main(String[] args) {

    Properties properties = new Properties();
  
    try {
      properties.load(properties.getClass().getResourceAsStream("test.properties"));
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("localhost");
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();

      String rhExchange = properties.getProperty(RabbitConstants.EXCHANGE_RH_NAME_PROPERTY);

      channel.exchangeDeclare(rhExchange,
              "direct", new Boolean(properties.getProperty(RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY)));
  
      ObjectMapper mapper = new ObjectMapper();
  
      AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder().contentType("application/json").build();

      Map<String, List<CloudResource>> federation = new HashMap<>();
      List<CloudResource> resources = new ArrayList<>();
      CloudResource resource = TestUtils.createResource("res1", true);
      FederationInfoBean fedInfo = new FederationInfoBean();
      fedInfo.setAggregationId(UUID.randomUUID().toString());
      Map<String, ResourceSharingInformation> sharingInfo = new HashMap<>();
      ResourceSharingInformation fedDetails = new ResourceSharingInformation();
      fedDetails.setSymbioteId(UUID.randomUUID().toString());
      fedDetails.setBartering(false);
      fedDetails.setSharingDate(new Date());
      sharingInfo.put("fed1", fedDetails);
      fedInfo.setSharingInformation(sharingInfo);
      resource.setFederationInfo(fedInfo);
      resources.add(resource);

      federation.put("fed1", resources);
      
      channel.basicPublish(rhExchange,
              properties.getProperty(RabbitConstants.ROUTING_KEY_RH_SHARED_PROPERTY), messageProperties,
              mapper.writeValueAsBytes(federation));
  
      MonitoringClient monitoringClient = Feign.builder()
                                              .encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                                              .target(MonitoringClient.class, "http://localhost:8200");
      
      List<DeviceMetric> metrics = new ArrayList<>();
      metrics.add(MonitoringTestUtils.generateMetric("res1", QoSMetric.availability.toString(), 0.0, new Date()));
      
      monitoringClient.postMetrics(metrics);
      
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  
  }
  
}

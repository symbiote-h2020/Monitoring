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

package eu.h2020.symbiote.monitoring.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;
import eu.h2020.symbiote.core.cci.accessNotificationMessages.*;
import eu.h2020.symbiote.monitoring.beans.CloudMonitoringResource;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.MongoDbMonitoringBackend;
import eu.h2020.symbiote.monitoring.tests.utils.TestUtils;
import eu.h2020.symbiote.util.RabbitConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
        "server.port=18036",
        "symbIoTe.core.interface.url=http://localhost:18036/coreInterface/v1/"
})
//@SpringBootTest( webEnvironment = WebEnvironment.DEFINED_PORT, properties = {"eureka.client.enabled=false", "spring.cloud.sleuth.enabled=false", "platform.id=helloid", "server.port=18033", "symbIoTe.core.cloud.interface.url=http://localhost:18033/testiifnosec", "security.coreAAM.url=http://localhost:18033", "security.rabbitMQ.ip=localhost", "security.enabled=false", "symbIoTe.component.username=user", "symbIoTe.component.password=password"})
@Configuration
@ComponentScan
@TestPropertySource(
        locations = "classpath:test.properties")
public class RAPIntegrationTest {

    private static Log logger = LogFactory.getLog(RAPIntegrationTest.class);

    private static final int NUM_RESOURCES = 100;
    private static final int NUM_METRICS_RESOURCE = 100;

    @Value("${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}")
    private String rhExchangeName;

    @Value("${" + RabbitConstants.EXCHANGE_RAP_NAME_PROPERTY + "}")
    private String rapExchangeName;

    @Value("${" + RabbitConstants.ROUTING_KEY_RH_REGISTER_PROPERTY + "}")
    private String resourceRegistrationKey;

    @Value("${" + RabbitConstants.ROUTING_KEY_RAP_ACCESS_PROPERTY + "}")
    private String resourceAccessKey;

    @Value("${monitoring.mongo.database}")
    private String mongoDatabase;

    @Autowired
    private MongoTemplate template;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CloudResourceRepository resourceRepo;

    private MongoDbMonitoringBackend backend;

    private ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("UTC"));

    @Before
    public void setUp() throws JsonProcessingException, InterruptedException {
        template.getDb().dropDatabase();

        backend = new MongoDbMonitoringBackend(null, mongoDatabase,
                template.getCollectionName(CloudMonitoringResource.class));

        List<CloudResource> toAdd = new ArrayList<>();

        for (int i = 0; i < NUM_RESOURCES; i++) {
            toAdd.add(TestUtils.createResource(Integer.toString(i), false));
        }

        TestUtils.sendMessage(rabbitTemplate, rhExchangeName, resourceRegistrationKey, toAdd);

    }

    @Test
    public void testRapIntegration() throws JsonProcessingException, InterruptedException {

        NotificationMessage accessMessage = new NotificationMessage();

        List<SuccessfulAccessMessageInfo> success = createAccessList(0, NUM_RESOURCES/3, (resource -> {
            SuccessfulAccessMessageInfo result = new SuccessfulAccessMessageInfo();
            result.setAccessType(SuccessfulAccessMessageInfo.AccessType.NORMAL.toString());
            result.setSymbIoTeId(resource.getResource().getId());
            return result;
        }));
        accessMessage.setSuccessfulAttempts(success);

        List<SuccessfulPushesMessageInfo> successPush = createAccessList(NUM_RESOURCES/3, (NUM_RESOURCES/3)*2,
                (resource -> {
            SuccessfulPushesMessageInfo result = new SuccessfulPushesMessageInfo();
            result.setSymbIoTeId(resource.getResource().getId());
            return result;
        }));
        accessMessage.setSuccessfulPushes(successPush);

        List<FailedAccessMessageInfo> failed = createAccessList((NUM_RESOURCES/3)*2, NUM_RESOURCES, (resource -> {
            FailedAccessMessageInfo result = new FailedAccessMessageInfo();
            result.setCode("500");
            result.setSymbIoTeId(resource.getResource().getId());
            return result;
        }));
        accessMessage.setFailedAttempts(failed);

        int numMetrics = getMetricsNumber(accessMessage.getSuccessfulAttempts()) +
                getMetricsNumber(accessMessage.getSuccessfulPushes()) +
                getMetricsNumber(accessMessage.getFailedAttempts());

        logger.info("Sending " + numMetrics +" metrics to RAP listener");

        TestUtils.sendMessage(rabbitTemplate, rapExchangeName, resourceAccessKey, accessMessage);

        List<DeviceMetric> rawMetrics = backend.getMetrics(null, null, null, null);

        logger.info("There are " + rawMetrics.size() + " metrics in MongoDB database before wait");

        // A little more time to save metrics
        //TimeUnit.SECONDS.sleep(10);

        Map<String, AggregatedMetrics> allMetrics = backend
                .getAggregatedMetrics(null, null, null, null, null, null)
                .stream().collect(Collectors.toMap(AggregatedMetrics::getDeviceId, metrics -> metrics));

        assert allMetrics != null;
        assert !allMetrics.isEmpty();

        checkAccessList(accessMessage.getSuccessfulAttempts(), allMetrics, 1.0);
        checkAccessList(accessMessage.getSuccessfulPushes(), allMetrics, 1.0);
        checkAccessList(accessMessage.getFailedAttempts(), allMetrics, 0.0);
    }

    private <T extends MessageInfo> int getMetricsNumber(List<T> successfulAttempts) {
        int num = 0;
        for (T attempt : successfulAttempts) {
            num += attempt.getTimestamps().size();
        }
        return num;
    }

    private <T extends MessageInfo> void checkAccessList(List<T> accesses, Map<String, AggregatedMetrics> allMetrics,
                                                         double expected) {
        for (T msg : accesses) {
            CloudResource resource = resourceRepo.findByResourceId(msg.getSymbIoTeId());
            assert resource != null;
            AggregatedMetrics metrics = allMetrics.get(resource.getInternalId());
            assert metrics != null;
            assert metrics.getValues() != null;
            for (Date date : msg.getTimestamps()) {
                assert findValue(date, metrics.getValues()) == expected;
            }
        }
    }

    private Double findValue(Date date, List<TimedValue> values) {
        Optional<TimedValue> value = values.stream().filter(val -> val.getDate().equals(date)).findFirst();
        if (value.isPresent()) {
            return new Double(value.get().getValue());
        } else {
            return null;
        }
    }

    private <T extends MessageInfo> List<T> createAccessList(int resStart, int resEnd, Function<CloudResource, T> factory) {
        ZonedDateTime time = now;
        List<T> result = new ArrayList<>();
        for (int i = resStart; i < resEnd; i++) {
            String resId = Integer.toString(i);
            CloudResource resource = resourceRepo.findOne(resId);
            T access = factory.apply(resource);
            List<Date> dates = new ArrayList<>();
            for (int j = 0; j < NUM_METRICS_RESOURCE ; j++) {
                dates.add(Date.from(time.toInstant()));
                time = time.plusMinutes(1);
            }
            access.setTimestamps(dates);
            result.add(access);
        }
        return result;
    }



}

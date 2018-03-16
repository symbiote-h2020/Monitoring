package eu.h2020.symbiote.monitoring.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.model.internal.FederationInfoBean;
import eu.h2020.symbiote.cloud.model.internal.ResourceSharingInformation;
import eu.h2020.symbiote.monitoring.beans.FederatedDeviceInfo;
import eu.h2020.symbiote.monitoring.beans.FederationInfo;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.FederationInfoRepository;
import eu.h2020.symbiote.monitoring.tests.utils.TestUtils;
import eu.h2020.symbiote.util.RabbitConstants;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
        "server.port=18034",
        "symbiote.coreinterface.url=http://localhost:18034/coreInterface/v1/"
})
//@SpringBootTest( webEnvironment = WebEnvironment.DEFINED_PORT, properties = {"eureka.client.enabled=false", "spring.cloud.sleuth.enabled=false", "platform.id=helloid", "server.port=18033", "symbIoTe.core.cloud.interface.url=http://localhost:18033/testiifnosec", "security.coreAAM.url=http://localhost:18033", "security.rabbitMQ.ip=localhost", "security.enabled=false", "security.user=user", "security.password=password"})
@Configuration
@ComponentScan
@TestPropertySource(
        locations = "classpath:test.properties")
public class MonitoringRabbitTests {

    private static Logger logger = LoggerFactory.getLogger(MonitoringRabbitTests.class);

    @Value("${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}")
    private String rhExchangeName;

    @Value("${" + RabbitConstants.ROUTING_KEY_RH_REGISTER_PROPERTY + "}")
    private String resourceRegistrationKey;

    @Value("${" + RabbitConstants.ROUTING_KEY_RH_DELETE_PROPERTY + "}")
    private String resourceDeleteKey;

    @Value("${" + RabbitConstants.ROUTING_KEY_RH_SHARED_PROPERTY + "}")
    private String resourceShareKey;

    @Value("${" + RabbitConstants.ROUTING_KEY_RH_UNSHARED_PROPERTY + "}")
    private String resourceUnshareKey;

    @Value("${" + RabbitConstants.ROUTING_KEY_RH_DELETED_PROPERTY + "}")
    private String resourceLocalDeleteKey;

    @Autowired
    private CloudResourceRepository resourceRepo;

    @Autowired
    private FederationInfoRepository federationInfoRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MongoTemplate template;

    public static final int NUM_FEDERATIONS = 3;

    public static final int NUM_RESOURCES = 100;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws IOException, TimeoutException {

        federationInfoRepository.deleteAll();
        template.getDb().dropDatabase();
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

    }


    @Test
    public void coreDevicesTest() throws Exception {

        List<CloudResource> toAdd = new ArrayList<>();

        for (int i = 0; i < NUM_RESOURCES; i++) {
            toAdd.add(getResource(Integer.toString(i)));
        }

        sendResourceMessage(resourceRegistrationKey, toAdd);

        List<CloudResource> result = resourceRepo.findAll();

        assertNotNull(result);

        assert result.size() == NUM_RESOURCES;
        result.forEach(resource -> {
            assert Integer.valueOf(resource.getInternalId()) < NUM_RESOURCES;
        });

        List<String> toDelete = new ArrayList<>();


        for (int i = 0; i < NUM_RESOURCES; i++) {
            if (i % 2 == 0) {
                toDelete.add(Integer.toString(i));
            }
        }

        sendResourceMessage(resourceDeleteKey, toDelete);

        result = resourceRepo.findAll();

        assertNotNull(result);

        assert result.size() == NUM_RESOURCES / 2;

        result.forEach(resource -> {
            assert Integer.valueOf(resource.getInternalId()) % 2 == 1;
        });
    }

    private Map<String, List<CloudResource>> addToFederations(int resourceStart, int resourceEnd) throws Exception {

        Map<String, List<CloudResource>> result = new HashMap<>();

        for (int f = 0; f < NUM_FEDERATIONS; f++) {
            String fedId = Integer.toString(f);

            List<CloudResource> resources = new ArrayList<>();

            for (int r = resourceStart; r < resourceEnd; r++) {

                if (r % 2 == f % 2) {
                    CloudResource resource = getResource(Integer.toString(r));
                    FederationInfoBean fedInfo = new FederationInfoBean();
                    fedInfo.setSymbioteId(UUID.randomUUID().toString());
                    ResourceSharingInformation resFedInfo = new ResourceSharingInformation();
                    resFedInfo.setSharingDate(new Date());
                    resFedInfo.setBartering(false);
                    fedInfo.getSharingInformation().put(fedId, resFedInfo);
                    resource.setFederationInfo(fedInfo);
                    resources.add(resource);
                }
            }

            result.put(fedId, resources);
        }

        sendResourceMessage(resourceShareKey, result);

        return result;
    }

    public void testFederationPartial(int resourceStart, int resourceEnd, int totalResources) throws Exception {
        Map<String, List<CloudResource>> created = addToFederations(resourceStart, resourceEnd);

        List<FederationInfo> federations = federationInfoRepository.findAll();

        assertNotNull(federations);

        assert federations.size() == NUM_FEDERATIONS;

        federations.forEach(federation -> {
            int fedId = Integer.valueOf(federation.getFederationId());
            List<CloudResource> fedInfo = created.get(federation.getFederationId());

            assert fedInfo != null;
            assert fedId < NUM_FEDERATIONS;
            Map<String, FederatedDeviceInfo> savedResources = federation.getResources();
            Map<String, CloudResource> createdResources = created.get(federation.getFederationId()).stream()
                    .collect(Collectors.toMap(CloudResource::getInternalId, resource -> resource));

            createdResources.forEach((resourceId, value) -> {
                int resId = Integer.valueOf(resourceId);

                assert resId % 2 == fedId % 2;
                FederatedDeviceInfo savedResource = savedResources.get(resourceId);
                assert savedResource != null;
                ResourceSharingInformation resFedInfo = savedResource.getSharingInformation();
                assert resFedInfo != null;

                FederationInfoBean createdFedInfo = value.getFederationInfo();
                assert createdFedInfo != null;
                assert savedResource.getSymbioteId().equals(createdFedInfo.getSymbioteId());

                ResourceSharingInformation valueFedInfo = createdFedInfo.getSharingInformation().get(federation.getFederationId());
                assert valueFedInfo != null;

                assert resFedInfo.getSharingDate().equals(valueFedInfo.getSharingDate());
                assert resFedInfo.getBartering().equals(valueFedInfo.getBartering());

            });

        });
    }

    @Test
    public void federationDevicesTest() throws Exception {

        //test federation creation with half of the resources
        testFederationPartial(0, NUM_RESOURCES / 2, NUM_RESOURCES / 2);

        //test federation update with the other half
        testFederationPartial(NUM_RESOURCES / 2, NUM_RESOURCES, NUM_RESOURCES);

        // Test unsharing
        Map<String, List<CloudResource>> toDelete = new HashMap<>();
        for (int i = 0; i < NUM_FEDERATIONS; i++) {
            String fedId = Integer.toString(i);
            List<CloudResource> resources = new ArrayList<>();
            toDelete.put(fedId, resources);
            boolean delete = true;
            for (int j = 0; j < NUM_RESOURCES; j++) {
                if (i % 2 == j % 2) {
                    if (delete) {
                        resources.add(TestUtils.createResource(Integer.toString(j), false));
                    }
                    delete = !delete;
                }
            }
        }

        sendResourceMessage(resourceUnshareKey, toDelete);

        List<FederationInfo> federations = federationInfoRepository.findAll();

        assertNotNull(federations);

        assert federations.size() == NUM_FEDERATIONS;

        federations.forEach(federation -> {
            assert federation.getResources().size() == NUM_RESOURCES / 4;
        });

        // Test remove
        List<String> toRemove = federations.stream()
                .map(federation ->
                        federation.getResources().isEmpty()?
                                null:federation.getResources().keySet().iterator().next())
                .filter(id -> id != null).collect(Collectors.toList());

        sendResourceMessage(resourceLocalDeleteKey, toRemove);

        federations = federationInfoRepository.findAll();
        for (FederationInfo federation : federations) {
            for (String resId : toRemove) {
                assert !federation.getResources().keySet().contains(resId);
            }
        }
    }


    private CloudResource getResource(String id) {
        return TestUtils.createResource(id, false);
    }

    void sendResourceMessage(String key, Object toSend) throws Exception {
        TestUtils.sendMessage(rabbitTemplate, rhExchangeName, key, toSend);
    }

}

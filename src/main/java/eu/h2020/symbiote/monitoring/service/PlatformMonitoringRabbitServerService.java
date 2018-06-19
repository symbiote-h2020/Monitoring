package eu.h2020.symbiote.monitoring.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.client.CRAMRestService;
import eu.h2020.symbiote.client.SymbioteComponentClientFactory;
import eu.h2020.symbiote.cloud.model.ResourceLocalSharingMessage;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.core.cci.accessNotificationMessages.MessageInfo;
import eu.h2020.symbiote.core.cci.accessNotificationMessages.NotificationMessage;
import eu.h2020.symbiote.monitoring.beans.CloudMonitoringResource;
import eu.h2020.symbiote.monitoring.beans.FederatedDeviceInfo;
import eu.h2020.symbiote.monitoring.beans.FederationInfo;
import eu.h2020.symbiote.monitoring.constants.MonitoringConstants;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import eu.h2020.symbiote.monitoring.db.FederationInfoRepository;
import eu.h2020.symbiote.monitoring.db.MongoDbMonitoringBackend;
import eu.h2020.symbiote.monitoring.utils.SecurityHandlerManager;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.util.RabbitConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PlatformMonitoringRabbitServerService {

    private static Log logger = LogFactory.getLog(PlatformMonitoringRabbitServerService.class);

    @Value("${monitoring.mongo.uri:#{null}}")
    private String mongoUri;

    @Value("${monitoring.mongo.database:symbiote-cloud-monitoring-database}")
    private String mongoDatabase;

    @Value("${symbIoTe.core.cloud.interface.url}")
    private String coreInterfaceUrl;

    @Value("${platform.id}")
    private String platformId;

    @Autowired
    private MongoTemplate template;

    @Autowired
    CloudResourceRepository coreRepository;

    @Autowired
    FederationInfoRepository federationRepository;

    @Autowired
    private SecurityHandlerManager secHandlerManager;

    private ObjectMapper mapper = new ObjectMapper();

    private MongoDbMonitoringBackend backend;

    private CRAMRestService cramRestService;

    private static final Map<String, String> idMapping = new HashMap<>();

    @PostConstruct
    public void init() throws SecurityHandlerException {
        backend = new MongoDbMonitoringBackend(mongoUri, mongoDatabase,
                template.getCollectionName(CloudMonitoringResource.class));

        cramRestService = SymbioteComponentClientFactory.createClient(coreInterfaceUrl, CRAMRestService.class,
                "cram", SecurityConstants.CORE_AAM_INSTANCE_ID, secHandlerManager.getSecurityHandler());

        List<CloudResource> coreResources = coreRepository.findAll();
        for (CloudResource resource : coreResources) {
            idMapping.put(resource.getResource().getId(), resource.getInternalId());
        }

        List<FederationInfo> federations = federationRepository.findAll();
        for (FederationInfo info : federations) {
            for (Map.Entry<String,FederatedDeviceInfo> deviceInfo : info.getResources().entrySet()) {
                idMapping.put(deviceInfo.getValue().getSymbioteId(), deviceInfo.getKey());
            }
        }
    }

    /**
     * Spring AMQP Listener for resource registration requests. This method is invoked when Registration
     * Handler sends a resource registration request and it is responsible for forwarding the message
     * to the symbIoTe core. As soon as it receives a reply, it manually sends back the response
     * to the Registration Handler via the appropriate message queue by the use of the RestAPICallback.
     *
     * @param message List of resources to add process
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MonitoringConstants.MONITORING_REGISTRATION_QUEUE_NAME, durable = "true",
                    exclusive = "false", autoDelete = "true"),
            exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}",
                    type = ExchangeTypes.DIRECT, durable = "${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}"),
            key = "${" + RabbitConstants.ROUTING_KEY_RH_REGISTER_PROPERTY + "}"))
    public void resourceRegistration(@Payload Message message) {
        try {
            logger.debug("ResourceRegistration");
            List<CloudResource> resources = toList(message, new TypeReference<List<CloudResource>>() {
            });
            coreRepository.save(resources);
            for (CloudResource resource : resources) {
                if (resource.getResource() != null) {
                    idMapping.put(resource.getResource().getId(), resource.getInternalId());
                }
            }
        } catch (Throwable t) {
            logger.warn("Throwable in resourceRegistration listener", t);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MonitoringConstants.MONITORING_UNREGISTRATION_QUEUE_NAME, durable = "true",
                    exclusive = "false", autoDelete = "true"),
            exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}",
                    type = ExchangeTypes.DIRECT, durable = "${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}"),
            key = "${" + RabbitConstants.ROUTING_KEY_RH_DELETE_PROPERTY + "}"))
    public void resourceUnregistration(@Payload Message message) {
        try {
            logger.debug("resourceUnregistration");

            List<String> resources = toList(message, new TypeReference<List<String>>() {
            });
            for (String resourceId : resources) {
                CloudResource res = coreRepository.findOne(resourceId);
                if (res != null) {
                    if (res.getResource() != null) {
                        idMapping.remove(res.getResource().getId());
                    }
                    coreRepository.delete(res);
                }
            }
        } catch (Throwable t) {
            logger.warn("Throwable in resourceUnregistration listener", t);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MonitoringConstants.MONITORING_SHARING_QUEUE_NAME, durable = "true",
                    exclusive = "false", autoDelete = "true"),
            exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}",
                    type = ExchangeTypes.DIRECT, durable = "${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}"),
            key = "${" + RabbitConstants.ROUTING_KEY_RH_SHARED_PROPERTY + "}"))
    public void resourceSharing(@Payload Message message) {
        try {
            logger.debug("resourceSharing");

            Map<String, List<CloudResource>> resources = toObject(message, ResourceLocalSharingMessage.class).getSharingMap();

            for (String federation : resources.keySet()) {
                FederationInfo fedInfo = federationRepository.findOne(federation);

                if (fedInfo == null) {
                    fedInfo = new FederationInfo();
                    fedInfo.setFederationId(federation);
                }

                for (CloudResource resource : resources.get(federation)) {

                    if (resource.getFederationInfo() != null
                            && resource.getFederationInfo().getSharingInformation() != null
                            && resource.getFederationInfo().getSharingInformation().get(federation) != null) {

                        FederatedDeviceInfo resourceInfo = new FederatedDeviceInfo();
                        String type = resource.getResource().getClass().getSimpleName();
                        resourceInfo.setType(type);
                        resourceInfo.setSymbioteId(
                                resource.getFederationInfo().getSharingInformation().get(federation).getSymbioteId());
                        resourceInfo.setSharingInformation(
                                resource.getFederationInfo().getSharingInformation().get(federation));
                        fedInfo.getResources().put(resource.getInternalId(), resourceInfo);

                        idMapping.put(resourceInfo.getSymbioteId(), resource.getInternalId());

                    } else {
                        logger.error("Error saving L2 information of resource " + resource.getInternalId()
                                + ": Federation " + federation +" information is missing in resource metadata");
                    }
                }

                federationRepository.save(fedInfo);
            }
        } catch (Throwable t) {
            logger.warn("Throwable in resourceSharing listener", t);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MonitoringConstants.MONITORING_UNSHARING_QUEUE_NAME, durable = "true",
                    exclusive = "false", autoDelete = "true"),
            exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}",
                    type = ExchangeTypes.DIRECT, durable = "${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}"),
            key = "${" + RabbitConstants.ROUTING_KEY_RH_UNSHARED_PROPERTY + "}"))
    public void resourceUnsharing(@Payload Message message) {

        try {
            logger.debug("resourceUnsharing");

            Map<String, List<CloudResource>> resources = toObject(message, ResourceLocalSharingMessage.class).getSharingMap();

            for (String federation : resources.keySet()) {
                FederationInfo fedInfo = federationRepository.findOne(federation);

                if (fedInfo != null) {
                    List<CloudResource> fedResources = resources.get(federation);
                    for (CloudResource resource : fedResources) {
                        FederatedDeviceInfo resInfo = fedInfo.getResources().get(resource.getInternalId());
                        if (resInfo != null) {
                            fedInfo.getResources().remove(resource.getInternalId());
                            federationRepository.save(fedInfo);
                            if (fedInfo.getResources().isEmpty()) {
                                idMapping.remove(resInfo.getSymbioteId());
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.warn("Throwable in resourceUnsharing listener", t);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MonitoringConstants.MONITORING_UNREGISTRATION_LOCAL_QUEUE_NAME, durable = "true",
                    exclusive = "false", autoDelete = "true"),
            exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}",
                    type = ExchangeTypes.DIRECT, durable = "${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}"),
            key = "${" + RabbitConstants.ROUTING_KEY_RH_DELETED_PROPERTY + "}"))
    public void resourceRemoveLocal(@Payload Message message) {

        try {
            logger.debug("resourceRemoveLocal");

            List<String> toRemove = toList(message, new TypeReference<List<String>>() {
            });
            List<FederationInfo> toDelete = new ArrayList<>();
            List<FederationInfo> toUpdate = new ArrayList<>();
            List<FederationInfo> federations = federationRepository.findAll();
            for (FederationInfo federation : federations) {
                for (String device : toRemove) {
                    federation.getResources().remove(device);
                }
                if (federation.getResources().isEmpty()) {
                    toDelete.add(federation);
                } else {
                    toUpdate.add(federation);
                }
            }

            if (!toDelete.isEmpty()) {
                federationRepository.delete(toDelete);
            }

            if (!toUpdate.isEmpty()) {
                federationRepository.save(toUpdate);
            }
        } catch (Throwable t) {
            logger.warn("Throwable in resourceRemoveLocal listener", t);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MonitoringConstants.MONITORING_RESOURCE_ACCESS_QUEUE_NAME, durable = "true",
                    exclusive = "false", autoDelete = "true"),
            exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RAP_NAME_PROPERTY + "}",
                    type = ExchangeTypes.DIRECT, durable = "${" + RabbitConstants.EXCHANGE_RAP_DURABLE_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_RAP_AUTODELETE_PROPERTY + "}"),
            key = "${" + RabbitConstants.ROUTING_KEY_RAP_ACCESS_PROPERTY+ "}"))
    public void insertMetrics(@Payload Message message) {

        try {
            logger.debug("insertMetrics");
            logger.debug(message);
            NotificationMessage accessMessage = toObject(message, NotificationMessage.class);

            List<DeviceMetric> toInsert = new ArrayList<>();
            toInsert.addAll(getMetrics(accessMessage.getSuccessfulAttempts(), true));
            toInsert.addAll(getMetrics(accessMessage.getSuccessfulPushes(), true));
            toInsert.addAll(getMetrics(accessMessage.getFailedAttempts(), false));

            logger.debug("Got " + toInsert.size() + " metrics from RAP");

            backend.saveMetrics(toInsert);

            List<String> coreResources = coreRepository.findAll().stream().map(cloudResource ->
                    cloudResource.getResource().getId()).collect(Collectors.toList());

            accessMessage.setSuccessfulAttempts(filterL2Access(coreResources, accessMessage.getSuccessfulAttempts()));
            accessMessage.setSuccessfulPushes(filterL2Access(coreResources, accessMessage.getSuccessfulPushes()));
            accessMessage.setFailedAttempts(filterL2Access(coreResources, accessMessage.getFailedAttempts()));

            cramRestService.publishAccessData(accessMessage);
        } catch (Throwable t) {
            logger.debug("Throwable in insertMetrics listener", t);
        }
    }

    private <T extends MessageInfo> List<T> filterL2Access(List<String> coreResources, List<T> attempts) {

        return attempts.stream().filter(attempt -> coreResources.contains(attempt.getSymbIoTeId()))
                .collect(Collectors.toList());
    }

    private <T extends MessageInfo> List<DeviceMetric>  getMetrics(List<T> inputs, boolean success) {
        logger.debug("getMetrics with idMappings = " + idMapping);

        List<DeviceMetric> result = new ArrayList<>();
        for (T message : inputs) {
            logger.debug(message.getSymbIoTeId());
            String internalId = idMapping.get(message.getSymbIoTeId());
            logger.debug(internalId);

            if (internalId != null) {
                for (Date date : message.getTimestamps()) {
                    DeviceMetric metric = new DeviceMetric();
                    metric.setDeviceId(internalId);
                    metric.setDate(date);
                    metric.setTag(MonitoringConstants.AVAILABILITY_TAG);
                    metric.setValue((success)?"1":"0");
                    result.add(metric);
                }
            }
        }
        return result;
    }

    private <T> T toList(Message message, TypeReference<T> reference) {
        try {
            return mapper.readValue(message.getBody(), reference);
        } catch (IOException e) {
            logger.warn("Invalid JSON message received: " + message.getBody(), e);
        }

        return (T) new ArrayList<>();
    }

    private <T> T toObject(Message message, Class<T> clazz) {
        try {
            return mapper.readValue(message.getBody(), clazz);
        } catch (IOException e) {
            logger.warn("Invalid JSON message received: " + message.getBody(), e);
        }

        return null;
    }
}

package eu.h2020.symbiote.service;

import com.mongodb.bulk.BulkWriteResult;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.beans.FederationInfo;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.CloudResourceRepository;
import eu.h2020.symbiote.db.FederationInfoRepository;
import eu.h2020.symbiote.db.MetricsRepository;
import eu.h2020.symbiote.db.MongoDbMonitoringBackend;
import eu.h2020.symbiote.db.ResourceMetricsRepository;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

/**
 * Manage the REST operations from Platform using MongoDB 
 * @author: Fernando Campos, Jose Antonio Sanchez
 * @version: 19/04/2017
 */
@RestController
@RequestMapping("/")
public class PlatformMonitoringRestService {
	
	private static final Log logger = LogFactory.getLog(PlatformMonitoringRestService.class);
	
	@Value("${monitoring.mongo.uri:#{null}}")
	private String mongoUri;
	
	@Value("${monitoring.mongo.database:#{symbiote-cloud-monitoring-database}}")
	private String mongoDatabase;
	
	@Autowired
	private ResourceMetricsRepository resourceMetricsRepository;
	
	@Autowired
	private MetricsRepository metricsRepository;
	
	@Autowired
	private CloudResourceRepository coreRepository;
	
	@Autowired
	private FederationInfoRepository federationInfoRepository;
	
	@Autowired
	private MongoTemplate template;
	
	@Autowired
	private MetricsProcessor processor;
	
	private MongoDbMonitoringBackend backend;
	
	@PostConstruct
	public void init() {
		backend = new MongoDbMonitoringBackend(mongoUri, mongoDatabase,
				template.getCollectionName(CloudMonitoringResource.class));
	}
	
	/**
	 * Listen from Platform Host. Received device monitoring data
	 */
	@RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.METRICS_DATA, produces = "application/json", consumes = "application/json")
	public @ResponseBody
	List<DeviceMetric> saveMetrics(@RequestBody List<DeviceMetric> metrics) throws Throwable {
		
		
		
		List<DeviceMetric> coreMetrics = new ArrayList<>();
		Map<String, CloudResource> resourceCache = new HashMap<>();
		
		metrics.forEach(metric -> {
			String deviceId = metric.getDeviceId();
			CloudResource resource = resourceCache.get(deviceId);
			if (resource == null) {
				resource = coreRepository.findOne(deviceId);
				resourceCache.put(deviceId, resource);
			}
			
			if (resource != null && resource.getResource() != null) {
				coreMetrics.add(metric);
			}
			
		});
		
		
		BulkWriteResult insertResult;
		
		if (!coreMetrics.isEmpty()) {
			template.bulkOps(BulkOperations.BulkMode.UNORDERED, DeviceMetric.class).insert(coreMetrics)
					.execute();
		}
		
		return backend.saveMetrics(metrics);
	}
	
	private String getDateWithoutTime(Date input) {
		return DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC")).format(input.toInstant());
	}
	
	private Date getDate(String isoDateTime) {
		if (isoDateTime != null) {
			return Date.from(DateTimeFormatter.ISO_INSTANT.parse(isoDateTime, Instant::from));
		} else {
			return null;
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, path = MonitoringConstants.METRICS_DATA, produces = "application/json", consumes = "application/json")
	public @ResponseBody
	List<DeviceMetric> getMetrics(@RequestParam(value = "device", required = false) String device,
																@RequestParam(value = "metric", required = false) String metric,
																/*
																 * Bug https://github.com/JodaOrg/joda-time/issues/443 which I still consider a bug, prevents automatic deserialization.
																 * If it's fixed in the future, we might rely on DateTimeFormat annotation.
																 */
																@RequestParam(value = "startDate", required = false) String startDateStr,
																@RequestParam(value = "endDate", required = false) String endDateStr) throws Throwable {
		
		
		Date startDate = getDate(startDateStr);
		Date endDate = getDate(endDateStr);
		return backend.getMetrics(Arrays.asList(device), Arrays.asList(metric), startDate, endDate);
	}
	
	public Map<String, Double> getAggregatedMetric(String federationId, String inputMetric) {
		
		if (!StringUtils.isBlank(federationId) && !StringUtils.isBlank(inputMetric)) {
			String metric = inputMetric.toLowerCase();
			String tag = null;
			String type = null;
			String duration = null;
			
			String[] metricPath = metric.split(".");
			int length = metricPath.length;
			
			switch (length) {
				case 1:
					tag = metric;
					duration = MonitoringConstants.ALL_QUALIFIER;
					break;
				case 2:
					tag = metricPath[0];
					duration = metricPath[1];
					break;
				default:
					tag = metricPath[0];
					type = metricPath[1];
					duration = metricPath[2];
					break;
			}
			
			if (MonitoringConstants.AVAILABILITY_TAG.equals(tag) ||
							MonitoringConstants.LOAD_TAG.equals(tag)) {
				
				if (MonitoringConstants.ALL_QUALIFIER.equals(duration) || StringUtils.isNumeric(duration)) {
					
					FederationInfo federationInfo = federationInfoRepository.findOne(federationId);
					if (federationInfo != null) {
						return getAggregatedMetric(federationInfo, tag, duration, type);
					} else {
						logger.error("Can't find information of federation " + federationId);
					}
					
				} else {
					logger.error("Non numeric or all duration " + duration);
				}
				
			} else {
				logger.error("Received unknown metric " + metric);
			}
			
		} else {
			logger.error("Received blank metric: " + inputMetric + "or federation id: " + federationId);
		}
		
		return null;
	}
	
	private Map<String, Double> getAggregatedMetric(FederationInfo federationInfo, String tag, String duration, String type) {
		
		List<String> devices = null;
		if (type != null) {
			devices = federationInfo.getResources().entrySet().stream()
										.filter(entry -> type.equals(entry.getValue().getValue()))
										.map(entry -> entry.getKey()).collect(Collectors.toList());
		} else {
			devices = federationInfo.getResources().keySet().stream().collect(Collectors.toList());
		}
		
		Date startDate = null;
		Date endDate = new Date();
		
		if (!MonitoringConstants.ALL_QUALIFIER.equals(duration)) {
			int days = Integer.valueOf(duration);
			
			Instant start = Instant.now().atZone(ZoneId.of("UTC")).minusDays(days).toInstant();
			startDate = Date.from(start);
		}
		
		List<DeviceMetric> metrics = backend.getMetrics(devices, Arrays.asList(tag), startDate, endDate);
		
		return processMetrics(federationInfo, metrics, tag);
		
	}
	
	private Map<String,Double> processMetrics(FederationInfo federationInfo, List<DeviceMetric> metrics, String tag) {
		
		Map<String, List<Double>> values = new HashMap<>();
		metrics.forEach(metric -> {
			String devId = metric.getDeviceId();
			Date sharingDate = federationInfo.getResources().get(devId).getDate();
			if (metric.getDate().after(sharingDate) || metric.getDate().equals(sharingDate)) {
				List<Double> metricValues = values.get(devId);
				if (metricValues == null) {
					metricValues = new ArrayList<>();
					values.put(devId, metricValues);
				}
				metricValues.add(Double.valueOf(metric.getValue()));
			}
		});
		
		return values.entrySet().stream().collect(
				Collectors.toMap(
						entry -> entry.getKey(),
						entry -> {
							if (MonitoringConstants.LOAD_TAG.equals(tag)) {
								return entry.getValue().stream().mapToDouble(d -> d).average().getAsDouble();
							} else {
								
								return 0.0;
							}
						}));

		
	}
}

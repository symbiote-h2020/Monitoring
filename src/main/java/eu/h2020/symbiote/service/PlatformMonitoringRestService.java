package eu.h2020.symbiote.service;

import com.mongodb.bulk.BulkWriteResult;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.CloudResourceRepository;
import eu.h2020.symbiote.db.MetricsRepository;
import eu.h2020.symbiote.db.MongoDbMonitoringBackend;
import eu.h2020.symbiote.db.ResourceMetricsRepository;

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
	
	public double getAggregatedMetric(String federationId, String metric) {
		return 0;
	}
}

package eu.h2020.symbiote.service;

import com.google.gson.Gson;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.beans.DayMetricList;
import eu.h2020.symbiote.beans.DeviceMetricList;
import eu.h2020.symbiote.beans.FederationInfo;
import eu.h2020.symbiote.beans.MetricValue;
import eu.h2020.symbiote.beans.MonitoringMetric;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.CloudResourceRepository;
import eu.h2020.symbiote.db.FederationInfoRepository;
import eu.h2020.symbiote.db.MetricsRepository;
import eu.h2020.symbiote.db.ResourceMetricsRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manage the REST operations from Platform using MongoDB 
 * @author: Fernando Campos, Jose Antonio Sanchez
 * @version: 19/04/2017
 */
@RestController
@RequestMapping("/")
public class PlatformMonitoringRestService {
	
	private static final Log logger = LogFactory.getLog(PlatformMonitoringRestService.class);
	
	@Autowired
	private ResourceMetricsRepository resourceMetricsRepository;
	
	@Autowired
	private FederationInfoRepository monitoringDeviceRepository;
	
	@Autowired
	private MetricsRepository metricsRepository;
	
	@Autowired
	private CloudResourceRepository cloudResourceRepository;
	
	@Autowired
	private MongoTemplate template;
	
	@Autowired
	private MetricsProcessor processor;
	
	/**
	 * Listen from Platform Host. Received device monitoring data
	 */
	@RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.METRICS_DATA, produces = "application/json", consumes = "application/json")
	public @ResponseBody
	List<DeviceMetric> saveMetrics(@RequestBody List<DeviceMetric> metrics) throws Throwable {
		
		FederationInfo coreInfo = monitoringDeviceRepository.findByFederationId(MonitoringConstants.CORE_FED_ID);
		
		List<String> coreDevices = coreInfo.getDevices();
		
		List<DeviceMetric> updated = new ArrayList<>();
		
		metrics.forEach(metric -> {
			
			if (coreDevices.contains(metric.getDeviceId())) {
				updated.add(metricsRepository.save(new MonitoringMetric(metric)).getMetric());
			}
			
			addMetricToDevice(metric);
			
		});
		
		return updated;
	}
	
	private String getDateWithoutTime(Date input) {
		return DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC")).format(input.toInstant());
	}
	
	private Date getDate(String isoDateTime) {
		return Date.from(DateTimeFormatter.ISO_INSTANT.parse(isoDateTime, Instant::from));
	}
	
	private CloudMonitoringResource createResourceDocument(DeviceMetric metric) {
		CloudMonitoringResource input = new CloudMonitoringResource();
		input.setResource(metric.getDeviceId());
		
		List<DeviceMetricList> deviceMetrics = new ArrayList<>();
		
		DeviceMetricList deviceMetric = new DeviceMetricList();
		deviceMetric.setMetric(metric.getTag());
		
		List<DayMetricList> dayMetrics = new ArrayList<>();
		
		DayMetricList dayMetricsList = new DayMetricList();
		dayMetricsList.setDay(getDateWithoutTime(metric.getDate()));
		
		List<MetricValue> listValues = new ArrayList<>();
		MetricValue metricValue = new MetricValue();
		metricValue.setDate(metric.getDate());
		metricValue.setValue(metric.getValue());
		
		listValues.add(metricValue);
		dayMetricsList.setValues(listValues);
		
		dayMetrics.add(dayMetricsList);
		
		deviceMetric.setMetricValues(dayMetrics);
		
		deviceMetrics.add(deviceMetric);
		
		input.setMetrics(deviceMetrics);
		
		return input;
	}
	
	private boolean addMetricToDevice(DeviceMetric metric) {
		
		String metricString = new Gson().toJson(metric);
		
		logger.debug("Saving metric " + metricString);
		
		
		
		String day = getDateWithoutTime(metric.getDate());
		
		Bson query = new BasicDBObject().append("_id",metric.getDeviceId());
		
		BSONObject valueObject = new BasicBSONObject();
		valueObject.put("date", metric.getDate());
		valueObject.put("value", metric.getValue());
		
		Bson update = new BasicDBObject().append("$push", new BasicDBObject().append("deviceMetrics.$[i].metricValues.$[j].values", valueObject));
		
		UpdateOptions options = new UpdateOptions();
		
		List<Bson> arrayFilters = new ArrayList<>();
		arrayFilters.add(new BasicDBObject().append("i.metric", metric.getTag()));
		arrayFilters.add(new BasicDBObject().append("j.day", day));
		options.arrayFilters(arrayFilters);
		
		MongoCollection collection = template.getDb().getCollection(template.getCollectionName(CloudMonitoringResource.class));
		
		UpdateResult result = collection.updateOne(query, update, options);
		
		if (result.getModifiedCount() == 0) {
			//Resource, metric or day doesn't exist. Let's try to add a day
			CloudMonitoringResource resource = createResourceDocument(metric);
			logger.debug("Trying by creating the day of " + metricString);
			
			Query resourceQuery = new Query(Criteria.where("resourceId").is(metric.getDeviceId()).and("deviceMetrics.metric").is(metric.getTag()));
			
			Update dayUpdate = new Update().push("deviceMetrics.$.metricValues").each(resource.getMetrics().get(0).getMetricValues());
			
			result = template.updateFirst(resourceQuery, dayUpdate, CloudMonitoringResource.class);
			
			if (result.getModifiedCount() == 0) {
				// Maybe metric doesn't exist either. Let's try to insert it
				
				logger.debug("Trying by creating the metric of " + metricString);
				
				resourceQuery = new Query(Criteria.where("resourceId").is(metric.getDeviceId()));
				
				Update metricUpdate = new Update().push("deviceMetrics").each(resource.getMetrics());
				
				result = template.updateFirst(resourceQuery, metricUpdate, CloudMonitoringResource.class);
				
				if (result.getModifiedCount() == 0) {
					// OK, it's the first time we see this device, let's initialize it
					
					logger.debug("Trying by creating the a resource for " + metricString);
					CloudMonitoringResource saved = resourceMetricsRepository.insert(resource);
					return saved != null;
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, path = MonitoringConstants.METRICS_DATA, produces = "application/json", consumes = "application/json")
	public @ResponseBody
	List<DeviceMetric> getMetrics(@RequestParam(value = "device", required = false) String device,
																@RequestParam(value = "type", required = false) String type,
																@RequestParam(value = "metric", required = false) String metric,
																/*
																 * Bug https://github.com/JodaOrg/joda-time/issues/443 which I still consider a bug, prevents automatic deserialization.
																 * If it's fixed in the future, we might rely on DateTimeFormat annotation.
																*/
																@RequestParam(value = "startDate", required = false) String startDateStr,
																@RequestParam(value = "endDate", required = false) String endDateStr) throws Throwable {
		
		List<AggregationOperation> list = new ArrayList<AggregationOperation>();
		
		if (device != null) {
			list.add(Aggregation.match(Criteria.where("resourceId").is(device)));
		} else {
			
			if (type != null) {
				Set<String> deviceIds = new HashSet<>();
				deviceIds.addAll(cloudResourceRepository.findByParamsType(type).stream()
														 .map(resource -> resource.getInternalId()).collect(Collectors.toList()));
				list.add(Aggregation.match(Criteria.where("resourceId").in(deviceIds)));
			}
			
		}
		
		list.add(Aggregation.unwind("deviceMetrics"));
		
		if (metric != null) {
			list.add(Aggregation.match(Criteria.where("deviceMetrics.metric").is(metric)));
		}
		
		list.add(Aggregation.unwind("deviceMetrics.metricValues"));
		
		if (startDateStr != null || endDateStr != null) {
			
			Date startDate = null;
			Date endDate = null;
			
			if (startDateStr != null) {
				startDate = getDate(startDateStr);
				String startDay = getDateWithoutTime(startDate);
				list.add(Aggregation.match(Criteria.where("deviceMetrics.metricValues.day").gte(startDay)));
			}
			
			if (endDateStr != null) {
				endDate = getDate(endDateStr);
				String endDay = getDateWithoutTime(endDate);
				list.add(Aggregation.match(Criteria.where("deviceMetrics.metricValues.day").lte(endDay)));
			}
			
			list.add(Aggregation.unwind("deviceMetrics.metricValues.values"));
			
			if (startDate != null) {
				list.add(Aggregation.match(Criteria.where("deviceMetrics.metricValues.values.date").gte(startDate)));
			}
			
			if (endDate != null) {
				list.add(Aggregation.match(Criteria.where("deviceMetrics.metricValues.values.date").lte(endDate)));
			}
		} else {
			list.add(Aggregation.unwind("deviceMetrics.metricValues.values"));
		}
		
		list.add(Aggregation.project("resourceId")
				.and("resourceId").as("deviceId")
								 .and("deviceMetrics.metric").as("tag")
								 .and("deviceMetrics.metricValues.values.date").as("date")
								 .and("deviceMetrics.metricValues.values.value").as("value"));
		
		TypedAggregation<CloudMonitoringResource> agg = Aggregation.newAggregation(CloudMonitoringResource.class, list);
		
		List<DeviceMetric> metrics = template.aggregate(agg, CloudMonitoringResource.class, DeviceMetric.class).getMappedResults();
		
		return metrics;
	}
}

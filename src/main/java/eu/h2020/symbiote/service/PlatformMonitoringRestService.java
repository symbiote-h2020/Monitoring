package eu.h2020.symbiote.service;

import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.beans.FederationInfo;
import eu.h2020.symbiote.beans.MetricValue;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.CloudResourceRepository;
import eu.h2020.symbiote.db.FederationInfoRepository;
import eu.h2020.symbiote.db.MetricsRepository;
import eu.h2020.symbiote.db.ResourceMetricsRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
		
		Map<String, CloudMonitoringResource> resources = new HashMap<>();
		
		FederationInfo coreInfo = monitoringDeviceRepository.findByFederationId(MonitoringConstants.CORE_FED_ID);
		
		List<String> coreDevices = coreInfo.getDevices();
		
		List<DeviceMetric> coreMetrics = new ArrayList<>();
		
		List<Pair<Query, Update>> updates = new ArrayList<>();
		
		BulkOperations bulkOps = template.bulkOps(BulkOperations.BulkMode.ORDERED, CloudMonitoringResource.class);
		
		metrics.forEach(metric -> {
			
			if (coreDevices.contains(metric.getDeviceId())) {
				coreMetrics.add(metric);
			}
			
			addToResource(resources, metric);
			
		});
		
		resources.values().forEach(resource -> {
			String resourceId = resource.getResource();
			resource.getMetrics().forEach((metric, days) -> {
				days.forEach((day, values) -> {
					
					Query query = new Query(Criteria.where("resourceId").is(resourceId));
					Update update = new Update().push("deviceMetrics."+metric+"."+day).each(values);
					Pair<Query, Update> updatePair = Pair.of(query, update);
					updates.add(updatePair);
				});
			});
		});
		
		BulkWriteResult insertResult;
		
		if (!coreMetrics.isEmpty()) {
			insertResult = template.bulkOps(BulkOperations.BulkMode.ORDERED, DeviceMetric.class)
												 .insert(coreMetrics).execute();
		}
		
		if (!updates.isEmpty()) {
			BulkWriteResult updateResult = bulkOps.upsert(updates).execute();
		}
		
		return coreMetrics;
	}
	
	private String getDateWithoutTime(Date input) {
		return DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC")).format(input.toInstant());
	}
	
	private Date getDate(String isoDateTime) {
		return Date.from(DateTimeFormatter.ISO_INSTANT.parse(isoDateTime, Instant::from));
	}
	
	private void addToResource(Map<String, CloudMonitoringResource> resources, DeviceMetric metric) {
		
		CloudMonitoringResource resource = resources.get(metric.getDeviceId());
		if (resource == null) {
			resource = new CloudMonitoringResource();
			resource.setResource(metric.getDeviceId());
			resources.put(metric.getDeviceId(), resource);
		}
		
		Map<String, List<MetricValue>> metricValues = resource.getMetrics().get(metric.getTag());
		if (metricValues == null) {
			metricValues = new HashMap<>();
			resource.getMetrics().put(metric.getTag(), metricValues);
		}
		
		String day = getDateWithoutTime(metric.getDate());
		List<MetricValue> dayMetricValues = metricValues.get(day);
		if (dayMetricValues == null) {
			dayMetricValues = new ArrayList<>();
			metricValues.put(day, dayMetricValues);
		}
		
		MetricValue value = new MetricValue();
		value.setDate(metric.getDate());
		value.setValue(metric.getValue());
		
		dayMetricValues.add(value);
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
		
		List<Bson> pipeline = new ArrayList<>();
		
		//List<AggregationOperation> list = new ArrayList<AggregationOperation>();
		
		if (device != null) {
			pipeline.add(new BasicDBObject().append("$match",new BasicDBObject("_id", device)));
			//list.add(Aggregation.match(Criteria.where("resourceId").is(device)));
		} else {
			
			if (type != null) {
				Set<String> deviceIds = new HashSet<>();
				deviceIds.addAll(cloudResourceRepository.findByParamsType(type).stream()
														 .map(resource -> resource.getInternalId()).collect(Collectors.toList()));
				pipeline.add(new BasicDBObject().append("$match",new BasicDBObject("_id",
						new BasicDBObject().append("$in", deviceIds))));
				//list.add(Aggregation.match(Criteria.where("resourceId").in(deviceIds)));
			}
			
		}
		
		pipeline.add(new BasicDBObject().append("$project",
				new BasicDBObject().append("deviceId","$_id").append("deviceMetrics",
						new BasicDBObject().append("$objectToArray", "$deviceMetrics"))));
		
		if (metric != null) {
			String[] match = new String[]{"$$metric.k",metric};
			pipeline.add(new BasicDBObject().append("$project",
					new BasicDBObject().append("deviceId", 1).append("deviceMetrics",
							new BasicDBObject().append("$filter",
									new BasicDBObject()
											.append("input", "$deviceMetrics")
											.append("as", "metric")
											.append("cond",
													new BasicDBObject().append("$eq", match))))));
		}
		
		pipeline.add(new BasicDBObject().append("$unwind", "$deviceMetrics"));
		
		pipeline.add(new BasicDBObject().append("$project",
				new BasicDBObject()
						.append("deviceId", 1)
						.append("tag", "$deviceMetrics.k")
						.append("dayValues",
								new BasicDBObject().append("$objectToArray", "$deviceMetrics.v"))));
		//list.add(Aggregation.unwind("deviceMetrics.metricValues"));
		
		if (startDateStr != null || endDateStr != null) {
			
			Date startDate = null;
			Date endDate = null;
			
			
			
			if (startDateStr != null) {
				startDate = getDate(startDateStr);
				String startDay = getDateWithoutTime(startDate);
				//list.add(Aggregation.match(Criteria.where("deviceMetrics.metricValues.day").gte(startDay)));
				String[] match = new String[]{"$$day.k",startDay};
				pipeline.add(new BasicDBObject().append("$project",
						new BasicDBObject()
								.append("deviceId", 1)
								.append("tag", 1)
								.append("dayValues",
								new BasicDBObject().append("$filter",
										new BasicDBObject()
												.append("input", "$dayValues")
												.append("as", "day")
												.append("cond",
														new BasicDBObject().append("$gte", match))))));
			}
			
			if (endDateStr != null) {
				endDate = getDate(endDateStr);
				String endDay = getDateWithoutTime(endDate);
				//list.add(Aggregation.match(Criteria.where("deviceMetrics.metricValues.day").lte(endDay)));
				String[] match = new String[]{"$$day.k",endDay};
				pipeline.add(new BasicDBObject().append("$project",
						new BasicDBObject()
								.append("deviceId", 1)
								.append("tag", 1)
								.append("dayValues",
										new BasicDBObject().append("$filter",
												new BasicDBObject()
														.append("input", "$dayValues")
														.append("as", "day")
														.append("cond",
																new BasicDBObject().append("$lte", match))))));
			}
			
			//list.add(Aggregation.unwind("deviceMetrics.metricValues.values"));
			pipeline.add(new BasicDBObject().append("$unwind", "$dayValues"));
			
			
			if (startDate != null) {
				//list.add(Aggregation.match(Criteria.where("deviceMetrics.metricValues.values.date").gte(startDate)));
				Object[] match = new Object[]{"$$value.k",startDate};
				pipeline.add(new BasicDBObject().append("$project",
						new BasicDBObject()
								.append("deviceId", 1)
								.append("tag", 1)
								.append("dayValues.v",
										new BasicDBObject().append("$filter",
												new BasicDBObject()
														.append("input", "$dayValues.v")
														.append("as", "value")
														.append("cond",
																new BasicDBObject().append("$gte", match))))));
			}
			
			if (endDate != null) {
				//list.add(Aggregation.match(Criteria.where("deviceMetrics.metricValues.values.date").lte(endDate)));
				Object[] match = new Object[]{"$$value.k",endDate};
				pipeline.add(new BasicDBObject().append("$project",
						new BasicDBObject()
								.append("deviceId", 1)
								.append("tag", 1)
								.append("dayValues.v",
										new BasicDBObject().append("$filter",
												new BasicDBObject()
														.append("input", "$dayValues.v")
														.append("as", "value")
														.append("cond",
																new BasicDBObject().append("$lte", match))))));
			}
			
		} else {
			//list.add(Aggregation.unwind("deviceMetrics.metricValues.values"));
			pipeline.add(new BasicDBObject().append("$unwind", "$dayValues"));
		}
		
		pipeline.add(new BasicDBObject().append("$project",
				new BasicDBObject()
						.append("deviceId", 1)
						.append("tag", 1)
						.append("values", "$dayValues.v")));
		
		pipeline.add(new BasicDBObject().append("$unwind", "$values"));
		
		pipeline.add(new BasicDBObject().append("$project",
				new BasicDBObject()
						.append("deviceId", 1)
						.append("tag", 1)
						.append("date", "$values.date")
						.append("value", "$values.value")));
		
		/*list.add(Aggregation.project("resourceId")
				.and("resourceId").as("deviceId")
								 .and("deviceMetrics.metric").as("tag")
								 .and("deviceMetrics.metricValues.values.date").as("date")
								 .and("deviceMetrics.metricValues.values.value").as("value"));
		
		TypedAggregation<CloudMonitoringResource> agg = Aggregation.newAggregation(CloudMonitoringResource.class, list);
		
		List<DeviceMetric> metrics = template.aggregate(agg, CloudMonitoringResource.class, DeviceMetric.class).getMappedResults();*/
		
		AggregateIterable<Document> aggregation = template.getDb()
																		 .getCollection(
																		 		template.getCollectionName(CloudMonitoringResource.class))
																		 .aggregate(pipeline, Document.class);
		
		
		List<DeviceMetric> metrics = new ArrayList<>();
		
		aggregation.map((document) -> {
			DeviceMetric newMetric = new DeviceMetric();
			newMetric.setDeviceId(document.getString("deviceId"));
			newMetric.setTag(document.getString("tag"));
			newMetric.setDate(document.getDate("date"));
			newMetric.setValue(document.getString("value"));
			return newMetric;
		}).into(metrics);
		
		
		return metrics;
	}
}

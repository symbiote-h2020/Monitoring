package eu.h2020.symbiote.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.client.model.DBCollectionUpdateOptions;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.beans.DayMetricList;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.QueryParam;

/**
 * Manage the REST operations from Platform using MongoDB 
 * @author: Fernando Campos, Jose Antonio Sanchez
 * @version: 19/04/2017
 */
@RestController
@RequestMapping("/")
public class PlatformMonitoringRestService {
	
	private static final Log logger = LogFactory.getLog(PlatformMonitoringRestService.class);
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
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
		return new SimpleDateFormat(DATE_FORMAT).format(input);
	}
	
	private CloudMonitoringResource createResourceDocument(DeviceMetric metric) {
		CloudMonitoringResource input = new CloudMonitoringResource();
		input.setResource(metric.getDeviceId());
		
		Map<String,List<DayMetricList>> metrics = new HashMap<>();
		
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
		
		
		metrics.put(metric.getTag(), dayMetrics);
		
		input.setMetrics(metrics);
		
		return input;
	}
	
	private boolean addMetricToDevice(DeviceMetric metric) {
		
		MetricValue value = new MetricValue();
		value.setDate(metric.getDate());
		value.setValue(metric.getValue());
		
		String day = getDateWithoutTime(metric.getDate());
		
		/*
		 * TODO: With MongoDB 3.6 we shoud be able to use arrayFilters but we have to wait until Spring Data catches up:
		 * https://jira.mongodb.org/browse/SERVER-831
		 */
		
		DBObject query = new BasicDBObject().append("resourceId",metric.getDeviceId());
		
		
		DBObject update = new BasicDBObject().append("$push", new BasicDBObject().append("deviceMetrics.$[i].metricValues.$[j].values", value));
		
		
		
		DBCollectionUpdateOptions options = new DBCollectionUpdateOptions();

		template.getDb().getCollection(template.getCollectionName(CloudMonitoringResource.class)).update(query, update, options);
		
		String tagElement = "deviceMetrics."+metric.getTag();
		
		Query match = new Query(Criteria
																.where("resourceId").is(metric.getDeviceId())
																.and(tagElement+".day").is(day));
		
		Update update = new Update().push(tagElement+".$.values", value);
		
		

		
		WriteResult result = template.updateFirst(match, update, CloudMonitoringResource.class);
		if (!result.isUpdateOfExisting()) {
			//Resource, metric or day doesn't exist. Let's try to create the initial document.
			CloudMonitoringResource resource = createResourceDocument(metric);
			
			Query resourceQuery = new Query(Criteria.where("resourceId").is(metric.getDeviceId()));
			
			result = template.upsert(resourceQuery,
					new Update().push(tagElement)
							.each(resource.getMetrics().get(metric.getTag())), CloudMonitoringResource.class);
			
			if (result.getN() != 1) {
				logger.error("Error upserting metric");
				return false;
			} else {
				return true;
			}
			
		} else {
			return true;
		}
	}
	
	private AggregationExpression getFilterExpression(String array, DBObject condition) {
		return new AggregationExpression() {
			@Override
			public DBObject toDbObject(AggregationOperationContext context) {
				
				DBObject filterExpression = new BasicDBObject();
				filterExpression.put("input", "$"+array);
				filterExpression.put("as", array);
				filterExpression.put("cond", condition);
				return new BasicDBObject("$filter", filterExpression);
			}
		};
	}
	
	
	@RequestMapping(method = RequestMethod.GET, path = MonitoringConstants.METRICS_DATA, produces = "application/json", consumes = "application/json")
	public @ResponseBody
	List<DeviceMetric> getMetrics(@QueryParam("device") String device,
																					 @QueryParam("deviceType") String type,
																					 @QueryParam("metric") String metric,
																					 @QueryParam("startDate") Date startDate,
																					 @QueryParam("endDate") Date endDate) throws Throwable {
		
		List<AggregationOperation> list = new ArrayList<AggregationOperation>();
		
		Set<String> deviceIds = new HashSet<>();
		
		if (device != null) {
			deviceIds.add(device);
		}
		
		if (type != null) {
			deviceIds.addAll(cloudResourceRepository.findByParamsType(type).stream()
													 .map(resource -> resource.getInternalId()).collect(Collectors.toList()));
		}
		
		if (!deviceIds.isEmpty()) {
			list.add(Aggregation.match(Criteria.where("deviceId").in(deviceIds)));
		}
		
		if (metric != null) {
			list.add(Aggregation.match(Criteria.where("deviceMetrics."+metric).exists(true)));
			list.add(Aggregation.project("deviceId", "deviceMetrics."+metric));
		}
		
		if (startDate != null || endDate != null) {
			Set<String> metrics = new HashSet<>();
			
			if (metric != null) {
				// Best case, get only one metric
				metrics.add(metric);
			} else {
				// Need to get the list of metrics. This is going to take time
				template.get
			}
			
			list.add(Aggregation.unwind("deviceMetrics"));
			list.add(Aggregation.unwind("deviceMetrics.metrics"));
			
			if (startDate != null) {
				String startDay = getDateWithoutTime(startDate);
				list.add(Aggregation.match(Criteria.where("deviceMetrics.metrics.day").gte(startDay)));
			}
			
			if (endDate != null) {
				String endDay = getDateWithoutTime(endDate);
				list.add(Aggregation.match(Criteria.where("deviceMetrics.metrics.day").lte(endDay)));
			}
			
			list.add(Aggregation.unwind("deviceMetrics.metrics.values"));
			
			if (startDate != null) {
				list.add(Aggregation.match(Criteria.where("deviceMetrics.metrics.values.date").gte(startDate)));
			}
			
			if (endDate != null) {
				list.add(Aggregation.match(Criteria.where("deviceMetrics.metrics.values.date").lte(endDate)));
			}
			
			
		}
		
		TypedAggregation<CloudMonitoringResource> agg = Aggregation.newAggregation(CloudMonitoringResource.class, list);
		
		List<CloudMonitoringResource> metrics = template.aggregate(agg, CloudMonitoringResource.class, CloudMonitoringResource.class).getMappedResults();
		
		return null;
	}
}

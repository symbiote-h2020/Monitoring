package eu.h2020.symbiote.service;

import com.mongodb.WriteResult;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.beans.MonitoringMetric;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.db.FederationInfoRepository;
import eu.h2020.symbiote.db.ResourceRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
	private ResourceRepository resourceRepository;
	
	@Autowired
	private FederationInfoRepository monitoringDeviceRepository;
	
	@Autowired
	private MongoTemplate template;
	
	@Autowired
	private MetricsProcessor processor;
	
	/**
	 * Listen from Platform Host.
	 * Received device monitoring data
	 * @throws Throwable
	 *
	 */
	@RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.METRICS_DATA,  produces = "application/json", consumes = "application/json")
	public @ResponseBody List<DeviceMetric> saveMetrics(@RequestBody List<DeviceMetric> metrics) throws Throwable {
		
		List<DeviceMetric> updated = new ArrayList<>();
		
		metrics.forEach(metric -> {
			MonitoringMetric monMetric = new MonitoringMetric(metric);
			Query query = new Query(Criteria.where("resource.internalId").is(metric.getDeviceId()));
			Update update = new Update().addToSet("metrics", monMetric);
			WriteResult result = template.updateFirst(query, update, CloudMonitoringResource.class);
			if (result.isUpdateOfExisting()) {
				updated.add(metric);
			}
		});
		
		List<CloudMonitoringResource> coreMetrics = processor.getCoreMetrics();
		
		return updated;
	}
	
}

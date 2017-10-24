package eu.h2020.symbiote.service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.h2020.symbiote.AppConfig;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringMetrics;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.constants.MonitoringConstants;
import eu.h2020.symbiote.datamodel.MonitoringDevice;
import eu.h2020.symbiote.datamodel.MonitoringDeviceStats;
import eu.h2020.symbiote.datamodel.MonitoringRequest;
import eu.h2020.symbiote.db.MonitoringDeviceRepository;
import eu.h2020.symbiote.db.MonitoringRepository;
import eu.h2020.symbiote.db.MonitoringRequestRepository;
import eu.h2020.symbiote.db.ResourceRepository;

/**
 * Manage the REST operations from Platform using MongoDB 
 * @author: Fernando Campos
 * @version: 19/04/2017
 */
@RestController
@RequestMapping("/")
public class PlatformMonitoringRestService {

	 private static final Log logger = LogFactory.getLog(PlatformMonitoringRestService.class);
	 
	 
	 @Value("${platform.id}")
	 private String platformId;
	 
	 @Autowired
	 private ResourceRepository resourceRepository;
	 
	 @Autowired
	 private MonitoringRepository monitoringRepository;
	 
	 @Autowired
	 private MonitoringDeviceRepository monitoringDeviceRepository;
	 
	 
	 @Autowired
	 private MonitoringRequestRepository monitoringRequestRepository;
	 /**
	  * Listen from Platform Host.
	  * Received device monitoring data 
	 * @throws Throwable 
	  * 
	  */
	 @RequestMapping(method = RequestMethod.POST, path = MonitoringConstants.SUBSCRIBE_MONITORING_DATA,  produces = "application/json", consumes = "application/json")
	 public @ResponseBody String  MonitorRestServer(@PathVariable("platformId") String platformId, @RequestBody CloudMonitoringPlatform platform) throws Throwable {

		 if( monitoringRequestRepository.getByTag("availability") == null) 
		 {
			 MonitoringRequest defaultRequest = new MonitoringRequest();
			 defaultRequest.setTag("availability");
			 defaultRequest.setFederationId("ALL");
			 defaultRequest.setDateFederationCreation(new Date());
			 monitoringRequestRepository.save(defaultRequest);

			 defaultRequest = new MonitoringRequest();
			 defaultRequest.setTag("load");
			 defaultRequest.setFederationId("ALL");
			 defaultRequest.setDateFederationCreation(new Date());
			 monitoringRequestRepository.save(defaultRequest);
			 
		 }
			 


		 
		  MonitoringDevice md = new MonitoringDevice();
		 
		  logger.info("***********************************************************");
		  try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}	
		  logger.info("****** CloudMonitoringPlatform RECEIVED *******************");
		  logger.info("Platform.Id:" + platform.getInternalId() + " " +
				  " has " + platform.getDevices().length + " devices" + " " +
				  "Platform.time: " + platform.getTimePlatform()
				  //+ " " +"Platform.kpis: " + platform.getKpis()
				  );
		  logger.info("DEVICES:");
		  int cantavai = 0;
		  int sumload = 0;
		  int sizeDevices = platform.getDevices().length;
		  for (int i = 0; i<sizeDevices; i++){
			  logger.info(
					  "Device.id:" + platform.getDevices()[i].getId() + " "+
				      "Device.timemetric: " + platform.getDevices()[i].getTimemetric()
				      );
			  md.setInternalId(platform.getDevices()[i].getId());
			  md.setTimemetric(platform.getDevices()[i].getTimemetric());
			  for (int m = 0; m<platform.getDevices()[i].getMetrics().length; m++){
				  logger.info(
						  "   Metric.tag: " + platform.getDevices()[i].getMetrics()[m].getTag() + " " +				
						  "   Metric.value: " + platform.getDevices()[i].getMetrics()[m].getValue()
						  );
				  md.setTag(platform.getDevices()[i].getMetrics()[m].getTag());				  
				  md.setValue(platform.getDevices()[i].getMetrics()[m].getValue());		
				  monitoringDeviceRepository.save(md);
				  if (md.getTag().equals("availability") && md.getValue() == 1)
				  {
					  	cantavai=cantavai+1;
				  }
				  else if (md.getTag().equals("load") && md.getValue() != -1 )
				  {
				  		sumload = sumload + md.getValue();
				  }
			  }
		  }

		  	  
		  		  // save CloudMonitoringPlatform in internalRepository
		  logger.info("Adding CloudMonitoringPlatform to database");
		  
	    		  
		  int percentAvaiPlatform = ((cantavai*100)/sizeDevices);
		  int mediaLoadPlatform = (sumload/sizeDevices);
		  platform.setAvaiPlatform(percentAvaiPlatform);
		  platform.setLoadPlatform(mediaLoadPlatform);
		  
		  platform.setTimeRegister(new Date());



//		   ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
//		   MongoTemplate mongotemp = ctx.getBean(MongoTemplate.class); 
//		   MongoOperations mongoOps = mongotemp; 
		   

		      
		   
		   String tagReq=null;
		   String metric=null;
		   List<MonitoringRequest> mreq = getMonitoringRequest();
		   
		   for (int r = 0; r < mreq.size(); r++){
			   tagReq = mreq.get(r).getTag();
			   logger.info(
					"Tag:" + tagReq + " " + 
					"Federation: " + mreq.get(r).getFederationId() + " " +
					"F.Date: " + mreq.get(r).getDateFederationCreation()
				);			
			   
			   Date maxdate=null;
			   Date mindate=null;
			   if(tagReq.indexOf(".") > 0)
			   {
				   int iMinday = 0;
				   String sMinDay = tagReq.substring(tagReq.indexOf(".")+1);
				   maxdate = new Date();		  
				   DateTime jdate = new DateTime(maxdate); 
				   if (!sMinDay.equals("all"))
				   {
					   iMinday = (new Integer(sMinDay)).intValue();  
					   mindate = (jdate.minusDays(iMinday)).toDate();
					   logger.info("mindate:"+mindate.toString());
				   }
				   else
				   {
					   logger.info("mindate:"+mindate);
				   }
				   logger.info("maxdate:"+maxdate.toString());

				   // Metric: availability
				   if ( tagReq.indexOf(".") > 0 && tagReq.substring(0, tagReq.indexOf(".")).equals("avai") )
					   metric="availability";
				   else if (tagReq.indexOf(".") > 0 && tagReq.substring(0, tagReq.indexOf(".")).equals("load"))
					   metric="load";

				   List<MonitoringDeviceStats> monitoringDeviceStats = getAggregation(metric, mindate, maxdate);
				   logger.info("--- monitoringDeviceStats ---");	  
				   String devId=null;
				   for (int i = 0; i<monitoringDeviceStats.size(); i++){
					  devId = monitoringDeviceStats.get(i).getId();		   	      
					  logger.info(
							  "Id:" + devId + " " + 
							  "Percentage: " + monitoringDeviceStats.get(i).getPercentage() + " " +  
							  "Average: " + monitoringDeviceStats.get(i).getAverage() + " " +
							  "MinValue: " + monitoringDeviceStats.get(i).getMinValue() + " " +
							  "MaxValue: " + monitoringDeviceStats.get(i).getMaxValue() + " " +
							  "Count: " + monitoringDeviceStats.get(i).getCount()
							  );
					  int posDev = getPosdevicebyId(platform, devId);
					  CloudMonitoringMetrics[] cmm = platform.getDevices()[posDev].getMetrics();
					  int ilenM = cmm.length;
					  List<CloudMonitoringMetrics> listCMM = new ArrayList<CloudMonitoringMetrics>();
					  for (int j = 0; j < ilenM; j++){
						  listCMM.add(cmm[j]);						  
					  }
					  CloudMonitoringMetrics newCMM = new CloudMonitoringMetrics();	 
					  newCMM.setTag(tagReq);
					  if(metric.equals("availability"))
						  newCMM.setValue(monitoringDeviceStats.get(i).getPercentage());
					  else if (metric.equals("load"))
						  newCMM.setValue(monitoringDeviceStats.get(i).getAverage());
					  newCMM.setDatemin(mindate);
					  newCMM.setDatemax(maxdate);	
					  newCMM.setCount(monitoringDeviceStats.get(i).getCount());
					  newCMM.setValuemin(monitoringDeviceStats.get(i).getMinValue());
					  newCMM.setValuemax(monitoringDeviceStats.get(i).getMaxValue());
					  
					  listCMM.add(newCMM);
					  
					  CloudMonitoringMetrics[] newlistCMM = new CloudMonitoringMetrics[ listCMM.size() ];
					  listCMM.toArray( newlistCMM );
					  platform.getDevices()[posDev].setMetrics(newlistCMM);
					  
				  } // end for monitoringDeviceStats
			   
			   } // end if tagReq.indexOf(".")
		  
		   } // end for MonitoringRequestfor
		   
		   logger.info("****** CloudMonitoringPlatform ADDING *******************");
		  
		  logger.info("Register TimeRegister: " + platform.getTimeRegister());
		  logger.info("Register UTC TimeRegister: "+ new DateTime(platform.getTimeRegister(),DateTimeZone.UTC));
	  
		  CloudMonitoringPlatform res = addOrUpdateInInternalRepository(platform);
		  logger.info("added: " + res);
		  
		  return "received";
	  }
	 






	/**
	  * Listen from Platform Host.
	  * Received request monitoring data from SLAM
	  * 
	  */
	 @RequestMapping(method = RequestMethod.GET, path = MonitoringConstants.SUBSCRIBE_REQUEST_MONITORING_DATA,  produces = "application/json", consumes = "application/json")
	 public @ResponseBody String  RequestMonitorRestServer(@PathVariable("platformId") String platformId, @RequestBody MonitoringRequest monitoringrequest) {

		  
		  return "received";
	  }
	 
		
	/**
	  * Add or Update CloudResource document from MongoDB.
	  */	 
	 public List<CloudResource>  addOrUpdateInInternalRepository(List<CloudResource>  resources){
		 logger.info("Adding CloudResource to database");
		 return resources.stream().map(resource -> {
			  CloudResource existingResource = resourceRepository.getByInternalId(resource.getInternalId());
		      if (existingResource != null) {
		    	  logger.info("update will be done");
		      }
		      return resourceRepository.save(resource);
		 })
	     .collect(Collectors.toList());
	  }
	 
	/**
	  * Delete CloudResource document from MongoDB.
	  */	
	  public List<CloudResource> deleteInInternalRepository(List<String> resourceIds){
		  List<CloudResource>  result = new ArrayList<CloudResource>();
		  for (String resourceId:resourceIds){
			  CloudResource existingResource = resourceRepository.getByInternalId(resourceId);
		      if (existingResource != null) {
		    	  result.add(existingResource);
		    	  resourceRepository.delete(resourceId);
		      }
		  }
		  return result;
	  }
	
	/**
	  * Get all CloudResource document from MongoDB.
	  */		  
	  public List<CloudResource> getResources() {
		  return resourceRepository.findAll();
	  }
	

	/**
	 * The getResource method retrieves \a ResourceBean identified by \a resourceId 
	 * from the mondodb database and will return it.
	 * @param resourceId from the resource to be retrieved from the database
	 * @return the ResourceBean
	 */  
	public CloudResource getResource(String resourceId) {
		if (!"".equals(resourceId)) {
			return resourceRepository.getByInternalId(resourceId);
		}
		return null;
	}
	
	
	/**
	  * Add or Update CloudMonitoringPlatform document from MongoDB.
	  */	 
	 public CloudMonitoringPlatform addOrUpdateInInternalRepository(CloudMonitoringPlatform resource){
		 	logger.info("Adding CloudMonitoringPlatform to database");
		    return monitoringRepository.save(resource);

	  }
	
	 
	 /**
	  * Get Monitoring information
	 * @throws Exception 
	  */
	public CloudMonitoringPlatform getMonitoringInfo() throws Exception{
		List<CloudResource> resources = resourceRepository.findAll();
		CloudMonitoringPlatform platform = null;
		if (resources != null){
			
			//monitoringRepository.getByInternalId(monitoringRepository);
			List<CloudMonitoringPlatform> lcmp = monitoringRepository.findAll(new Sort(Direction.DESC,"timeRegister"));
			if (lcmp.size() > 0)
			{
				platform= lcmp.get(0);
				
				logger.info("Last CloudMonitoringPlatform timeRegister at: " + platform.getTimeRegister());				
				CloudMonitoringDevice[] devicesPlat = platform.getDevices();
				ArrayList<CloudMonitoringDevice> devices = new ArrayList();
				
				int x=0;
				for(int i = 0;i<devicesPlat.length;i++)
				{
					String idDevPlat =  devicesPlat[i].getId();
					
					if (resourceRepository.getByInternalId(idDevPlat)!=null)
					{
						//devices[x] = devicesPlat[i];
						devices.add(devicesPlat[i]);
					}
					else
					{
						logger.info("Device not registered: " + devicesPlat[i].getId());
					}
					
				}
				
				CloudMonitoringDevice[] checkdevices= new CloudMonitoringDevice[devices.size()];
				for(int i = 0;i<devices.size();i++)
				{
					checkdevices[0] = devices.get(i);
				}
				platform.setDevices(checkdevices);
				
			}
			else
				logger.error("Not CloudMonitoringPlatform found in DB");
			

	
		}
		return platform;
	}

	 /**
	  * Get Monitoring information from device
	 * @throws Exception 
	  */
	private CloudMonitoringDevice getMonitoringInfoFromDevice(CloudResource resource) throws Exception{
		CloudMonitoringDevice monitoringDevice = null;
		
		String deviceId = resource.getInternalId();
		Query query = new Query();
		query.addCriteria(Criteria.where("internalId").is("helloid"));	
		MongoTemplate mongoTemplate = (new AppConfig()).mongoTemplate();
		List<CloudMonitoringPlatform> lcmp = mongoTemplate.find(query, CloudMonitoringPlatform.class);
		for(int i = 0;i<lcmp.size();i++)
		{
			CloudMonitoringPlatform cmp = lcmp.get(i);
			System.out.println("One Device:" + cmp.getDevices()[0].getId());
		}
		return monitoringDevice;
	}

	/**
	 * 
	 *  Get Device Position from device by deviceId
	 *  
	 */
	private int getPosdevicebyId(CloudMonitoringPlatform platform, String devId) 
	{
		int ipos=0;
		
		for (int m = 0; m < platform.getDevices().length; m++)
		{
			if ( platform.getDevices()[m].getId().equals(devId) )
			{
				ipos = m;
				break;
			}
		}
		//logger.info("Device: " + devId + " is in Pos:"+ipos);
		return ipos;
	}
	
	/**
	  * Get all MonitoringRequest document from MongoDB.
	  */		  
	public List<MonitoringRequest> getMonitoringRequest() {
		  return monitoringRequestRepository.findAll();
	}

	
	static <CloudMonitoringMetrics> CloudMonitoringMetrics[] append(CloudMonitoringMetrics[] arr, CloudMonitoringMetrics element) {
	    final int N = arr.length;
	    arr = Arrays.copyOf(arr, N + 1);
	    arr[N] = element;
	    return arr;
	}
	
	/**
	 * 
	 * Get aggregation data from mongoDB.
	 * 	 
	 */
	private List<MonitoringDeviceStats> getAggregation(String metric, Date mindate, Date maxdate) throws Exception {
		
		  MongoOperations mongoOps = (new AppConfig()).mongoTemplate();
		  List<AggregationOperation> listOp  = new ArrayList<AggregationOperation>();
		  
		  if (metric.equals("availability"))
		  {
			  if(mindate!=null && !mindate.equals(maxdate))			  
				  listOp.add( match(Criteria.where("tag").is(metric).andOperator(Criteria.where("timemetric").gte(mindate).andOperator(Criteria.where("timemetric").lt(maxdate)))) );
			  else
				  listOp.add( match(Criteria.where("tag").is(metric)));
			  listOp.add(
					  group("internalId").sum("value").as("sum").min("value").as("minValue").max("value").as("maxValue").count().as("count")
			  );
			  listOp.add(
					  project()
					  	.andExpression("sum*100/count").as("percentage")
					  	.and("count").as("count")
					  	.and("minValue").as("minValue")
					  	.and("maxValue").as("maxValue")
			  );
		  }	  
		  else if (metric.equals("load")) 
		  {
			  //listOp.add( match(Criteria.where("tag").is(metric).andOperator(Criteria.where("timemetric").gte(mindate).andOperator(Criteria.where("timemetric").lt(maxdate)))) );
			  if(mindate!=null && !mindate.equals(maxdate))			  
				  listOp.add( match(Criteria.where("tag").is(metric)
						  .andOperator(Criteria.where("value").ne(-1)
						  		.andOperator(Criteria.where("timemetric").gte(mindate)
						  				.andOperator(Criteria.where("timemetric").lt(maxdate)
									  				)
								  			)
						  				)
						  			) 
						  );
			  else
				  listOp.add( match(Criteria.where("tag").is(metric)
						  .andOperator(Criteria.where("value").ne(-1)
								  		)
						  			)
						  );
			  listOp.add(
					  group("internalId").avg("value").as("average").min("value").as("minValue").max("value").as("maxValue").count().as("count")
			  );
		  }	  

		  
		  
		  TypedAggregation<MonitoringDevice> agg = newAggregation(MonitoringDevice.class,listOp);

		  AggregationResults<MonitoringDeviceStats> results = mongoOps.aggregate(agg, MonitoringDeviceStats.class);

		  return results.getMappedResults();
	}
}

package eu.h2020.symbiote.service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.lang.SerializationUtils;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.AppConfig;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringMetrics;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.datamodel.MonitoringDevice;
import eu.h2020.symbiote.datamodel.MonitoringDeviceStats;
import eu.h2020.symbiote.datamodel.MonitoringFedDev;
import eu.h2020.symbiote.datamodel.MonitoringRequest;
import eu.h2020.symbiote.db.MonitoringRepository;
import eu.h2020.symbiote.db.MonitoringRequestRepository;
import eu.h2020.symbiote.db.ResourceRepository;
import eu.h2020.symbiote.rest.crm.CRMMessageHandler;

/**
 * This class implements the rest interfaces. Initially created by jose
 *
 * @author: Elena Garrido, David Rojo; Fernando Campos
 * @version: 02/11/2017
 */
@Component
public class PlatformMonitoringComponentRestService {
  private static final Log logger = LogFactory.getLog(PlatformMonitoringComponentRestService.class);
  
  @Value("${symbIoTe.crm.integration}")
  private boolean pubCRM;
  
  @Autowired
  private CRMMessageHandler crmMessageHandler;
  
  @Autowired
  private ResourceRepository resourceRepository;
	 
  @Autowired
  private MonitoringRepository monitoringRepository;

  @Autowired
  private MonitoringRequestRepository monitoringRequestRepository;

  @Autowired
  private AppConfig config;
	 
  @Scheduled(cron = "${symbiote.crm.publish.period}")
  public void publishMonitoringDataCrm() throws Exception{
	  
	  logger.info("Polling...");
	  
	  List<String> pubList = new ArrayList<String>();
	  Hashtable<String, Hashtable<String, Date>> htFedDevices = new Hashtable<String, Hashtable<String, Date>>();
	  Hashtable<String, Date> htDevices = new Hashtable<String, Date>();
	  Hashtable<String, Date> htCoreDevices = new Hashtable<String, Date>();
	  //CloudMonitoringPlatform platformDevOri = getMonitoringInfo();
	  CloudMonitoringPlatform platform= getMonitoringInfo();
	  
	  CloudMonitoringDevice[] platformDevOri = new CloudMonitoringDevice[platform.getDevices().length];
	  platformDevOri=platform.getDevices();

	  List<CloudMonitoringDevice> regCoreDev = getRegisteredCoreDevices();

	  for (int i = 0; i < regCoreDev.size(); i++){

		  logger.info(
				  "RegisteredCoreDevices Id:" + regCoreDev.get(i).getId()
				  );
		  htCoreDevices.put((String)regCoreDev.get(i).getId(), new Date());
	  }
	  
	  String sDeviceList = getDeviceList(regCoreDev);	  	  	  
	  logger.info("sDeviceList:" + sDeviceList);
	  if(sDeviceList != null)
	  {
		  pubList.add("CORE");
		  htFedDevices.put("ALL", htCoreDevices);
	  }

	  
	  List<MonitoringFedDev> regFedDev = getRegisteredFedDevices();
	  String idFedPrev = null;	  
	  for (int i = 0; i < regFedDev.size(); i++){
		  String idFed = regFedDev.get(i).getIdfed();
		  String sidDev = regFedDev.get(i).getIddev();
		  sidDev = sidDev.substring(sidDev.indexOf('"')+1,sidDev.lastIndexOf('"'));
		  
		  if (idFedPrev==null)
			  idFedPrev=idFed;
		  logger.info(
				  
				  "RegisteredFedDevices IdFed:" + regFedDev.get(i).getIdfed() + " " +
				  "RegisteredFedDevices DateFed:" + regFedDev.get(i).getDatefed()+ " " +
				  "RegisteredFedDevices IdDev:" + sidDev
				  
				  );
		  
		  if (idFed.equals(idFedPrev))
		  {
			  htDevices.put(sidDev, regFedDev.get(i).getDatefed());
			  if ( i == (regFedDev.size()-1))
			  {
				  htFedDevices.put(idFed, htDevices);
				  pubList.add(idFed);
			  }  
		  }
		  else
		  {
			  htFedDevices.put(idFedPrev, htDevices);
			  pubList.add(idFedPrev);
			  idFedPrev=idFed;
			  htDevices = new Hashtable<String, Date>();
			  htDevices.put(sidDev, regFedDev.get(i).getDatefed());
		  }
			  
		  
	  }
	  logger.info(
			  
			  "RegisteredFedDevices htFedDevices:" + htFedDevices
			  
			  );
	  
	  if (platform != null && platform.getMetrics()==null) 
	  {  
		  
		  for (int p = 0; p < pubList.size(); p++)
		  {

			  logger.info("********** START PROCESS *********** ->" + pubList.get(p));

			  //CloudMonitoringPlatform platform= duplicationCloudMonitoringPlatform(platformDevOri);
			  platform.setDevices(platformDevOri);
			  
			  // Obtained list devices to be published 
			  Hashtable<String, Date> listDevices = new Hashtable<String, Date>();  
			  if(pubList.get(p).equals("CORE"))
				  listDevices = htCoreDevices;
			  else
				  listDevices = htFedDevices.get(pubList.get(p));
			  
			  // Filtered platforms received devices with the registar by Core or FederationId.
			  platform = filterDevicestoSend (listDevices, platform);
			  
			  // Reorganize tags
			  List<MonitoringRequest> mreq = getMonitoringRequest();
			  List<MonitoringRequest> mreqdev = new ArrayList<MonitoringRequest>();	 
			  Hashtable<String, String> tagTable = new Hashtable<String, String>();

			  for (int i = 0; i < mreq.size(); i++){
				  String tagReq = mreq.get(i).getTag(); 

				  logger.info(
						  "Tag:" + tagReq 
						  );
				  if (tagReq.indexOf(".") > 0 )
				  {

					  String sTagwithoutGroup = tagReq.substring(tagReq.indexOf(".")+1);
					  //logger.info("sTagwithoutGroup:" + sTagwithoutGroup);
					  if ( tagTable.get(sTagwithoutGroup) == null)
					  {
						  tagTable.put(sTagwithoutGroup, sTagwithoutGroup);
						  mreqdev.add(mreq.get(i));
					  } 
				  } // end if
			  }	// end for

			  
			  
			  
			  List<CloudMonitoringMetrics> listCMMPlat = new ArrayList<CloudMonitoringMetrics>();
			  Hashtable<String, String> tagProcessed = new Hashtable<String, String>();

			  // Include Metrics in CloudMonitoringPlatform level		  
			  // Add accumulated metric by Platform
			  for (int r = 0; r < mreq.size(); r++){
				  String metric=null;
				  String tagReq = mreq.get(r).getTag();

				  if (tagReq.indexOf(".") > 0 && tagProcessed.get(tagReq)==null )
				  {
					  int sizeCMM=0;	
					  StringTokenizer strTkn = new StringTokenizer(tagReq, ".");
					  String sType = strTkn.nextToken();
					  String sTag = strTkn.nextToken();
					  String sMinDay = strTkn.nextToken();

					  Date mindate=null;
					  Date maxdate = new Date();				   
					  DateTime jdate = new DateTime(maxdate); 
					  if (!sMinDay.equals("all"))
					  {
						  int iMinday = (new Integer(sMinDay)).intValue();  
						  mindate = (jdate.minusDays(iMinday)).toDate();
						  logger.info("mindate:"+mindate.toString());
					  }
					  else
					  {
						  logger.info("mindate:"+mindate);
					  }
					  logger.info("maxdate:"+maxdate.toString());

					  if ( sTag.equals("avai") ) metric="availability";
					  else if (sTag.equals("load")) metric="load";
					  logger.info("--- monitoringDeviceStats by Type ---> " + sTag);	
					  List<MonitoringDeviceStats> monitoringDeviceStats = getAggregation(pubList.get(p), htFedDevices, metric, mindate, maxdate, "type");

					  String devId=null;
					  sizeCMM = monitoringDeviceStats.size();



					  for (int j = 0; j<sizeCMM; j++){
						  CloudMonitoringMetrics objCMM = new CloudMonitoringMetrics();
						  devId = monitoringDeviceStats.get(j).getId();		   	      
						  logger.info(
								  "Id:" + devId + " " +
										  "Percentage: " + monitoringDeviceStats.get(j).getPercentage() + " " +  
										  "Average: " + monitoringDeviceStats.get(j).getAverage() + " " +
										  "MinValue: " + monitoringDeviceStats.get(j).getMinValue() + " " +
										  "MaxValue: " + monitoringDeviceStats.get(j).getMaxValue() + " " +
										  "Count: " + monitoringDeviceStats.get(j).getCount()
								  );

						  String tagProc = devId+tagReq.substring(tagReq.indexOf("."));
						  tagProcessed.put(tagProc, tagProc);
						  logger.info("tagProc:"+tagProc);
						  objCMM.setTag(tagProc);
						  if(metric.equals("availability"))				
							  objCMM.setValue(monitoringDeviceStats.get(j).getPercentage());
						  else if (metric.equals("load"))
							  objCMM.setValue(monitoringDeviceStats.get(j).getAverage());
						  objCMM.setDatemin(mindate);
						  objCMM.setDatemax(maxdate);
						  objCMM.setValuemin(monitoringDeviceStats.get(j).getMinValue());
						  objCMM.setValuemax(monitoringDeviceStats.get(j).getMaxValue());
						  objCMM.setCount(monitoringDeviceStats.get(j).getCount());

						  listCMMPlat.add(objCMM);

					  }

				  } // end if

			  } // end for
			  CloudMonitoringMetrics[] cmmPlat = new CloudMonitoringMetrics[listCMMPlat.size()];
			  listCMMPlat.toArray(cmmPlat);
			  platform.setMetrics(cmmPlat);	  

			  // Add accumulated metric by Device
			  for (int r = 0; r < mreqdev.size(); r++){
				  String metric=null;
				  String tagReq = mreqdev.get(r).getTag();



				  if(tagReq.indexOf(".") > 0)
				  {
					  StringTokenizer strTkn = new StringTokenizer(tagReq, ".");
					  String sType = strTkn.nextToken();
					  String sTag = strTkn.nextToken();
					  String sMinDay = strTkn.nextToken();
					  logger.info(
							  "sTag: " + sTag + " " +
									  "sMinDay: " + sMinDay
							  );

					  Date mindate=null;
					  Date maxdate = new Date();				   
					  DateTime jdate = new DateTime(maxdate); 
					  if (!sMinDay.equals("all"))
					  {
						  int iMinday = (new Integer(sMinDay)).intValue();  
						  mindate = (jdate.minusDays(iMinday)).toDate();
						  logger.info("mindate:"+mindate.toString());
					  }
					  else
					  {
						  logger.info("mindate:"+mindate);
					  }
					  logger.info("maxdate:"+maxdate.toString());

					  if ( sTag.equals("avai") ) metric="availability";
					  else if (sTag.equals("load")) metric="load";


					  List<MonitoringDeviceStats> monitoringDeviceStats = getAggregation(pubList.get(p), htFedDevices, metric, mindate, maxdate, "internalId");
					  logger.info("--- monitoringDeviceStats by DeviceId ---");	

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
						  if (posDev >= 0)
						  {	  
							  CloudMonitoringMetrics[] cmm = platform.getDevices()[posDev].getMetrics();
							  int ilenM = cmm.length;
							  List<CloudMonitoringMetrics> listCMM = new ArrayList<CloudMonitoringMetrics>();
							  for (int j = 0; j < ilenM; j++){
								  listCMM.add(cmm[j]);						  
							  }
							  CloudMonitoringMetrics newCMM = new CloudMonitoringMetrics();	 
							  newCMM.setTag(sTag+"."+sMinDay);
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
						  }


					  } // end for monitoringDeviceStats

				  } // end if tagReq.indexOf(".")

			  } // end for MonitoringRequestfor
			  
			  //if ( !pubList.get(p).equals("CORE") )
			  platform.setFederationId(pubList.get(p));
			  platform.setTimeRegister(new Date());

			  logger.info("****** CloudMonitoringPlatform ADDING *******************");

			  logger.info("FederationId: " + platform.getFederationId());
			  logger.info("Register TimeRegister: " + platform.getTimeRegister());
			  logger.info("Register UTC TimeRegister: "+ new DateTime(platform.getTimeRegister(),DateTimeZone.UTC));

			  CloudMonitoringPlatform res = addOrUpdateInInternalRepository(platform);
			  logger.info("added: " + res);


			  logger.info("Publishing monitoring info to CRM");
			  logger.info("Platform " + platform.getInternalId() + " has " + platform.getDevices().length + " devices");

			  for (int i = 0; i<platform.getDevices().length; i++){
				  logger.info("Device " + platform.getDevices()[i].getId());
			  }
			  //Send data to POST endpoint in CRM
			  String result=null;
			  if (pubCRM)
				  result = crmMessageHandler.doPost2Crm(platform);
			  else
				  result = "NO POST to CRM. Change symbIoTe.crm.integration=true in bootstrap.properties to POST";

			  logger.info("************** Result of post to crm = " + result);
			  logger.info("Publishing monitoring data for platform " + platform.getInternalId());
			  logger.info("Platform " + platform.getInternalId() + " has " + platform.getDevices().length + " devices");
			  for (int i = 0; i<platform.getDevices().length; i++)
			  {
				  logger.info("Device " + platform.getDevices()[i].getId());
				  logger.info("load: " + platform.getDevices()[i].getLoad());
				  logger.info("availability: " + platform.getDevices()[i].getAvailability());
				  logger.info("timestamp: " + platform.getDevices()[i].getTimemetric());		  
			  }

		  } // end for publish
	  }	// end if
	  
  }
  


	/**
	  * Get Monitoring information
	 * @throws Exception 
	  */
	public CloudMonitoringPlatform getMonitoringInfo() throws Exception{

		CloudMonitoringPlatform platform = null;
		List<CloudMonitoringPlatform> lcmp = monitoringRepository.findAll(new Sort(Direction.DESC,"timeRegister"));
		if (lcmp.size() > 0)
		{
			platform= lcmp.get(0);
			logger.info("Last CloudMonitoringPlatform timeRegister at: " + platform.getTimeRegister());				
		}
		else
			logger.error("Not CloudMonitoringPlatform found in DB");
		return platform;
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
	  * Get all MonitoringRequest document from MongoDB.
	  */		  
	public List<MonitoringRequest> getMonitoringRequest() {
		  return monitoringRequestRepository.findAll();
	}
	
	/**
	 * 
	 * Get aggregation data from mongoDB.
	 * 	 
	 */
	private List<MonitoringDeviceStats> getAggregation(String typeSym, Hashtable<String, Hashtable<String, Date>> htFedDevices, String metric, Date mindate, Date maxdate, String groupby) throws Exception {
		
		  MongoOperations mongoOps = config.mongoTemplate();
		  List<AggregationOperation> listOp  = new ArrayList<AggregationOperation>();
		  Hashtable<String, Date> htDevices = null;
		  if (typeSym.equals("CORE"))
			  htDevices = (Hashtable<String, Date>)htFedDevices.get("ALL");
		  else
			  htDevices = (Hashtable<String, Date>)htFedDevices.get(typeSym);
		  
		  //Arrays.asList(ht.keys().nextElement());
		  List<String> listDev = new ArrayList<String>();
		  Enumeration<String> e = htDevices.keys();
		  while(e.hasMoreElements())
			listDev.add(e.nextElement());
	  

		  if (metric.equals("availability"))
		  {
			  if(mindate!=null && !mindate.equals(maxdate))
			  {
				  if (typeSym.equals("CORE"))
					  listOp.add( 
							  match(Criteria.where("tag").is(metric)
									  .andOperator(Criteria.where("internalId").in(listDev)
											  .andOperator(Criteria.where("timemetric").gte(mindate)
													  .andOperator(Criteria.where("timemetric").lt(maxdate)
															  )
													  )
											  )
									  ) 
							  );
//				  else
//					  listOp.add( 
//							  match(Criteria.where("tag").is(metric)
//									  .andOperator(Criteria.where("timemetric").gte(mindate)
//											  .andOperator(Criteria.where("timemetric").lt(maxdate)
//													  )
//											  )
//									  )
//							  );

				  else
					  listOp.add( 
						  match(Criteria.where("tag").is(metric)
								  .andOperator(Criteria.where("timemetric").gte(mindate)
										  .andOperator(Criteria.where("timemetric").lt(maxdate)
									  				.andOperator(
				  													getFederationCriteria(htDevices)							  						
									  						)
												  )
										  )
								  ) 
						  );
			  }
			  else
			  {
				  if (typeSym.equals("CORE"))
					  listOp.add( 
							  match(Criteria.where("tag").is(metric)
									  .andOperator(Criteria.where("internalId").in(listDev)
											  )
									  )				
							  );
				  else
					  listOp.add( 
							  match(Criteria.where("tag").is(metric)
									  .andOperator(
											  getFederationCriteria(htDevices)							  						
											  )

									  )

							  );
			  }
			  listOp.add(
					  group(groupby).sum("value").as("sum").min("value").as("minValue").max("value").as("maxValue").count().as("count")
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
			  {
				  if (typeSym.equals("CORE"))
					  listOp.add( match(Criteria.where("tag").is(metric)
							  .andOperator(Criteria.where("internalId").in(listDev)
									  .andOperator(Criteria.where("value").ne(-1)
											  .andOperator(Criteria.where("timemetric").gte(mindate)
													  .andOperator(Criteria.where("timemetric").lt(maxdate)
															  )
													  )
							  					)
							  				)
							  			)
							  );
				  else
					  listOp.add( match(Criteria.where("tag").is(metric)
							  .andOperator(Criteria.where("value").ne(-1)
									  .andOperator(Criteria.where("timemetric").gte(mindate)
											  .andOperator(Criteria.where("timemetric").lt(maxdate)
													  .andOperator(
															  getFederationCriteria(htDevices)							  						
															  )							  						
													  )
											  )
									  )
							  ) 
							  );
			  }
			  else
			  {
				  if (typeSym.equals("CORE"))
					  listOp.add( match(Criteria.where("tag").is(metric)
							  .andOperator(Criteria.where("internalId").in(listDev)
									  .andOperator(Criteria.where("value").ne(-1)
									  			)
							  				)
							  			)
							  );
				  else
					  listOp.add( match(Criteria.where("tag").is(metric)
							  .andOperator(Criteria.where("value").ne(-1)
									  .andOperator(
											  getFederationCriteria(htDevices)							  						
											  )
									  )
							  )
							  );
			  }	  
			  listOp.add(
					  group(groupby).avg("value").as("average").min("value").as("minValue").max("value").as("maxValue").count().as("count")
			  );
		  }	  

		  TypedAggregation<MonitoringDevice> agg = newAggregation(MonitoringDevice.class,listOp);

		  AggregationResults<MonitoringDeviceStats> results = mongoOps.aggregate(agg, MonitoringDeviceStats.class);

		  return results.getMappedResults();
	}
	

	/**
	  * Get Monitoring information from device
	  * @throws Exception
	  *  
	  */
	private CloudMonitoringDevice getMonitoringInfoFromDevice(CloudResource resource) throws Exception{
		CloudMonitoringDevice monitoringDevice = null;
		
		String deviceId = resource.getInternalId();
		Query query = new Query();
		query.addCriteria(Criteria.where("internalId").is("helloid"));	
		MongoTemplate mongoTemplate = config.mongoTemplate();
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
		int ipos=-1;
		
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
	  * Add or Update CloudMonitoringPlatform document from MongoDB.
	  */	 
	 public CloudMonitoringPlatform addOrUpdateInInternalRepository(CloudMonitoringPlatform resource){
		 	logger.info("Adding CloudMonitoringPlatform to database");
		    return monitoringRepository.save(resource);

	  }

	 /**
	  * Get aggregation data to registered core devices from mongoDB.
	  * 	 
	  */
	 private List<CloudMonitoringDevice> getRegisteredCoreDevices() throws Exception {

		 MongoOperations mongoOps = config.mongoTemplate();
		 List<AggregationOperation> listOp  = new ArrayList<AggregationOperation>();
		 listOp.add( match(Criteria.where("params.isCore").is(true)));
		 listOp.add(
					  project()
					  	.and("internalId").as("id")
				 );
		 
		  TypedAggregation<CloudResource> agg = newAggregation(CloudResource.class,listOp);
		  AggregationResults<CloudMonitoringDevice> results = mongoOps.aggregate(agg, CloudMonitoringDevice.class);
		  return results.getMappedResults();
		 
	 }

	 /*
	  * Get list to String Array from CloudMonitoringDevice object
	  * "dev1","dev2","dev3"
	  */
	 private String getDeviceList(List<CloudMonitoringDevice> list) throws Exception {

		 String sList=null;
		 
		 for(int i = 0;i<list.size();i++)
		 {
			 if (i==0)
				 sList = '"'+list.get(i).getId()+'"';
			 else
				 sList = sList +','+'"'+list.get(i).getId()+'"';
			 
		 }
		 
		 return sList;
		 
	 }
	 /**
	  * Get aggregation data to registered federation devices from mongoDB.
	  * 	 
	  */
	 private List<MonitoringFedDev> getRegisteredFedDevices() throws Exception {

		 MongoOperations mongoOps = config.mongoTemplate();
		 List<AggregationOperation> listOp  = new ArrayList<AggregationOperation>();

		  listOp.add(unwind("params.listFederations"));
		  listOp.add(
				  group("params.listFederations").push("internalId").as("internalId")
		  );
		 listOp.add(
					  project() 
					  	.and("_id.idfederation").as("idfed")
					  	.and("_id.sincedate").as("datefed")
					  	.and("internalId").as("iddev")

				 );
		  listOp.add(sort(Direction.ASC, "idfed"));

		  
		  TypedAggregation<CloudResource> agg = newAggregation(CloudResource.class,listOp);
		  AggregationResults<MonitoringFedDev> results = mongoOps.aggregate(agg, MonitoringFedDev.class);
		  return results.getMappedResults();
		 
	 }
	 
	 /*
	  * Filter devices from CloudMonitoringPlatform object. Get a new  CloudMonitoringPlatform filtered object 
	  * 
	  */
	 private CloudMonitoringPlatform filterDevicestoSend(Hashtable listDevices, CloudMonitoringPlatform platOri) {

		 List<CloudMonitoringDevice> listCMD = new ArrayList<CloudMonitoringDevice>();
		 
		 for (int i = 0; i < platOri.getDevices().length; i++){
			 String idd = platOri.getDevices()[i].getId();
			 if (listDevices.get(idd) != null)
				 listCMD.add(platOri.getDevices()[i]);
		 }
		 
		 CloudMonitoringDevice[] devices = new CloudMonitoringDevice[listCMD.size()];
		 listCMD.toArray(devices);
		 platOri.setDevices(devices);
		 
		 return platOri;
	
	 }
	 
	 /**
	  * Get Criteria using and operator with internalId=deviceId and timemetric=federationDate parameters 
	  *
	  * @param htDevices
	  * @return
	  */
	 private Criteria getFederationCriteria(Hashtable<String, Date> htDevices) {
		 Criteria result = new Criteria();
		 Criteria andCriteria = new Criteria();
		 		 
		 Enumeration<String> e = htDevices.keys();

		 List<Criteria> docCriterias = new ArrayList<Criteria>();		 
		 while(e.hasMoreElements())
		 {
			 String idDev =  e.nextElement();
			 Date dateFed = htDevices.get(idDev);
			 logger.info("getFederationCriteria: idDev=" + idDev + "dateFed=" + dateFed);
			 
			 List<Criteria> arrCriterias = new ArrayList<Criteria>();
			 arrCriterias.add(Criteria.where("internalId").is(idDev));
			 arrCriterias.add(Criteria.where("timemetric").gte(dateFed));

			 // mal porque hay varios AND
			 //docCriterias.add(andCriteria.andOperator(arrCriterias.toArray(new Criteria[arrCriterias.size()])));
			 
			docCriterias.add(Criteria.where("internalId").is(idDev).and("timemetric").gte(dateFed));
			//docCriterias.add(Criteria.where("internalId").is(idDev));
			 
		 }

		 //							 .andOperator(Criteria.where("timemetric").gte(dateFed))

		 result = result.orOperator(
						 docCriterias.toArray(new Criteria[docCriterias.size()])
				 );
		 
//		 result = result.orOperator(
//				 Criteria.where("internalId").is("dev1")
//				 );
				 
		 return result;
	 }
	 
	 private CloudMonitoringPlatform duplicationCloudMonitoringPlatform(CloudMonitoringPlatform cmpSource) {
		 	CloudMonitoringPlatform cmpTarget = null;
			try {
				cmpTarget = (CloudMonitoringPlatform) SerializationUtils.clone((Serializable) cmpSource);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return cmpTarget;
		}

	 
}

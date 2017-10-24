package eu.h2020.symbiote.db;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;





/**
 * Created by Fernando on 13/10/17.
 */
/**! \class ResourceRepository 
 * \brief ResourceRepository interface to connect with the mongodb database where the registered resources will be stored
 * within the platform
 **/
//public interface MonitoringRepository extends MongoRepository<CloudMonitoringPlatform, String>, QueryByExampleExecutor<CloudMonitoringPlatform>  {
public interface MonitoringRepository extends MongoRepository<CloudMonitoringPlatform, String> {

	//! Retrieves a \a ResourceBean.
	/*!
	 * The getByInternalId method retrieves \a ResourceBean identified by the \a resourceId parameter from the   
	 * mondodb database 
	 *
	 * \param resourceId id from the resource to be retrieved 
	 * \return \a getByInternalId returns the \a ResourceBean identified by  \a resourceId
	 */
	CloudMonitoringPlatform getByInternalId(String resourceId);
	
	//CloudMonitoringPlatform findByBirthdateAfter

	
//	Query query = new Query();
//	query.addCriteria(Criteria.where("internalId").is("helloid"));	

	//CloudMonitoringPlatform cmp = mongoTemplate.findAndModify(query, CloudMonitoringPlatform.class);
	
}

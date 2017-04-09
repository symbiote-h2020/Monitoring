package eu.h2020.symbiote.db;

import org.springframework.data.mongodb.repository.MongoRepository;

import eu.h2020.symbiotelibraries.cloud.model.current.CloudResource;





/**
 * Created by jose on 27/09/16.
 */
/**! \class ResourceRepository 
 * \brief ResourceRepository interface to connect with the mongodb database where the registered resources will be stored
 * within the platform
 **/
public interface ResourceRepository extends MongoRepository<CloudResource, String> {

	//! Retrieves a \a ResourceBean.
	/*!
	 * The getByInternalId method retrieves \a ResourceBean identified by the \a resourceId parameter from the   
	 * mondodb database 
	 *
	 * \param resourceId id from the resource to be retrieved 
	 * \return \a getByInternalId returns the \a ResourceBean identified by  \a resourceId
	 */
  CloudResource getByInternalId(String resourceId);

}

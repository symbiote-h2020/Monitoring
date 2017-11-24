package eu.h2020.symbiote.db;


import eu.h2020.symbiote.beans.FederationInfo;

import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * Created by Fernando on 16/10/17.
 */
/**! \class ResourceRepository 
 * \brief ResourceRepository interface to connect with the mongodb database where the registered resources will be stored
 * within the platform
 **/
public interface FederationInfoRepository extends MongoRepository<FederationInfo, String> {
	
	
	FederationInfo findByFederationId(String federationId);
	
}

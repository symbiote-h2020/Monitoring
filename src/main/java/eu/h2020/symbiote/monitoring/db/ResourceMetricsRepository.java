package eu.h2020.symbiote.monitoring.db;

import eu.h2020.symbiote.monitoring.beans.CloudMonitoringResource;

import org.springframework.data.mongodb.repository.MongoRepository;





/**
 * Created by jose on 27/09/16.
 */
/**! \class ResourceMetricsRepository
 * \brief ResourceMetricsRepository interface to connect with the mongodb database where the registered resources will be stored
 * within the platform
 **/
public interface ResourceMetricsRepository extends MongoRepository<CloudMonitoringResource, String> {


}

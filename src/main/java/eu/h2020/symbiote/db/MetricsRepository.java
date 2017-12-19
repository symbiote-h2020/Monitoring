package eu.h2020.symbiote.db;


import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface MetricsRepository extends MongoRepository<DeviceMetric, String> {

	
}

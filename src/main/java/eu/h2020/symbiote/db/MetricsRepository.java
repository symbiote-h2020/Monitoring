package eu.h2020.symbiote.db;


import eu.h2020.symbiote.beans.MonitoringMetric;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface MetricsRepository extends MongoRepository<MonitoringMetric, String> {

	
}

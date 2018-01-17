package eu.h2020.symbiote.monitoring.db;

import eu.h2020.symbiote.monitoring.beans.FederationInfo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FederationInfoRepository extends MongoRepository<FederationInfo, String> {
}

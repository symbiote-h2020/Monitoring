package eu.h2020.symbiote.db;

import eu.h2020.symbiote.beans.FederationInfo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FederationInfoRepository extends MongoRepository<FederationInfo, String> {
}

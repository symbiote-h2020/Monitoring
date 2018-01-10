package eu.h2020.symbiote.db;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CloudResourceRepository extends MongoRepository<CloudResource, String> {
  
  CloudResource findByResourceId(String symbioteId);
  
}

package eu.h2020.symbiote.db;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CloudResourceRepository extends MongoRepository<CloudResource, String> {
  

  List<CloudResource> findByParamsType(String type);
}

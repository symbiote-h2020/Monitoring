package eu.h2020.symbiote.monitoring.tests;

import eu.h2020.symbiote.cloud.model.CloudResourceParams;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.model.cim.Resource;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {
  
  public static final String SYMBIOTE_PREFIX = "symbiote_";
  public static final String RESOURCE_TYPE = "type";
  
  public static CloudResource createResource(String id, String type) {
    CloudResource resource = new CloudResource();
  
    resource.setInternalId(id);
  
    CloudResourceParams params = new CloudResourceParams();
    params.setType(type);
    resource.setParams(params);
  
    Resource r = new Resource();
    r.setId(SYMBIOTE_PREFIX+id);
    r.setInterworkingServiceURL("http://tests.io/interworking/url");
    List<String> comments = new ArrayList<String>();
    comments.add("comment1");
    comments.add("comment2");
//		r.setComments(comments);
    List<String> labels = new ArrayList<String>();
    labels.add("label1");
    labels.add("label2");
//		r.setLabels(labels);
    resource.setResource(r);
  
    return resource;
  
  }
  
  public static CloudResource createResource(String id) {
    return createResource(id, RESOURCE_TYPE+"1");
  }
  
}
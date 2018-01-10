package eu.h2020.symbiote.db;

import com.mongodb.BulkWriteError;
import com.mongodb.BulkWriteException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;

import eu.h2020.symbiote.beans.CloudMonitoringResource;
import eu.h2020.symbiote.beans.TimedValue;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.compat.FiltersCompat;
import eu.h2020.symbiote.compat.ProjectionsCompat;
import eu.h2020.symbiote.utils.MonitoringUtils;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MongoDbMonitoringBackend {
  
  public static final String DEVICE_ID = "deviceId";
  public static final String DEVICE_METRICS = "deviceMetrics";
  public static final String FIRST_DAY = "firstDay";
  public static final String LAST_DAY = "lastDay";
  public static final String TAG = "tag";
  private MongoCollection<Document> collection;
  private MongoClient client;
  
  public MongoDbMonitoringBackend(String connectionUri, String database, String collection) {
    client = new MongoClient(connectionUri,
        MongoClientOptions.builder().codecRegistry(
            CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )).build());
    this.collection = client.getDatabase(database).getCollection(collection);
  }
  
  public MongoCollection<Document> getCollection() {
    return collection;
  }
  
  public MongoClient getClient() {
    return client;
  }
  
  public List<DeviceMetric> saveMetrics(List<DeviceMetric> metrics) {
    Map<String, CloudMonitoringResource> resources = MonitoringUtils.toResourceMap(metrics);
    
    List<DeviceMetric> result = new ArrayList<>(metrics);
  
    List<UpdateOneModel<Document>> ops = new ArrayList<>();
  
    resources.values().forEach(resource -> {
      String resourceId = resource.getResource();
      resource.getMetrics().forEach((metric, days) -> {
        days.forEach((day, values) -> {
        
          ops.add(new UpdateOneModel<>(
              new Document("_id", resourceId),
              new Document("$push",
                  new Document("deviceMetrics."+metric+"."+day,
                      new Document("$each", values))),
              new UpdateOptions().upsert(true)));
        });
      });
    });
  
    BulkWriteOptions options = new BulkWriteOptions().bypassDocumentValidation(true).ordered(false);
    try {
      collection.bulkWrite(ops, options);
    } catch (BulkWriteException e) {
      com.mongodb.BulkWriteResult exceptionResult = e.getWriteResult();
      for (BulkWriteError error : e.getWriteErrors()) {
        int index = error.getIndex();
        if (index < ops.size()) {
          UpdateOneModel operation = ops.get(index);
          result.removeAll(getMetricsFromOperation(operation));
        }
      }
    }
    return result;
  }
  
  private Collection<DeviceMetric> getMetricsFromOperation(UpdateOneModel operation) {
    List<DeviceMetric> metricList = new ArrayList<>();
    String deviceId = operation.getFilter()
                          .toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry())
                          .getString("_id").getValue();
    Document push = ((Document) operation.getUpdate())
                                  .get("$push", Document.class);
    
    String dayKey = push.keySet().iterator().next();
    String[] path = dayKey.split(".");
    String tag = path[1];
    
    Document valuesDocument = push.get(dayKey, Document.class);
    
    List<TimedValue> values = (List<TimedValue>) valuesDocument.get("$each");
    
    for (TimedValue value : values) {
      DeviceMetric metric = new DeviceMetric();
      metric.setDate(value.getDate());
      metric.setValue(value.getValue());
      metric.setTag(tag);
      metric.setDeviceId(deviceId);
      
      metricList.add(metric);
    }
    
    return metricList;
  }
  
  List<Document> debugPipeline(List<Bson> pipeline) {
    List<Document> result = new ArrayList<>();
    collection.aggregate(pipeline, Document.class).into(result);
    return result;
  }
  
  private String field(String field) {
    return "$" + field;
  }
  
  private String obj(String field) {
    return field + "Obj";
  }
  
  public List<DeviceMetric> getMetrics(List<String> devices, List<String> metrics,
                                Date startDate, Date endDate) {
  
    List<Bson> pipeline = new ArrayList<>();
  
    if (devices != null && !devices.isEmpty()) {
      pipeline.add(Aggregates.match(Filters.in("_id", devices)));
    }
  
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.computed(DEVICE_ID, "$_id"),
        Projections.computed(DEVICE_METRICS,
            ProjectionsCompat.objectToArray(field(DEVICE_METRICS)))
    )));
    
    if (metrics != null && !metrics.isEmpty()) {
      pipeline.add(Aggregates.project(Projections.fields(
          Projections.include(DEVICE_ID, DEVICE_METRICS),
          Projections.computed(DEVICE_METRICS,
              ProjectionsCompat.filter(field(DEVICE_METRICS), "metric",
                  FiltersCompat.in(field("$metric.k"), metrics)))
      )));
    }
  
    pipeline.add(Aggregates.unwind(field(DEVICE_METRICS)));
  
    
    
    if (startDate  != null && endDate != null
            && MonitoringUtils.getDateWithoutTime(startDate).equals(
                MonitoringUtils.getDateWithoutTime(endDate))) {
      // Special shortcut. We can shortcut here as we only need to filter one day
      return getDayMetrics(startDate, endDate, pipeline);
    }
  
    Document rangeCondition = manageDate(startDate, FIRST_DAY, pipeline);
  
    Document endCondition = manageDate(endDate, LAST_DAY, pipeline);
    
    if (rangeCondition != null && endCondition != null) {
      rangeCondition = FiltersCompat.and(rangeCondition, endCondition);
    } else {
      if (endCondition != null) {
        rangeCondition = endCondition;
      }
    }
  
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.include(DEVICE_ID),
        Projections.computed(TAG, field(DEVICE_METRICS+".k")),
        Projections.computed("values",
            ProjectionsCompat.objectToArray(field(DEVICE_METRICS+".v"))),
        Projections.computed(obj(FIRST_DAY), ProjectionsCompat.arrayAsObject(field(FIRST_DAY))),
        Projections.computed(obj(LAST_DAY), ProjectionsCompat.arrayAsObject(field(LAST_DAY)))
    )));
    
    if (rangeCondition != null) {
      pipeline.add(Aggregates.project(Projections.fields(
          Projections.include(DEVICE_ID, TAG, obj(FIRST_DAY), obj(LAST_DAY)),
          Projections.computed("values",
              ProjectionsCompat.filter("$values", "value",
                  rangeCondition))
      )));
    }
  
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.include(DEVICE_ID, TAG),
        Projections.computed("total",
            ProjectionsCompat.concatArrays(field(obj(FIRST_DAY)), "$values", field(obj(LAST_DAY))))
    )));
    
    pipeline.add(Aggregates.unwind("$total"));
    
    pipeline.add(Aggregates.unwind("$total.v"));
    
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.include(DEVICE_ID, TAG),
        Projections.computed("date", "$total.v.date"),
        Projections.computed("value", "$total.v.value")
    )));
    
    List<DeviceMetric> result = new ArrayList<>();
    
    collection.aggregate(pipeline, DeviceMetric.class).into(result);
    return result;
  }
  
  private List<DeviceMetric> getDayMetrics(Date startDate, Date endDate, List<Bson> pipeline) {
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.include(DEVICE_ID),
        Projections.computed(TAG, field(DEVICE_METRICS+".k")),
        Projections.computed("total",
            ProjectionsCompat.filter(field(DEVICE_METRICS+".v.") +
                                         MonitoringUtils.getDateWithoutTime(startDate),
                "metric",
                FiltersCompat.and(
                    FiltersCompat.gte("$$metric.date",startDate)
                    ,FiltersCompat.lte("$$metric.date", endDate)))))));
    //debug = debugPipeline(pipeline);
    
    pipeline.add(Aggregates.unwind("$total"));
    
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.include(DEVICE_ID, TAG),
        Projections.computed("date", "$total.date"),
        Projections.computed("value", "$total.value")
    )));
    
    //debug = debugPipeline(pipeline);
    
    List<DeviceMetric> metrics = new ArrayList<>();
    
    collection.aggregate(pipeline, DeviceMetric.class).into(metrics);
    return metrics;
  }
  
  private Document manageDate(Date date, String field, List<Bson> pipeline) {
    if (date != null) {
      String elem = "metric";
      String condField = "$$"+elem+".date";
      
      String day = MonitoringUtils.getDateWithoutTime(date);
    
      pipeline.add(Aggregates.project(Projections.fields(
          Projections.include(DEVICE_ID, DEVICE_METRICS, FIRST_DAY, LAST_DAY),
          Projections.computed(field, field(DEVICE_METRICS+".v.") + day)
      )));
    
      String cond = (FIRST_DAY.equals(field))?"$gte":"$lte";
      pipeline.add(Aggregates.project(Projections.fields(
          Projections.include(DEVICE_ID, DEVICE_METRICS, FIRST_DAY, LAST_DAY),
          Projections.computed(field,
              ProjectionsCompat.filter(field(field), elem,
                  FiltersCompat.op(cond, condField, date)))
      )));
    
      return FiltersCompat.op(cond.substring(0,3),"$$value.k", day);
    } else {
      pipeline.add(Aggregates.project(Projections.fields(
          Projections.include(DEVICE_ID, DEVICE_METRICS, FIRST_DAY, LAST_DAY),
          Projections.computed(field, new ArrayList<>())
      )));
      return null;
    }
  }
  
}

package eu.h2020.symbiote.compat;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

public class ObjectToArray implements AggregationExpression {
  
  private String arrayField;
  
  public ObjectToArray(String arrayField) {
    this.arrayField = arrayField;
  }
  
  @Override
  public Document toDocument(AggregationOperationContext context) {
    return new Document("$objectToArray", "$"+arrayField);
  }
}

package eu.h2020.symbiote.compat;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

import java.util.Arrays;

public class MetricArrayToObjectArray implements AggregationExpression {
  
  private String arrayName;
  private String field;
  
  public MetricArrayToObjectArray(String arrayName, String field) {
    this.arrayName = arrayName;
    this.field = field;
  }
  
  @Override
  public Document toDocument(AggregationOperationContext context) {
    return new Document(arrayName, Arrays.asList(new Document("v", "$" + field)));
  }
}

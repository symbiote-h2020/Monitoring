package eu.h2020.symbiote.monitoring.compat;

import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class FiltersCompat {
  
  public static Document op(String op, Object op1, Object op2) {
    return new Document(op, Arrays.asList(op1,op2));
  }
  
  public static Document eq(String field, Object value) {
    return op("$eq", field, value);
  }
  
  public static Document gt(String field, Object value) {
    return op("$gt", field, value);
  }
  
  public static Document lt(String field, Object value) {
    return op("$lt", field, value);
  }
  
  public static Document gte(String field, Object value) {
    return op("$gte", field, value);
  }
  
  public static Document lte(String field, Object value) {
    return op("$lte", field, value);
  }
  
  public static <T> Document in(String field, List<T> value) {
    return op("$in", field, value);
  }
  
  public static Document and(Document cond1, Object cond2) {
    return op("$and", cond1, cond2);
  }
  
}

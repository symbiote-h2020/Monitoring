package eu.h2020.symbiote.monitoring.compat;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

public class ProjectionsCompat {
  
  
  public static Document objectToArray(String field) {
    return new Document("$objectToArray", field);
  }
  
  public static Document filter(String array, String as, Bson cond) {
    Document options = new Document("input", array);
    options.put("as", as);
    options.put("cond", cond);
    return new Document("$filter",options);
  }
  
  public static Document concatArrays(String... arrays) {
    return new Document("$concatArrays", Arrays.asList(arrays));
  }
  
  public static List<Document> arrayAsObject(String array) {
    return Arrays.asList(new Document("v", array));
  }
}

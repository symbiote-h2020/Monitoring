/*
 * Copyright 2018 Atos
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

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

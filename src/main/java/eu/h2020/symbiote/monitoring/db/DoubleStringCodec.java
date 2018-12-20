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

package eu.h2020.symbiote.monitoring.db;

import org.apache.commons.lang3.math.NumberUtils;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class DoubleStringCodec implements Codec<String>  {
  @Override
  public String decode(BsonReader reader, DecoderContext decoderContext) {
    if (BsonType.DOUBLE.equals(reader.getCurrentBsonType())) {
      return new Double(reader.readDouble()).toString();
    } else {
      return reader.readString();
    }
  }
  
  @Override
  public void encode(BsonWriter writer, String value, EncoderContext encoderContext) {
    if (NumberUtils.isParsable(value)) {
      writer.writeDouble(new Double(value));
    } else {
      writer.writeString(value);
    }
  }
  
  @Override
  public Class<String> getEncoderClass() {
    return String.class;
  }
}

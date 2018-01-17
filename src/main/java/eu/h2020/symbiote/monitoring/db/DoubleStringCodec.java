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

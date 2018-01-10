package eu.h2020.symbiote.db;

import eu.h2020.symbiote.beans.TimedValue;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.Date;

public class MetricValueCodec implements Codec<TimedValue> {
  @Override
  public TimedValue decode(BsonReader reader, DecoderContext decoderContext) {
    reader.readStartDocument();
    TimedValue value = new TimedValue();
    value.setDate(new Date(reader.readDateTime("date")));
    value.setValue(reader.readString("value"));
    reader.readEndDocument();
    return value;
  }
  
  @Override
  public void encode(BsonWriter writer, TimedValue value, EncoderContext encoderContext) {
    writer.writeStartDocument();
    writer.writeDateTime("date", value.getDate().getTime());
    writer.writeString("value", value.getValue());
    writer.writeEndDocument();
  }
  
  @Override
  public Class<TimedValue> getEncoderClass() {
    return TimedValue.class;
  }
}

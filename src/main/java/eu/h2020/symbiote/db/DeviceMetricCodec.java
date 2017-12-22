package eu.h2020.symbiote.db;

import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.Date;

public class DeviceMetricCodec implements Codec<DeviceMetric> {
  @Override
  public DeviceMetric decode(BsonReader reader, DecoderContext decoderContext) {
    DeviceMetric metric = new DeviceMetric();
    reader.readStartDocument();
    metric.setDeviceId(reader.readString("deviceId"));
    metric.setTag(reader.readString("tag"));
    metric.setValue(reader.readString("value"));
    metric.setDate(new Date(reader.readDateTime("date")));
    reader.readEndDocument();
    return metric;
  }
  
  @Override
  public void encode(BsonWriter writer, DeviceMetric value, EncoderContext encoderContext) {
    writer.writeStartDocument();
    writer.writeString("deviceId", value.getDeviceId());
    writer.writeString("tag", value.getTag());
    writer.writeString("value", value.getValue());
    writer.writeDateTime("date", value.getDate().getTime());
  }
  
  @Override
  public Class<DeviceMetric> getEncoderClass() {
    return DeviceMetric.class;
  }
}

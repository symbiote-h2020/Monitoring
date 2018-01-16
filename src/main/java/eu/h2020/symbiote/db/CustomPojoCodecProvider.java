package eu.h2020.symbiote.db;

import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.PojoCodecProvider;

public class CustomPojoCodecProvider implements CodecProvider {
  
  private PojoCodecProvider pojoCodecProvider;
  
  public CustomPojoCodecProvider() {
    pojoCodecProvider = PojoCodecProvider.builder().automatic(true).register(
        getAutomaticConversionClassModel(TimedValue.class),
        getAutomaticConversionClassModel(DeviceMetric.class)
    ).build();
  }
  
  @Override
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
    return pojoCodecProvider.get(clazz, registry);
  }
  
  public <T> ClassModel<T> getAutomaticConversionClassModel(Class<T> clazz) {
    ClassModelBuilder builder = ClassModel.builder(clazz);
    builder.getProperty("value").codec(new DoubleStringCodec());
    return builder.build();
  }
}

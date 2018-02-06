package eu.h2020.symbiote.monitoring.tests.utils;

import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MonitoringTestUtils {
  
  public static final String DEVICE_PF = "device";
  public static final String TAG_PF = "tag";
  
  public static class GenerationResults {
    private ZonedDateTime firstDate;
    private ZonedDateTime lastDate;
    private List<DeviceMetric> metrics;
  
    public GenerationResults(ZonedDateTime firstDate, ZonedDateTime lastDate, List<DeviceMetric> metrics) {
      this.firstDate = firstDate;
      this.lastDate = lastDate;
      this.metrics = metrics;
    }
  
    public ZonedDateTime getFirstDate() {
      return firstDate;
    }
  
    public void setFirstDate(ZonedDateTime firstDate) {
      this.firstDate = firstDate;
    }
  
    public ZonedDateTime getLastDate() {
      return lastDate;
    }
  
    public void setLastDate(ZonedDateTime lastDate) {
      this.lastDate = lastDate;
    }
  
    public List<DeviceMetric> getMetrics() {
      return metrics;
    }
  
    public void setMetrics(List<DeviceMetric> metrics) {
      this.metrics = metrics;
    }
  }
  
  private static final Log logger = LogFactory.getLog(MonitoringTestUtils.class);
  
  public static <T> T benchmark(String message, Benchmark<T> run) {
    long t1 = System.nanoTime();
    T result = run.execute();
    String elements = "";
    if (result instanceof List) {
      elements = "; " + ((List) result).size() + " elements";
    }
    long elapsed = System.nanoTime() - t1;
    double elapsed_milis = elapsed/1000000.0;
    logger.info(message + elements + ": " + elapsed + " ns; " + elapsed_milis + " ms");
    return result;
  }
  
  public static GenerationResults generateMetrics(int numDevices, int numTags, int numDays, int metricsPerDay) {
  
    ZonedDateTime lastDate = Instant.now().atZone(ZoneId.of("UTC"))
                                 .withHour(0).withMinute(0).withSecond(0).withNano(0);
  
  
    ZonedDateTime firstDate = lastDate.minusDays(numDays);
  
    List<DeviceMetric> metrics = new ArrayList<>();
    
    for (int i=0; i<numDevices; i++) {
      String deviceId = DEVICE_PF+i;
      
      for (int t=0; t<numTags; t++) {
        lastDate = firstDate.minusDays(1);
        String tag = TAG_PF+t;
        
        for (int j = 0; j < numDays; j++) {
          lastDate = lastDate.plusDays(1);
          lastDate = lastDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
          lastDate = lastDate.minusMinutes(1);
    
          for (int k = 0; k < metricsPerDay; k++) {
            lastDate = lastDate.plusMinutes(1);
      
            metrics.add(generateMetric(deviceId, tag, 101,
                Date.from(lastDate.toInstant())));
          }
        }
      }
    }
    
    return new GenerationResults(firstDate, lastDate, metrics);
  }
  
  public static DeviceMetric generateMetric(String deviceId, String tag, int maxValue, Date date) {
    return generateMetric(deviceId, tag, new Double(ThreadLocalRandom.current().nextInt(0,maxValue)), date);
  }
  
  public static DeviceMetric generateMetric(String deviceId, String tag, double value, Date date) {
    DeviceMetric metric = new DeviceMetric();
    metric.setDeviceId(deviceId);
    metric.setDate(date);
    metric.setTag(tag);
    metric.setValue(Double.toString(value));
    return metric;
  }
  
}

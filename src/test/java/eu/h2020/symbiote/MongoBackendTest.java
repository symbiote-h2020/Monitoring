package eu.h2020.symbiote;

import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.db.MongoDbMonitoringBackend;
import eu.h2020.symbiote.utils.MonitoringTestUtils;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MongoBackendTest {

  public static final String DATABASE = "monitoring-test";
  public static final String COLLECTION = "cloudMonitoringResource";
  
  public static final Integer NUM_DEVICES = 10;
  public static final Integer NUM_TAGS = 10;
  public static final Integer NUM_DAYS = 10;
  public static final Integer NUM_METRICS_PER_DAY = 10;
  
  private MongoDbMonitoringBackend backend;
  
  ZonedDateTime lastDate;
  ZonedDateTime firstDate;
  
  @Before
  public void setup() {
    
    backend = new MongoDbMonitoringBackend(null, "metrics-test", "cloudMonitoringResource");
    backend.getCollection().drop();
  
    MonitoringTestUtils.GenerationResults result =
        MonitoringTestUtils.generateMetrics(NUM_DEVICES, NUM_TAGS, NUM_DAYS, NUM_METRICS_PER_DAY);
    
    this.firstDate = result.getFirstDate();
    this.lastDate = result.getLastDate();
    
    List<DeviceMetric> saved = MonitoringTestUtils.benchmark("Insertion of " + result.getMetrics().size() + " elements",
        () -> backend.saveMetrics(result.getMetrics()));
    
    assert result.getMetrics().size() == saved.size();
  }
  
  List<String> getEven(int max, String prefix) {
    List<String> result = new ArrayList<>();
    for (int i=0 ; i < max; i++) {
      if (i%2 == 0) {
        result.add(prefix+i);
      }
    }
    return result;
  }
  
  @Test
  public void testFilters() {
    
    int metricsPerTag = NUM_DAYS * NUM_METRICS_PER_DAY;
    
    int metricsPerDevice = NUM_TAGS * metricsPerTag;
    
    int total = NUM_DEVICES * metricsPerDevice;
    
    List<DeviceMetric> metrics = metrics = MonitoringTestUtils.benchmark("Get by all",
        () ->  backend.getMetrics(null, null, null, null));
    
    assert metrics != null;
    assert metrics.size() == total;
    
    List<String> devices = getEven(NUM_DEVICES, MonitoringTestUtils.DEVICE_PF);
    metrics = MonitoringTestUtils.benchmark("Get by device",
        () ->  backend.getMetrics(devices, null, null, null));
    
    assert metrics.size() == devices.size() * metricsPerDevice;
    metrics.forEach(metric -> {
      assert devices.contains(metric.getDeviceId());
    });
    
    List<String> tags = getEven(NUM_TAGS, MonitoringTestUtils.TAG_PF);
    metrics = MonitoringTestUtils.benchmark("Get by metric",
        () ->  backend.getMetrics(null, tags, null, null));
    
    assert metrics.size() == NUM_DEVICES * tags.size() * metricsPerTag;
    metrics.forEach(metric -> {
      assert tags.contains(metric.getTag());
    });

    metrics = MonitoringTestUtils.benchmark("Get by non existing metric",
        () ->  backend.getMetrics(null, Arrays.asList("nonexistent"), null, null));
    assert metrics.size() == 0;
    
    ZonedDateTime start = firstDate.plusDays(1).plusMinutes(1);
    Date startDate = Date.from(start.toInstant());
    
    metrics = MonitoringTestUtils.benchmark("Get by init date",
        () ->  backend.getMetrics(null, null, startDate, null));
    assert metrics.size() == total - (NUM_DEVICES * NUM_TAGS * (NUM_METRICS_PER_DAY + 1));
    metrics.forEach(metric -> {
      ZonedDateTime metricDate = ZonedDateTime.ofInstant(metric.getDate().toInstant(), ZoneId.of("UTC"));
      assert  start.isBefore(metricDate) || start.isEqual(metricDate);
    });
    
    ZonedDateTime end = lastDate.minusDays(1).minusMinutes(1);
    Date endDate = Date.from(end.toInstant());
    
    metrics = MonitoringTestUtils.benchmark("Get by end date",
        () ->  backend.getMetrics(null, null, null, endDate));
    assert metrics.size() == total - (NUM_TAGS * NUM_DEVICES * (NUM_METRICS_PER_DAY + 1));
    metrics.forEach(metric -> {
      ZonedDateTime metricDate = ZonedDateTime.ofInstant(metric.getDate().toInstant(), ZoneId.of("UTC"));
      assert  end.isAfter(metricDate) || end.isEqual(metricDate);
    });
  
    metrics = MonitoringTestUtils.benchmark("Get by full query group",
        () ->  backend.getMetrics(devices, tags, startDate, endDate));
    int totalMetrics = devices.size() * tags.size() * NUM_DAYS * NUM_METRICS_PER_DAY;
    assert metrics.size() == totalMetrics - devices.size()*tags.size()*2*(NUM_METRICS_PER_DAY + 1);
    metrics.forEach(metric -> {
    
      assert devices.contains(metric.getDeviceId());
      assert tags.contains(metric.getTag());
    
      ZonedDateTime metricDate = ZonedDateTime.ofInstant(metric.getDate().toInstant(), ZoneId.of("UTC"));
    
      assert (start.isBefore(metricDate) || start.isEqual(metricDate))
                 && (end.isAfter(metricDate) || end.isEqual(metricDate));
    
    });
    
    
    String deviceId = MonitoringTestUtils.DEVICE_PF+"0";
    String tag = MonitoringTestUtils.TAG_PF+"0";
    metrics = MonitoringTestUtils.benchmark("Get by full query",
        () ->  backend.getMetrics(Arrays.asList(deviceId),
            Arrays.asList(tag), startDate, endDate));
    assert metrics.size() == (NUM_DAYS * NUM_METRICS_PER_DAY) - 2*(NUM_METRICS_PER_DAY + 1);
    metrics.forEach(metric -> {
      
      assert deviceId.equals(metric.getDeviceId());
      assert tag.equals(metric.getTag());
      
      ZonedDateTime metricDate = ZonedDateTime.ofInstant(metric.getDate().toInstant(), ZoneId.of("UTC"));
      
      assert (start.isBefore(metricDate) || start.isEqual(metricDate))
                 && (end.isAfter(metricDate) || end.isEqual(metricDate));
      
    });
    
  }

}

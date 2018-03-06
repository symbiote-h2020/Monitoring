package eu.h2020.symbiote.monitoring.tests;

import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;
import eu.h2020.symbiote.cloud.monitoring.model.AggregationOperation;
import eu.h2020.symbiote.cloud.monitoring.model.DeviceMetric;
import eu.h2020.symbiote.cloud.monitoring.model.TimedValue;
import eu.h2020.symbiote.monitoring.db.MongoDbMonitoringBackend;
import eu.h2020.symbiote.monitoring.tests.utils.MonitoringTestUtils;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoBackendTest {

  public static final String DATABASE = "monitoring-backend-test";
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
    
    backend = new MongoDbMonitoringBackend(null, DATABASE, COLLECTION);
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
  
    startDate.setTime(firstDate.plusMinutes(1).toInstant().toEpochMilli());
    endDate.setTime(firstDate.plusMinutes(NUM_METRICS_PER_DAY-2).toInstant().toEpochMilli());
  
    metrics = MonitoringTestUtils.benchmark("Get by single day",
        () ->  backend.getMetrics(Arrays.asList(deviceId),
            Arrays.asList(tag), startDate, endDate));
    
    assert metrics.size() == NUM_METRICS_PER_DAY-2;
  
    metrics.forEach(metric -> {
    
      assert deviceId.equals(metric.getDeviceId());
      assert tag.equals(metric.getTag());
    
      assert (startDate.before(metric.getDate()) || startDate.equals(metric.getDate()))
                 && (endDate.after(metric.getDate()) || endDate.equals(metric.getDate()));
    
    });
    
  }
  
  @Test
  public void testAggregations() {
  
    List<String> devices = new ArrayList<>();
    List<String> tags = new ArrayList<>();
    
    for (int i = 0; i < NUM_DEVICES/2; i++) {
      devices.add(MonitoringTestUtils.DEVICE_PF+i);
    }
    
    for (int i=0; i < NUM_TAGS/2; i++) {
      tags.add(MonitoringTestUtils.TAG_PF+i);
    }
  
    ZonedDateTime start = firstDate.plusDays(1).plusMinutes(1);
    Date startDate = Date.from(start.toInstant());
  
    ZonedDateTime end = lastDate.minusDays(1).minusMinutes(1);
    Date endDate = Date.from(end.toInstant());
  
    List<DeviceMetric> raw = MonitoringTestUtils.benchmark("Get by full query several metrics",
        () -> backend.getMetrics(devices,tags, startDate, endDate));
  
    assert raw.size() == (NUM_DEVICES/2 * NUM_TAGS/2) * ((NUM_DAYS * NUM_METRICS_PER_DAY) - 2*(NUM_METRICS_PER_DAY + 1));
    raw.forEach(metric -> {
    
      assert devices.contains(metric.getDeviceId());
      assert tags.contains(metric.getTag());
    
      ZonedDateTime metricDate = ZonedDateTime.ofInstant(metric.getDate().toInstant(), ZoneId.of("UTC"));
    
      assert (start.isBefore(metricDate) || start.isEqual(metricDate))
                 && (end.isAfter(metricDate) || end.isEqual(metricDate));
    
    });
    
    List<AggregationOperation> operations = Arrays.asList(AggregationOperation.AVG, AggregationOperation.MAX);
    List<String> counts = Arrays.asList("100", "50");
  
    Map<String, Map<String, AggregatedMetrics>> rawOrganized = aggregate(raw, operations, counts);
    
    List<AggregatedMetrics> aggregated = MonitoringTestUtils.benchmark("Get aggregated by full query",
        () -> backend.getAggregatedMetrics(devices,tags, startDate, endDate, operations, counts));
    
    assert aggregated.size() == NUM_DEVICES/2 * NUM_TAGS/2;
    
    aggregated.forEach(aggregation -> {
      assert devices.contains(aggregation.getDeviceId());
      assert tags.contains(aggregation.getTag());
      
      assert aggregation.getValues().size() == (NUM_DAYS * NUM_METRICS_PER_DAY) - 2*(NUM_METRICS_PER_DAY + 1);
      
      assert rawOrganized.get(aggregation.getDeviceId()) != null;
      
      assert rawOrganized.get(aggregation.getDeviceId()).get(aggregation.getTag()) != null;
      
      compareAggregations(rawOrganized.get(aggregation.getDeviceId()).get(aggregation.getTag()), aggregation);
      
    });
    
  }
  
  private void compareAggregations(AggregatedMetrics toCompare, AggregatedMetrics aggregation) {
    assert toCompare != null;
    
    assert toCompare.getDeviceId().equals(aggregation.getDeviceId());
    
    assert toCompare.getTag().equals(aggregation.getTag());
    
    assert toCompare.getValues().size() == aggregation.getValues().size();
    
    compareValues(toCompare.getValues(), aggregation.getValues());
    
    compareMap(toCompare.getStatistics(), aggregation.getStatistics());
  
    compareMap(toCompare.getCounts(), aggregation.getCounts());
  }
  
  private <T> void  compareMap(Map<String, T> toCompare, Map<String, T> map) {
    
    toCompare.forEach((key, value) -> {
      map.get(key).equals(value);
    });
    
  }
  
  private void compareValues(List<TimedValue> toCompare, List<TimedValue> values) {
    
    List<TimedValue> toRemove = new ArrayList<>(toCompare);
    
    values.forEach(value -> {
      toRemove.removeIf(comparison -> {
        return value.getDate().equals(comparison.getDate()) && value.getValue().equals(comparison.getValue());
      });
    });
    
    assert toRemove.isEmpty();
  }
  
  Map<String, Map<String, AggregatedMetrics>> aggregate(List<DeviceMetric> raw, List<AggregationOperation> operations, List<String> counts) {
    Map<String, Map<String, AggregatedMetrics>> result = new HashMap<>();
    
    raw.forEach(deviceMetric -> {
      Map<String, AggregatedMetrics> deviceMetrics = result.get(deviceMetric.getDeviceId());
      if (deviceMetrics == null) {
        deviceMetrics = new HashMap<>();
        result.put(deviceMetric.getDeviceId(), deviceMetrics);
      }
      
      AggregatedMetrics tagMetrics = deviceMetrics.get(deviceMetric.getTag());
      if (tagMetrics == null) {
        tagMetrics = new AggregatedMetrics();
        tagMetrics.setDeviceId(deviceMetric.getDeviceId());
        tagMetrics.setTag(deviceMetric.getTag());
        deviceMetrics.put(deviceMetric.getTag(), tagMetrics);
      }
      
      addMetric(deviceMetric, tagMetrics, counts);
      
    });
    
    result.values().forEach(tag -> {
      tag.values().forEach(aggregation -> {
        DoubleSummaryStatistics statistics = aggregation.getValues().stream()
                                                 .mapToDouble(value -> new Double(value.getValue()))
                                                 .summaryStatistics();
        operations.forEach(operation -> {
          Double value = null;
          switch (operation) {
            case AVG:
              value = statistics.getAverage();
              break;
            case MAX:
              value = statistics.getMax();
              break;
            case MIN:
              value = statistics.getMin();
              break;
            case SUM:
              value = statistics.getSum();
              break;
          }
          aggregation.getStatistics().put(operation.toString(), value);
        });
      });
    });
    
    return result;
  }
  
  private void addMetric(DeviceMetric deviceMetric, AggregatedMetrics tagMetrics, List<String> counts) {
  
    TimedValue value = new TimedValue();
    value.setDate(deviceMetric.getDate());
    value.setValue(deviceMetric.getValue());
    
    tagMetrics.getValues().add(value);
    
    for (String count : counts) {
      if (count.equals(deviceMetric.getValue())) {
        Integer actual = tagMetrics.getCounts().get(count);
        if (actual == null) {
          tagMetrics.getCounts().put(count, 0);
        } else {
          tagMetrics.getCounts().put(count, actual + 1);
        }
      }
    }
  }
  
}

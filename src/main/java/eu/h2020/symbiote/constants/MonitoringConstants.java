package eu.h2020.symbiote.constants;

public class MonitoringConstants {
	
	public static final String AVAILABILITY_TAG = "availability";
	public static final String LOAD_TAG = "load";
	public static final String ALL_QUALIFIER = "all";

	public static final String PUBLISH_MONITORING_DATA = "crm/monitoring/{platformId}/devices/status";
	public static final String METRICS_DATA = "monitoring/metrics/raw";
	public static final String AGGREGATED_DATA = "monitoring/metrics/aggregated";
	public static final String SUMMARY_DATA = "monitoring/metrics/summary";
	
	public static final String EXCHANGE_NAME_RH = "symbIoTe.rh";

	public static final String MONITORING_REGISTRATION_QUEUE_NAME = "symbIoTe.rh.monitoring.registration";
	public static final String MONITORING_UNREGISTRATION_QUEUE_NAME = "symbIoTe.rh.monitoring.unregistration";
	public static final String MONITORING_SHARING_QUEUE_NAME = "symbIoTe.rh.monitoring.sharing";
	public static final String MONITORING_UNSHARING_QUEUE_NAME = "symbIoTe.rh.monitoring.unsharing";
	
	public static final String RESOURCE_REGISTRATION_KEY = "symbiote.rh.resource_registration";
	public static final String RESOURCE_UNREGISTRATION_KEY = "symbiote.rh.resource_unregistration";
	
	public static final String RESOURCE_SHARING_KEY = "symbiote.rh.resource_sharing";
	public static final String RESOURCE_UNSHARING_KEY = "symbiote.rh.resource_unsharing";
	
}

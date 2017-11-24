package eu.h2020.symbiote.constants;

public class MonitoringConstants {
	
	public static final String CORE_FED_ID = "{{core}}";
	public static final String AVAILABILITY_TAG = "availability";
	public static final String LOAD_TAG = "load";

	public static final String PUBLISH_MONITORING_DATA = "crm/monitoring/{platformId}/devices/status";
	public static final String METRICS_DATA = "monitoring/metrics";
	
	public static final String EXCHANGE_NAME_REGISTRATION = "symbIoTe.rap";
	public static final String EXCHANGE_NAME_UNREGISTRATION = "symbIoTe.rap";
	public static final String EXCHANGE_NAME_UPDATED = "symbIoTe.rap";

	public static final String RESOURCE_REGISTRATION_QUEUE_NAME = "symbIoTe.monitoring.registrationHandler.register_resources";
	public static final String RESOURCE_UNREGISTRATION_QUEUE_NAME = "symbIoTe.monitoring.registrationHandler.unregister_resources";
	public static final String RESOURCE_UPDATED_QUEUE_NAME = "symbIoTe.monitoring.registrationHandler.update_resources";

	public static final String RESOURCE_REGISTRATION_ROUTING_KEY = "symbIoTe.rap.registrationHandler.register_resources";
	public static final String RESOURCE_UNREGISTRATION_ROUTING_KEY = "symbIoTe.rap.registrationHandler.unregister_resources";
	public static final String RESOURCE_UPDATED_ROUTING_KEY = "symbIoTe.rap.registrationHandler.update_resources";
		
	public static final String EXCHANGE_NAME_REGISTRATION_TEST = "symbIoTe_test.rh.reg";
	public static final String EXCHANGE_NAME_UNREGISTRATION_TEST = "symbIoTe_test.rh.unreg";
	public static final String EXCHANGE_NAME_UPDATED_TEST = "symbIoTe_test.rh.update";
	
	public static final String RESOURCE_REGISTRATION_QUEUE_NAME_TEST = "symbIoTe_test.monitoring.registrationHandler.register_resources";
	public static final String RESOURCE_UNREGISTRATION_QUEUE_NAME_TEST = "symbIoTe_test.monitoring.registrationHandler.unregister_resources";
	public static final String RESOURCE_UPDATED_QUEUE_NAME_TEST = "symbIoTe_test.monitoring.registrationHandler.update_resources";

}

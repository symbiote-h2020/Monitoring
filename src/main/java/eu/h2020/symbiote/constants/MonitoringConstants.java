package eu.h2020.symbiote.constants;

public class MonitoringConstants {

	public static final String PLATFORM_ID = "platformId1";
	public static final String RESOURCE_ID = "id";
//    public static final String DO_CREATE_RESOURCES="/platforms/{platformId}/resources";
//	public static final String DO_UPDATE_RESOURCES = "/platforms/{platformId}/resources";
//	public static final String DO_REMOVE_RESOURCES = "/platforms/{platformId}/resources";
	public static final String PUBLISH_MONITORING_DATA = "crm/monitoring/{platformId}/devices/status";
	public static final String SUBSCRIBE_MONITORING_DATA = "monitoring/{platformId}";
	public static final String SUBSCRIBE_REQUEST_MONITORING_DATA = "monitoring/getdata/{platformId}";
	public static final String PUBLISH_SLAM_MONITORING_DATA = "slam/monitoring/{platformId}";

	
	// public static final String EXCHANGE_NAME_REGISTRATION = "symbIoTe.rh.reg";
	// public static final String EXCHANGE_NAME_UNREGISTRATION = "symbIoTe.rh.unreg";
	// public static final String EXCHANGE_NAME_UPDATED = "symbIoTe.rh.update";
	
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

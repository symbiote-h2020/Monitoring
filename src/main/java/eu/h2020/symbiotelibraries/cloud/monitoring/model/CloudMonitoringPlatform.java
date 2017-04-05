package eu.h2020.symbiotelibraries.cloud.monitoring.model;

public class CloudMonitoringPlatform {

	//platformID
	private String internalId;
	
	private CloudMonitoringDevice[] devices;
	
	public CloudMonitoringPlatform(){
		
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public CloudMonitoringDevice[] getDevices() {
		return devices;
	}

	public void setDevices(CloudMonitoringDevice[] devices) {
		this.devices = devices;
	}
	
	
}

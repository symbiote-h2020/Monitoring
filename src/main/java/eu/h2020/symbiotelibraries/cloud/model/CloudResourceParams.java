package eu.h2020.symbiotelibraries.cloud.model;

public class CloudResourceParams {

	private String symbiote_id;
	
	private String device_name;
	
	private String ip_address;
	
	public CloudResourceParams(){
		
	}

	public String getSymbiote_id() {
		return symbiote_id;
	}

	public void setSymbiote_id(String symbiote_id) {
		this.symbiote_id = symbiote_id;
	}

	public String getDevice_name() {
		return device_name;
	}

	public void setDevice_name(String device_name) {
		this.device_name = device_name;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}
	
	
	
}

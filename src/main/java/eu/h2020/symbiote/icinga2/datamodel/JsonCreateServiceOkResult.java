package eu.h2020.symbiote.icinga2.datamodel;

public class JsonCreateServiceOkResult {
	
	private double code;
	
	private String status;
	
	public JsonCreateServiceOkResult(){
		
	}

	public double getCode() {
		return code;
	}

	public void setCode(double code) {
		this.code = code;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}

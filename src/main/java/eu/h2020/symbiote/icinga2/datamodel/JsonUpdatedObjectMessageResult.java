package eu.h2020.symbiote.icinga2.datamodel;

public class JsonUpdatedObjectMessageResult {

	private double code;
	
	private String name;
	
	private String status;
	
	private String type;
	
	public JsonUpdatedObjectMessageResult(){
		
	}

	public double getCode() {
		return code;
	}

	public void setCode(double code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}

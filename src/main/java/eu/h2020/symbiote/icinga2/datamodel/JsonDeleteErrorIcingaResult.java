package eu.h2020.symbiote.icinga2.datamodel;

public class JsonDeleteErrorIcingaResult extends JsonDeleteMessageIcingaResult {

	private String[] errors;
	
	public JsonDeleteErrorIcingaResult(){
		super();
	}

	public String[] getErrors() {
		return errors;
	}

	public void setErrors(String[] errors) {
		this.errors = errors;
	}
	
}

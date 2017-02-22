package eu.h2020.symbiote.icinga2.datamodel;

public class JsonDeleteMessageIcingaResponse {

	
	private JsonDeleteMessageIcingaResult[] results;
	
	public JsonDeleteMessageIcingaResponse(){
		
	}

	public JsonDeleteMessageIcingaResult[] getResults() {
		return results;
	}

	public void setResults(JsonDeleteMessageIcingaResult[] results) {
		this.results = results;
	}
	
}

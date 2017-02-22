package eu.h2020.symbiote.icinga2.datamodel;

public class JsonDeleteErrorIcingaResponse {

	
	private JsonDeleteErrorIcingaResult[] results;
	
	public JsonDeleteErrorIcingaResponse(){
		
	}

	public JsonDeleteErrorIcingaResult[] getResults() {
		return results;
	}

	public void setResults(JsonDeleteErrorIcingaResult[] results) {
		this.results = results;
	}
	
}

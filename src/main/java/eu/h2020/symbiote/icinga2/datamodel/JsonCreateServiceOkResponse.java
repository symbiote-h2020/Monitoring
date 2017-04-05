package eu.h2020.symbiote.icinga2.datamodel;

public class JsonCreateServiceOkResponse {

	private JsonCreateServiceOkResult[] results;
	
	public JsonCreateServiceOkResponse(){
		
	}

	public JsonCreateServiceOkResult[] getResults() {
		return results;
	}

	public void setResults(JsonCreateServiceOkResult[] results) {
		this.results = results;
	}
	
}

package eu.h2020.symbiote.icinga2.datamodel;

import eu.h2020.symbiote.icinga2.datamodel.service.JsonServiceResult;

public class JsonServicesResponse {

	private JsonServiceResult[] results;

	public JsonServicesResponse(){
		
	}

	public JsonServiceResult[] getResults() {
		return results;
	}

	public void setResults(JsonServiceResult[] results) {
		this.results = results;
	}
	
	
}

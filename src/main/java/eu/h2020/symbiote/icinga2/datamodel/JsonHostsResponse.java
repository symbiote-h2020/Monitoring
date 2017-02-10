package eu.h2020.symbiote.icinga2.datamodel;

import eu.h2020.symbiote.icinga2.datamodel.host.JsonHostResult;

public class JsonHostsResponse {

	private JsonHostResult[] results;
	
	public JsonHostsResponse(){
		
	}

	public JsonHostResult[] getResults() {
		return results;
	}

	public void setResults(JsonHostResult[] results) {
		this.results = results;
	}
}

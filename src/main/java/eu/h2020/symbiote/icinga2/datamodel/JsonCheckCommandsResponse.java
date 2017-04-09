package eu.h2020.symbiote.icinga2.datamodel;

import eu.h2020.symbiote.icinga2.datamodel.checkcommand.JsonCheckCommandsResult;

public class JsonCheckCommandsResponse {

	private JsonCheckCommandsResult[] results;

	public JsonCheckCommandsResponse(){
		
	}

	public JsonCheckCommandsResult[] getResults() {
		return results;
	}

	public void setResults(JsonCheckCommandsResult[] results) {
		this.results = results;
	}
}

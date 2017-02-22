package eu.h2020.symbiote.icinga2.datamodel;

public class JsonUpdatedObjectMessageResponse {

	private JsonUpdatedObjectMessageResult[] results;

	public JsonUpdatedObjectMessageResponse(){
		
	}

	public JsonUpdatedObjectMessageResult[] getResults() {
		return results;
	}

	public void setResults(JsonUpdatedObjectMessageResult[] results) {
		this.results = results;
	}
}

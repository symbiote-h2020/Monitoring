package eu.h2020.symbiote.icinga2.datamodel.host.ip;

public class JsonIpByHostResponse {

	private JsonIpByHostResult[] results;
	
	public JsonIpByHostResponse(){
		
	}

	public JsonIpByHostResult[] getResults() {
		return results;
	}

	public void setResults(JsonIpByHostResult[] results) {
		this.results = results;
	}
}

package eu.h2020.symbiote.icinga2.datamodel.host.ip;

public class JsonHostByIpResponse {

	private JsonHostByIpResult[] results;
	
	public JsonHostByIpResponse(){
		
	}

	public JsonHostByIpResult[] getResults() {
		return results;
	}

	public void setResults(JsonHostByIpResult[] results) {
		this.results = results;
	}
}

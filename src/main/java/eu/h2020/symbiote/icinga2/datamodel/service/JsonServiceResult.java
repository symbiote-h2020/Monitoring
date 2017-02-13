package eu.h2020.symbiote.icinga2.datamodel.service;

import eu.h2020.symbiote.beans.ServiceBean;

public class JsonServiceResult {

	private ServiceBean attrs;
	private JsonServiceJoins joins;
	private Object meta;
	private String name;
	private String type;
	
	public JsonServiceResult(){
		
	}

	public ServiceBean getAttrs() {
		return attrs;
	}

	public void setAttrs(ServiceBean attrs) {
		this.attrs = attrs;
	}

	public JsonServiceJoins getJoins() {
		return joins;
	}

	public void setJoins(JsonServiceJoins joins) {
		this.joins = joins;
	}

	public Object getMeta() {
		return meta;
	}

	public void setMeta(Object meta) {
		this.meta = meta;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}

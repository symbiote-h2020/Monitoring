package eu.h2020.symbiote.icinga2.datamodel.checkcommand;

import eu.h2020.symbiote.icinga2.datamodel.service.JsonServiceJoins;

public class JsonCheckCommandsResult {

	private CheckCommandAttrs attrs;
	private Object joins;
	private Object meta;
	private String name;
	private String type;
	
	public JsonCheckCommandsResult(){
		
	}

	public CheckCommandAttrs getAttrs() {
		return attrs;
	}

	public void setAttrs(CheckCommandAttrs attrs) {
		this.attrs = attrs;
	}

	public Object getJoins() {
		return joins;
	}

	public void setJoins(Object joins) {
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

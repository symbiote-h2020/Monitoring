package eu.h2020.symbiote.icinga2.datamodel.host.ip;

public class JsonIpByHostResult {

	private JsonIpByHostAttrs attrs;
	private Object joins;
	private Object meta;
	private String name;
	private String type;
	
	public JsonIpByHostResult(){
		
	}

	public JsonIpByHostAttrs getAttrs() {
		return attrs;
	}

	public void setAttrs(JsonIpByHostAttrs attrs) {
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

package eu.h2020.symbiote.icinga2.datamodel.host;

public class JsonHostAttrs {
	
	private boolean active;
	private String address;
	private String[] groups;
	private double last_check;
	private String name;
	
	public JsonHostAttrs(){
		
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String[] getGroups() {
		return groups;
	}

	public void setGroups(String[] groups) {
		this.groups = groups;
	}

	public double getLast_check() {
		return last_check;
	}

	public void setLast_check(double last_check) {
		this.last_check = last_check;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	

}

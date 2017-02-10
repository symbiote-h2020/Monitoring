package eu.h2020.symbiote.beans;

public class HostBean {
	
	private String name;
	
	private String address;
	
	private boolean active;
	
	private String last_check;
	
	private String[] groups;

	
	public HostBean(){
		
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

	public String getLast_check() {
		return last_check;
	}

	public void setLast_check(String last_check) {
		this.last_check = last_check;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

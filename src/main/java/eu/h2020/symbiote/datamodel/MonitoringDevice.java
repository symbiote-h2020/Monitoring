package eu.h2020.symbiote.datamodel;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;

public class MonitoringDevice {

	//Platform Device id
	private String internalId;
	private String type;
	private Date timemetric;
	private String tag;
	private int value;
	@Field("count") int countvalue;
	
	public String getInternalId() {
		return internalId;
	}
	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}
	public Date getTimemetric() {
		return timemetric;
	}
	public void setTimemetric(Date timemetric) {
		this.timemetric = timemetric;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getCountvalue() {
		return countvalue;
	}
	public void setCountvalue(int countvalue) {
		this.countvalue = countvalue;
	}
	
}

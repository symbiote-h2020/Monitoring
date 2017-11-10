package eu.h2020.symbiote.datamodel;

import java.util.Date;

public class MonitoringFedDev {
	
	private String idfed; 
	private Date datefed; 
	private String iddev;
	
	public String getIdfed() {
		return idfed;
	}
	public void setIdfed(String idfed) {
		this.idfed = idfed;
	}
	public Date getDatefed() {
		return datefed;
	}
	public void setDatefed(Date datefed) {
		this.datefed = datefed;
	}
	public String getIddev() {
		return iddev;
	}
	public void setIddev(String iddev) {
		this.iddev = iddev;
	}	
	
}

package eu.h2020.symbiote.datamodel;

import java.util.Date;

public class MonitoringRequest {
	
	private String tag;
	private String federationId;
	private Date dateFederationCreation;
	
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getFederationId() {
		return federationId;
	}
	public void setFederationId(String federationId) {
		this.federationId = federationId;
	}
	public Date getDateFederationCreation() {
		return dateFederationCreation;
	}
	public void setDateFederationCreation(Date dateFederationCreation) {
		this.dateFederationCreation = dateFederationCreation;
	}
	
}

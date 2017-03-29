package eu.h2020.symbiote.beans;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringResource;
import eu.h2020.symbiote.cloud.monitoring.model.resource.JsonServiceLastCheckResult;
import eu.h2020.symbiote.cloud.monitoring.model.resource.JsonServiceVars;

//import eu.h2020.symbiote.cloud.monitoring.model.

public class ServiceBean {

	private boolean active;
	
	private String check_command;
	
	private double check_interval;
	
	private String display_name;
	
	private String last_check;
	
	private JsonServiceLastCheckResult last_check_result;
	
	public ServiceBean(){
		
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getCheck_command() {
		return check_command;
	}

	public void setCheck_command(String check_command) {
		this.check_command = check_command;
	}

	public double getCheck_interval() {
		return check_interval;
	}

	public void setCheck_interval(double check_interval) {
		this.check_interval = check_interval;
	}

	public String getLast_check() {
		return last_check;
	}

	public void setLast_check(String last_check) {
		this.last_check = last_check;
	}

	public JsonServiceLastCheckResult getLast_check_result() {
		return last_check_result;
	}

	public void setLast_check_result(JsonServiceLastCheckResult last_check_result) {
		this.last_check_result = last_check_result;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}
	
	public CloudMonitoringResource toCloudMonitoringResource(){
		CloudMonitoringResource cmr = new CloudMonitoringResource();
		cmr.setActive(this.isActive());
		cmr.setCheck_command(this.getCheck_command());
		cmr.setCheck_interval(this.getCheck_interval());
		cmr.setDisplay_name(this.getDisplay_name());
		cmr.setLast_check(this.getLast_check());
		
		cmr.setLast_check_result(new JsonServiceLastCheckResult());
		cmr.getLast_check_result().setActive(this.getLast_check_result().isActive());
		cmr.getLast_check_result().setCheck_source(this.getLast_check_result().getCheck_source());
		cmr.getLast_check_result().setCommand(this.getLast_check_result().getCommand());
		cmr.getLast_check_result().setExecution_end(this.getLast_check_result().getExecution_end());
		cmr.getLast_check_result().setExecution_start(this.getLast_check_result().getExecution_start());
		cmr.getLast_check_result().setExit_status(this.getLast_check_result().getExit_status());
		cmr.getLast_check_result().setOutput(this.getLast_check_result().getOutput());
		cmr.getLast_check_result().setSchedule_end(this.getLast_check_result().getSchedule_end());
		cmr.getLast_check_result().setSchedule_start(this.getLast_check_result().getSchedule_start());
		cmr.getLast_check_result().setState(this.getLast_check_result().getState());
		cmr.getLast_check_result().setType(this.getLast_check_result().getType());
		
		cmr.getLast_check_result().setVars_after(new JsonServiceVars());
		cmr.getLast_check_result().getVars_after().setAttempt(this.getLast_check_result().getVars_after().getAttempt());
		cmr.getLast_check_result().getVars_after().setReachable(this.getLast_check_result().getVars_after().isReachable());
		cmr.getLast_check_result().getVars_after().setState(this.getLast_check_result().getVars_after().getState());
		cmr.getLast_check_result().getVars_after().setState_type(this.getLast_check_result().getVars_after().getState_type());

		cmr.getLast_check_result().setVars_before(new JsonServiceVars());
		cmr.getLast_check_result().getVars_before().setAttempt(this.getLast_check_result().getVars_before().getAttempt());
		cmr.getLast_check_result().getVars_before().setReachable(this.getLast_check_result().getVars_before().isReachable());
		cmr.getLast_check_result().getVars_before().setState(this.getLast_check_result().getVars_before().getState());
		cmr.getLast_check_result().getVars_before().setState_type(this.getLast_check_result().getVars_before().getState_type());

		return cmr;
	}
}

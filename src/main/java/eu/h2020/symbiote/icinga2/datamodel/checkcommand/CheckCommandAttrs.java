package eu.h2020.symbiote.icinga2.datamodel.checkcommand;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CheckCommandAttrs {
	
	private String __name;
	private boolean active;
	private Object arguments;
	private String[] command;
	private Object env;
	private Object execute;
	private double ha_mode;
	private String name;
	private Object original_attributes;
	@JsonProperty("package")
	private String _package;
	private boolean paused;
	private String[] templates;
	private double timeout;
	private String type;
	private Object vars;
	private double version;
	private String zone;
	
	public CheckCommandAttrs(){
		
	}

	public String get__name() {
		return __name;
	}

	public void set__name(String __name) {
		this.__name = __name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Object getArguments() {
		return arguments;
	}

	public void setArguments(Object arguments) {
		this.arguments = arguments;
	}

	public String[] getCommand() {
		return command;
	}

	public void setCommand(String[] command) {
		this.command = command;
	}

	public Object getEnv() {
		return env;
	}

	public void setEnv(Object env) {
		this.env = env;
	}

	public Object getExecute() {
		return execute;
	}

	public void setExecute(Object execute) {
		this.execute = execute;
	}

	public double getHa_mode() {
		return ha_mode;
	}

	public void setHa_mode(double ha_mode) {
		this.ha_mode = ha_mode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getOriginal_attributes() {
		return original_attributes;
	}

	public void setOriginal_attributes(Object original_attributes) {
		this.original_attributes = original_attributes;
	}

	public String get_package() {
		return _package;
	}

	public void set_package(String _package) {
		this._package = _package;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public String[] getTemplates() {
		return templates;
	}

	public void setTemplates(String[] templates) {
		this.templates = templates;
	}

	public double getTimeout() {
		return timeout;
	}

	public void setTimeout(double timeout) {
		this.timeout = timeout;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getVars() {
		return vars;
	}

	public void setVars(Object vars) {
		this.vars = vars;
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
	
	

}

package eu.h2020.symbiote.icinga2.utils;

public class Icinga2Utils {
	
	/**
	 *  Return the value for the load analyzing the output of a icinga service that is a symbiote device
	 *  
	 * @param deviceScriptOutput example of value for this param: symbiote result#AVAILABILITY=0#LOAD=-1
	 * @return
	 */
	public static int getLoad (String deviceScriptOutput){
		String [] aux = deviceScriptOutput.split("#");
		String [] load = aux[2].split("=");
		return Integer.parseInt(load[1]);
	}
	
	/**
	 *  Return the value for the availability analyzing the output of a icinga service that is a symbiote device
	 *  
	 * @param deviceScriptOutput example of value for this param: symbiote result#AVAILABILITY=0#LOAD=-1
	 * @return
	 */
	public static int getAvailability (String deviceScriptOutput){
		String [] aux = deviceScriptOutput.split("#");
		String [] load = aux[1].split("=");
		return Integer.parseInt(load[1]);
	}
	

}

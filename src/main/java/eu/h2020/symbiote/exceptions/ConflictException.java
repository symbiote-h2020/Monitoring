package eu.h2020.symbiote.exceptions;

/**
 * Exception thrown when an error in the data is detected
 *
 * @author: Elena Garrido, David Rojo
 * @version: 30/01/2017
 */

public class ConflictException extends RuntimeException{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2020303865004496068L;
	String extraInfo = "";
    public ConflictException(String info){
        super(info);
        this.extraInfo = info;
    }

    public ConflictException(String info, String extraInfo){
        super(info);
        this.extraInfo = extraInfo;
    }
    public String getExtraInfo() {
        return extraInfo;
    }
}

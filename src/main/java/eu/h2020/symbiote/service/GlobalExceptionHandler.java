package eu.h2020.symbiote.service;

import eu.h2020.symbiote.exceptions.ConflictException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used to control the output of the exceptions
 *
 * @author: Elena Garrido, David Rojo
 * @version: 30/01/2017
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static Log logger = LogFactory.getLog(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public @ResponseBody
     ErrorMessage handleConflict(ConflictException e) {
        logger.error("Catched exception:", e);
         ErrorMessage errorMessage = new ErrorMessage();
         errorMessage.setErrorCode(HttpStatus.CONFLICT.name());
         errorMessage.setErrorInfo("Conflict with data: "+e.getExtraInfo());
         return errorMessage;
     }



    @XmlRootElement
    class ErrorMessage {
        String errorCode = "";
        String errorInfo = "";
        @XmlElement
        public String getErrorCode() {    return errorCode;    }
        public void setErrorCode(String errorCode) {    this.errorCode = errorCode;   }
        @XmlElement
        public String getErrorInfo() {     return errorInfo;   }
        public void setErrorInfo(String errorInfo) {   this.errorInfo = errorInfo;  }
    }
}

package eu.h2020.symbiote.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.h2020.symbiote.Icinga2Manager;
import eu.h2020.symbiote.beans.HostBean;

/**
 * This class implements the rest interfaces. Initially created by jose
 *
 * @author: Elena Garrido, David Rojo
 * @version: 30/01/2017
 */
@RestController
public class PlatformMonitoringRestService {
  private static final Log logger = LogFactory.getLog(PlatformMonitoringRestService.class);

//  @Autowired
//  private PlatformMonitoringManager monitoringManager;
  
  @Autowired
  private Icinga2Manager icinga2Manager;

  @RequestMapping(method = RequestMethod.GET, path = "/hosts")
  public List<HostBean> getHosts() {
    List<HostBean>result = (List<HostBean>) icinga2Manager.getHosts();
    return result;
  }
  
  @RequestMapping(method = RequestMethod.GET, path = "/host/{hostname:.+}")
  @ResponseBody
  public HostBean getHost(@PathVariable("hostname") String hostname) {
    logger.info("START OF getHost");
    HostBean result = icinga2Manager.getHost(hostname);
    logger.info("END OF getHosts, result "+ result);
    return result;
  }
  
  @RequestMapping(method = RequestMethod.GET, path = "/host/{hostname:.+}/services")
  @ResponseBody
  public HostBean getServicesFromHost(@PathVariable("hostname") String hostname) {
    logger.info("START OF getHost");
    HostBean result = icinga2Manager.getServicesFromHost(hostname);
    logger.info("END OF getHosts, result "+ result);
    return result;
  }



}

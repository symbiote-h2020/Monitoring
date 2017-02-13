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
import eu.h2020.symbiote.beans.ServiceBean;

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
	  HostBean result = icinga2Manager.getHost(hostname);
	  return result;
  }

  @RequestMapping(method = RequestMethod.GET, path = "/host/{hostname:.+}/services")
  @ResponseBody
  public List<ServiceBean> getServicesFromHost(@PathVariable("hostname") String hostname) {
	  List<ServiceBean> result = (List<ServiceBean>) icinga2Manager.getServicesFromHost(hostname);
	  return result;
  }
  
  @RequestMapping(method = RequestMethod.GET, path = "/host/{hostname:.+}/service/{service:.+}")
  @ResponseBody
  public ServiceBean getServiceFromHost(@PathVariable("hostname") String hostname, @PathVariable("service") String service) {
	  ServiceBean result = icinga2Manager.getServiceFromHost(hostname, service);
	  return result;
  }



}

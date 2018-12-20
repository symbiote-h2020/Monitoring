/*
 * Copyright 2018 Atos
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.h2020.symbiote.monitoring.beans;

import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

public class FederationInfo {
  
  @Id
  private String federationId;
  
  private Map<String, FederatedDeviceInfo> resources = new HashMap<>();
  
  public String getFederationId() {
    return federationId;
  }
  
  public void setFederationId(String federationId) {
    this.federationId = federationId;
  }
  
  public Map<String, FederatedDeviceInfo> getResources() {
    return resources;
  }
  
  public void setResources(Map<String, FederatedDeviceInfo> resources) {
    this.resources = resources;
  }
}

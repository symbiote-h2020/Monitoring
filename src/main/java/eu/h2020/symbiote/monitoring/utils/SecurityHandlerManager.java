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

package eu.h2020.symbiote.monitoring.utils;


import eu.h2020.symbiote.client.SymbioteComponentClientFactory;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SecurityHandlerManager {

    @Value("${platform.id}")
    private String platformId;

    @Value("${symbIoTe.core.interface.url}")
    private String crmUrl;

    @Value("${symbIoTe.aam.integration:true}")
    private boolean useSecurity;

    @Value("${symbIoTe.core.interface.url:}")
    private String coreAAMAddress;

    @Value("${symbIoTe.component.keystore.password:}")
    private String keystorePassword;

    @Value("${symbIoTe.component.keystore.path:}")
    private String keystorePath;

    @Value("${symbIoTe.component.clientId:}")
    private String clientId;

    @Value("${symbIoTe.localaam.url:}")
    private String localAAMAddress;

    @Value("${symbIoTe.component.username:}")
    private String username;

    @Value("${symbIoTe.component.password:}")
    private String password;

    private IComponentSecurityHandler secHandler;

    @PostConstruct
    private void init() throws SecurityHandlerException {
        SymbioteComponentClientFactory.SecurityConfiguration securityConfiguration = (useSecurity)?new SymbioteComponentClientFactory.SecurityConfiguration(keystorePath, keystorePassword, clientId, platformId,
                null, coreAAMAddress, username, password): null;
        secHandler = SymbioteComponentClientFactory.createSecurityHandler(securityConfiguration);
    }

    public IComponentSecurityHandler getSecurityHandler() {
        return secHandler;
    }

}

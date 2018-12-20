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

package eu.h2020.symbiote.monitoring.tests.mocks;

import eu.h2020.symbiote.client.ClientConstants;
import eu.h2020.symbiote.core.cci.accessNotificationMessages.MessageInfo;
import eu.h2020.symbiote.core.cci.accessNotificationMessages.NotificationMessage;
import eu.h2020.symbiote.monitoring.db.CloudResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/coreInterface/v1")
public class DummyCRAMService {

    @Autowired
    CloudResourceRepository coreRepository;

    @RequestMapping(method = RequestMethod.POST, path = ClientConstants.PUBLISH_ACCESS_DATA,
            consumes = "application/json", produces = "application/json")
    private void publishAccessData(NotificationMessage message){

        List<String> coreResources = coreRepository.findAll().stream().map(cloudResource ->
                cloudResource.getResource().getId()).collect(Collectors.toList());

        assertValid(coreResources, message.getSuccessfulAttempts());
        assertValid(coreResources, message.getSuccessfulPushes());
        assertValid(coreResources, message.getFailedAttempts());
    }

    private <T extends MessageInfo> void assertValid(List<String> coreResources, List<T> attempts) {
        for (T attempt : attempts) {
            assert coreResources.contains(attempt.getSymbIoTeId());
        }
    }

}

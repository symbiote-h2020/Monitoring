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

package eu.h2020.symbiote.monitoring.db;

import eu.h2020.symbiote.monitoring.beans.CloudMonitoringResource;

import org.springframework.data.mongodb.repository.MongoRepository;





/**
 * Created by jose on 27/09/16.
 */
/**! \class ResourceMetricsRepository
 * \brief ResourceMetricsRepository interface to connect with the mongodb database where the registered resources will be stored
 * within the platform
 **/
public interface ResourceMetricsRepository extends MongoRepository<CloudMonitoringResource, String> {


}

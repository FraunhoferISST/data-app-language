/**
 * Copyright 2020-2022 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhg.isst.oe270.degree.remote.processing.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GitConfiguration {

    @Value("\${git.repository}")
    var repositoryAddress = ""

    @Value("\${git.identity.name:remoteProcessingEngine}")
    var identityName = "remoteProcessingEngine"

    @Value("\${git.identity.email:-none-}")
    var identityEmail = "-none-"

    @Value("\${git.authorization.credentials:}")
    var useCredentials = true

    @Value("\${git.authorization.username:}")
    var username = ""

    @Value("\${git.authorization.password:}")
    var password = ""

}
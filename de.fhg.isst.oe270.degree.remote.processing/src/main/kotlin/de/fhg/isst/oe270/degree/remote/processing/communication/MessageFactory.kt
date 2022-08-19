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
package de.fhg.isst.oe270.degree.remote.processing.communication

import de.fhg.isst.oe270.degree.remote.processing.communication.requests.*
import de.fhg.isst.oe270.degree.remote.processing.communication.response.*

object MessageFactory {

    fun createMessage(clazz: Class<Message>): Message? {
        return when (clazz) {
            RegisterGitRequest::class.java -> RegisterGitRequest()
            RegisterResponse::class.java -> RegisterResponse()
            UpdateRequest::class.java -> UpdateRequest()
            UpdateResponse::class.java -> UpdateResponse()
            DeployRequest::class.java -> DeployRequest()
            DeployResponse::class.java -> DeployResponse()
            CompileRequest::class.java -> CompileRequest()
            CompileResponse::class.java -> CompileResponse()
            StatusRequest::class.java -> StatusRequest()
            StatusResponse::class.java -> StatusResponse()
            DeleteRequest::class.java -> DeleteRequest()
            DeleteResponse::class.java -> DeleteResponse()
            StartRequest::class.java -> StartRequest()
            StartResponse::class.java -> StartResponse()
            DispatchRequest::class.java -> DispatchRequest()
            DispatchResponse::class.java -> DispatchResponse()
            StopRequest::class.java -> StopRequest()
            StopResponse::class.java -> StopResponse()
            else -> null
        }
    }

}
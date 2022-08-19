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
package de.fhg.isst.oe270.degree.remote.processing.communication.requests

import de.fhg.isst.oe270.degree.remote.processing.communication.IUsernamePasswordMessage
import de.fhg.isst.oe270.degree.remote.processing.communication.Message
import de.fhg.isst.oe270.degree.remote.processing.communication.IUuidMessage
import java.util.*

class UpdateRequest : Message(), IUuidMessage, IUsernamePasswordMessage {

    override val blurredFields: List<String>
        get() = listOf(usernameKey, passwordKey)

    override val checkedFields: List<String>
        get() = listOf(timestampKey, uuidKey, usernameKey, passwordKey)

    override fun validateFieldValues() {
        val errors = mutableSetOf<String>()

        try {
            super<Message>.validateFieldValues()
        } catch (e: Exception) {
            errors.add(e.message!!)
        }

        try {
            super<IUuidMessage>.validateFieldValues()
        } catch (e: Exception) {
            errors.add(e.message!!)
        }

        try {
            super<IUsernamePasswordMessage>.validateFieldValues()
        } catch (e: Exception) {
            errors.add(e.message!!)
        }

        if (errors.isNotEmpty()) {
            throw RuntimeException(errors.joinToString(separator = "\n"))
        }
    }

}
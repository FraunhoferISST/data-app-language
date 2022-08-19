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

import com.beust.klaxon.Klaxon

interface IPayloadMessage: IMessage {

    val payloadKey: String
        get() = "payload"

    fun getPayload(): String {
        return values[payloadKey] ?: ""
    }

    fun setPayload(value: String) {
        values[payloadKey] = value
    }

    override fun validateFieldValues() {
        val errors = mutableListOf<String>()
        try {
            super.validateFieldValues()
        } catch (e: Exception) {
            errors.add(e.message!!)
        }

        try {
            // at this point only check if the message is valid JSON
            Klaxon().parse<Map<String,String>>((values[payloadKey] ?: "").reader())
        } catch (e: Exception) {
            errors.add("Payload is no well-formed JSON.")
        }

        if (errors.isNotEmpty()) {
            throw RuntimeException(errors.joinToString(separator = "\n"))
        }
    }

}
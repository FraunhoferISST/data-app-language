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
import java.lang.Exception
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface IMessage {

    val blurredFieldValue: String
        get() = "***"

    val timestampKey: String
        get() = "timestamp"

    val checkedFields: List<String>

    val blurredFields: List<String>

    val values: HashMap<String, String>

    fun updateTimestamp() {
        values[timestampKey] = DateTimeFormatter
                .ISO_INSTANT
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())
    }

    fun getTimestamp(): String {
        return values[timestampKey] ?: ""
    }

    fun toJson(): String {
        validateFieldExistence()
        validateFieldValues()

        // blur fields
        val blurredValues = HashMap<String, String> ()
        blurredFields.forEach {key ->
            blurredValues[key] = values[key]!!
            values[key] = blurredFieldValue
        }

        // create json
        val result = Klaxon().toJsonString(values)

        // deblur fields
        blurredFields.forEach {key ->
            values[key] = blurredValues[key]!!
        }

        return result
    }

    fun fromJson(json: String) {
        values.clear()
        values.putAll(Klaxon().parse<Map<String, String>>(json)!!)
        validateFieldExistence()
        validateFieldValues()
    }

    private fun validateFieldExistence() {
        val message = "Missing value(s): "
        val fields = mutableListOf<String>()
        checkedFields.forEach { entry ->
            if (!values.contains(entry))
                fields.add(entry)
        }
        if (fields.isNotEmpty()) {
            throw RuntimeException(message + fields.joinToString(separator = ", "))
        }
    }

    fun validateFieldValues() {
        val errors = mutableSetOf<String>()
        try {
            DateTimeFormatter.ISO_INSTANT.parse(getTimestamp())
        } catch (e: Exception) {
            errors.add("Timestamp is not well-formed.")
        }
        if (errors.isNotEmpty()) {
            throw RuntimeException(errors.joinToString(separator = "\n"))
        }
    }

}
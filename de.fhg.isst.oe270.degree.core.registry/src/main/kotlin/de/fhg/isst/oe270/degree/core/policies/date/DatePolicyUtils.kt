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
package de.fhg.isst.oe270.degree.core.policies.date

import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.util.TypeConverter
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DatePolicyUtils {

    private val logger = LoggerFactory.getLogger("DatePolicyUtils")!!

    /**
     * Helper function to check a PolicyInputScope for the existence of a specific 'time.LocalDateTime' instance and
     * converting it to a java LocalDateTime.
     *
     * @param policyInput input scope which contains an 'time.DateTime' instance
     * @return LocalDateTime object with the same values as the instance inside the policyInput
     */
    fun convertLocalDateTimeToJava(policyInput : PolicyInputScope, key: String): LocalDateTime {
        return try {
            TypeConverter.nukleusToJavaDateTime(policyInput.get(key)!!)
        } catch (e : Exception) {
            logger.error("Missing input of type 'LocalDateTime' with identifier '$key'.")
            throw IllegalArgumentException("Missing input of type 'LocalDateTime' with identifier '$key'.")
        }
    }

    /**
     * Formatter which is used to format dates which need to be printed (e.g. in error messages).
     */
    val dateFormatter = DateTimeFormatter.ISO_DATE_TIME!!

}
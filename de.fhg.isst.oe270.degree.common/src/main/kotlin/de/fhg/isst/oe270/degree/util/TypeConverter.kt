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
package de.fhg.isst.oe270.degree.util

import nukleus.core.CompositeInstance
import nukleus.core.Identifier
import nukleus.core.Instance
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * The TypeConverter provides various helper function which can be used for transformations between
 * Java types and Nukleus types.
 */
object TypeConverter {

    /**
     * The used logger.
     */
    private val logger = LoggerFactory.getLogger("TypeConverter")!!

    /**
     * Transform a Nukleus instance of 'time.DateTime' to a java object of type 'LocalDateTime'
     * which represents the same point in time.
     *
     * @param instance Nukleus instance representing a 'time.DateTime'
     * @return LocalDateTime representation of the given instance
     * @throws IllegalArgumentException In case the given instance does not have the expected type
     */
    fun nukleusToJavaDateTime(instance: Instance): LocalDateTime {
        if (instance.type.identifier.toString() != "core.LocalDateTime") {
            logger.error("Input type does not match. Expected 'LocalDateTime' but found ${instance.type.identifier}")
            throw IllegalArgumentException("Expected 'LocalDateTime' but found ${instance.type.identifier}")
        }
        return LocalDateTime.of(
                Integer.valueOf((instance as CompositeInstance).read(Identifier.of("year"))),
                Integer.valueOf(instance.read(Identifier.of("month"))),
                Integer.valueOf(instance.read(Identifier.of("day"))),
                Integer.valueOf(instance.read(Identifier.of("hour"))),
                Integer.valueOf(instance.read(Identifier.of("minute"))),
                Integer.valueOf(instance.read(Identifier.of("second"))))

    }

}

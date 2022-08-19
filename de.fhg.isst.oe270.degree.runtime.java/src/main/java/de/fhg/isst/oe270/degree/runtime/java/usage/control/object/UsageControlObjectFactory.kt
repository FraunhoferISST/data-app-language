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
package de.fhg.isst.oe270.degree.runtime.java.usage.control.`object`

import org.slf4j.LoggerFactory

/**
 * The UsageControlFactory can be fed with configuration items which are used to determine which kind of usage
 * control object is used within an app.
 *
 * The configured item can be build and set by the factory.
 */
object UsageControlObjectFactory {

    /**
     * Logger used in the usage control object.
     */
    private val logger = LoggerFactory.getLogger("UsageControlObjectFactory")!!

    /**
     * Type of usage control objects that will be built with this factory.
     */
    private var ucObjectType = UsageControlObjectType.DEGREE

    /**
     * Set the type of the usage control object to built.
     *
     * @param objectType the usage control object type.
     */
    fun type(objectType: UsageControlObjectType): UsageControlObjectFactory {
        ucObjectType = objectType
        return this
    }

    /**
     * Build the usage control object with given attributes.
     */
    fun build() {
        if (UsageControlObject.isSet()) {
            logger.warn("Trying to build and overwrite UsageControlObject.")
        }

        when (ucObjectType) {
            UsageControlObjectType.DEGREE -> {
                val ucObject = DegreeUsageControlObject()
                UsageControlObject.setUCObject(ucObject)
            }
            UsageControlObjectType.IDS -> {
                throw UnsupportedOperationException("...implement IDS UC Object")
            }
        }
    }

}

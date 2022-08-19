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

import nukleus.core.Instance
import org.slf4j.LoggerFactory
import java.time.LocalDateTime


/**
 * The usage control object is the source for reliable information which can be used within applications
 * and are mainly required for usage control.
 *
 * This abstract super class defines the API of the usage control object for D° and provides some commonly
 * used implementations.
 */
abstract class UsageControlObject {

    companion object {

        /**
         * Logger used in the usage control object.
         */
        protected val logger = LoggerFactory.getLogger("UsageControlObject")!!

        /**
         * The UsageControlObject which is used in the application.
         */
        private var UCObject: UsageControlObject? = null

        /**
         * Allows to set the usage control object which will be used a single time.
         *
         * @param The usage control object, used for this execution
         */
        fun setUCObject(ucObject: UsageControlObject) {
            if (UCObject == null) {
                UCObject = ucObject
                logger.info("Usage control object successfully set.")
            } else
                logger.error("Tried to overwrite UsageControlObject")
        }

        /**
         * Retrieve the usage control object which is used in this application if it is set.
         *
         * @return The used usage control object
         */
        fun getUCObject(): UsageControlObject {
            if (UCObject == null) {
                logger.error("Tried to retrieve unset UsageControlObject")
                throw NullPointerException("Tried to retrieve unset UsageControlObject")
            }

            return UCObject!!
        }

        fun getInstance(): UsageControlObject = getUCObject()

        /**
         * Check if the usage control object is set.
         *
         * @return true if the UsageControlObject is set, false otherwise
         */
        fun isSet(): Boolean {
            return UCObject != null
        }

    }

    /**
     * Map of Identity#linKValue() to lists of policy instances in order to determine which policies apply to which
     * data.
     */
    val dataPolicies = mutableMapOf<String, List<Instance>>()

    /**
     * D° applications may receive data from external systems which use different kind of IDs.
     * In order to keep track of these different IDs, this is a map of externalID --> NukleusIDs.
     */
    val externalIdentifierMapping = mutableMapOf<String, MutableList<String>>()

    /**
     * Retrieve the type of this usage control object.
     *
     * @return The enum-value which identifies this usage control object type
     */
    abstract fun retrieveUCObjectType(): UsageControlObjectType

    /**
     * Retrieve the usage control object type as string.
     *
     * @return String representation of the usage control object type of this UCObject
     * @see retrieveUCObjectType
     */
    fun retrieveUCObjectTypeAsString(): String {
        return retrieveUCObjectType().name
    }

    fun addExternalIdentifierMappingEntry(key: String, value: String) {
        val list = if (externalIdentifierMapping.containsKey(key))
            externalIdentifierMapping[key]
        else
            mutableListOf()

        list!!.add(value)

        externalIdentifierMapping[key] = list
    }

    /**
     * Retrieve the current date (time zone naive) from the usage control object.
     *
     * @return a date object representing the current time
     */
    abstract fun retrieveCurrentTime(): LocalDateTime

}

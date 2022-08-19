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
package de.fhg.isst.oe270.degree.policies.execution

import nukleus.core.Instance
import java.util.*

class PolicyInputScope(val values: HashMap<String, Instance> = HashMap()) {

    /**
     * Tries to insert a (String, Instance) pair into this scope.
     * Will not override existing values, instead the operation fails.
     *
     * @param key identifier which will be used for insertion and later access
     * @param value the instance which should be inserted
     * @return true if the key is free and the insertion was successful, false otherwise
     */
    fun add(key : String, value : Instance) : Boolean {
        if (values.containsKey(key)) {
            return false
        }
        if (values.put(key, value) == null) {
            return false
        }
        return true
    }

    /**
     * Gets the instance for a given key if it is stored.
     *
     * @param key the identifier used for lookup
     * @return the searched instance if available, null otherwise
     */
    fun get(key : String) : Instance? {
        if (!values.containsKey(key)) {
            return null
        }
        if (values[key] == null) {
            return null
        }
        return values[key]
    }

}
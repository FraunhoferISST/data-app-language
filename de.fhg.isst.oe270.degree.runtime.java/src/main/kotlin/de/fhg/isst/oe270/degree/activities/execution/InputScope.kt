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
package de.fhg.isst.oe270.degree.activities.execution

import nukleus.core.Instance
import java.util.*

/**
 * The InputScope is used as input for Data Apps and Activities.
 */
data class InputScope(override val values: HashMap<String, Instance> = HashMap()) : Scope(values) {

    /**
     * Check if the scope contains a specific item.
     *
     * @param key checked name
     * @return true if the scope contains the key, false otherwise
     */
    fun containsKey(key: String): Boolean {
        return values.containsKey(key);
    }

}

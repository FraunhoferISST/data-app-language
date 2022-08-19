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

import java.time.LocalDateTime

/**
 * The default usage control object which is used if no other usage control object is specified.
 *
 * @see UsageControlObject
 */
class DegreeUsageControlObject : UsageControlObject() {

    /**
     * @see UsageControlObject.retrieveUCObjectType
     */
    override fun retrieveUCObjectType(): UsageControlObjectType {
        return UsageControlObjectType.DEGREE
    }

    /**
     * @see UsageControlObject.retrieveCurrentTime
     */
    override fun retrieveCurrentTime(): LocalDateTime {
        return LocalDateTime.now()
    }

}

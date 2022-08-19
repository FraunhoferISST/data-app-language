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

/**
 * The functions inside this object simplify the access to the various subsystems
 * of D°.
 *
 * The provided functions aim at simplifying the management (e.g. updating) ob the
 * subsystems.
 *
 */
object SubSystemUtils {

    /**
     * Update all subsystems after ensuring the required directories exist.
     */
    fun updateSubSystems() {
        UserConfigurationUtils.initializeUserPropertiesFolder()
        UserConfigurationUtils.initializeSubsystemFolder()

        UserConfigurationUtils.updateSubSystems()
    }

}

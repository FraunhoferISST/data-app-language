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
package de.fhg.isst.oe270.degree.activities.api

import de.fhg.isst.oe270.degree.activities.execution.InputScope
import de.fhg.isst.oe270.degree.activities.execution.OutputScope
import de.fhg.isst.oe270.degree.registry.instances.api.DegreeJavaApiImplementation

/**
 * Uniform API which all Activities must provide for functionality.
 */
interface ActivityApi : DegreeJavaApiImplementation {

    /**
     * Execute the logic of this activity.
     *
     * @param input the input for this activity
     * @return an output scope which contains the results of the execution
     */
    fun run(input : InputScope) : OutputScope

}

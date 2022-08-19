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
package de.fhg.isst.oe270.degree.registry.instances.execution.container

import com.fasterxml.jackson.databind.node.ObjectNode
import de.fhg.isst.oe270.degree.registry.instances.execution.container.ExecutionContainer.Companion.EXECUTION_CONTAINER_TYPE
import de.fhg.isst.oe270.degree.registry.instances.execution.container.ExecutionContainer.Companion.EXECUTION_CONTAINER_TYPE_NOOP
import nukleus.core.Format

/**
 * This is the default execution container. It indicates that no execution of an activity is possible.
 * If a possible solution for executing an activity is found this class will be replaced by one of its siblings.
 */
class NOOPExecutionContainer : ExecutionContainer {

    /**
     * Create a serialization object.
     */
    fun save(): ObjectNode {
        val result = Format.createObjectNode();

        result.put(EXECUTION_CONTAINER_TYPE, EXECUTION_CONTAINER_TYPE_NOOP);

        return result
    }

}

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

/**
 * The execution container is linked to an Activity definition and contains
 * all information how the activity can be executed.
 *
 * @see de.fhg.isst.degree.activities.Activity
 */
interface ExecutionContainer {
    companion object {
        /**
         * Key used for serialization.
         */
        const val EXECUTION_CONTAINER_TYPE = "executionContainerType"

        /**
         * Key used for serialization.
         */
        const val EXECUTION_CONTAINER_TYPE_NOOP = "NOOPExecutionContainer"

        /**
         * Key used for serialization.
         */
        const val EXECUTION_CONTAINER_TYPE_EMBEDDED = "EmbeddedExecutionContainer"

        /**
         * Key used for serialization.
         */
        const val EXECUTION_CONTAINER_TYPE_JAVA = "JavaExecutionContainer"

        /**
         * Key used for serialization.
         */
        const val EXECUTION_CONTAINER_TYPE_JAVA_CLASSNAME = "className"
    }
}

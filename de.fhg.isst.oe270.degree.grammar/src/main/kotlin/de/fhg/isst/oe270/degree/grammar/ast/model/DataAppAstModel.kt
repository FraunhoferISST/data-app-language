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
package de.fhg.isst.oe270.degree.grammar.ast.model

import de.fhg.isst.oe270.degree.parsing.grammar.interfaces.Node
import de.fhg.isst.oe270.degree.parsing.types.Position
import de.fhg.isst.oe270.degree.parsing.types.QualifiedName

data class DataApp(val configurationItems: Map<String, String> = mapOf(),
                   val inputs: Map<String, Pair<QualifiedName, List<DefinitionFunction>>> = mapOf(),
                   val code: Block,
                   override val file: String,
                   override val position: Position,
                   override var parent: Node? = null) : Node {
    init {
        code.parent = this
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}
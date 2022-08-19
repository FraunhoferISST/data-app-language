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
import de.fhg.isst.oe270.degree.parsing.types.Point
import de.fhg.isst.oe270.degree.parsing.types.Position
import de.fhg.isst.oe270.degree.parsing.types.QualifiedName
import org.antlr.v4.runtime.Token

fun Token.startPoint() = Point(line, charPositionInLine)

fun Token.endPoint() = Point(line, charPositionInLine + text.length)

//interface Statement : Node

data class ActivityReference(val name: QualifiedName,
                             override val file: String,
                             override val position: Position,
                             override var parent: Node? = null) : Node {
    init {
        name.parent = this
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

interface DefinitionFunctionArgument : Node

data class TypeReference(val name: QualifiedName,
                         override val file: String,
                         override val position: Position,
                         override var parent: Node? = null) : DefinitionFunctionArgument {
    init {
        name.parent = this
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class VariableReference(val name: String,
                             val index: Int = -1,
                             override val file: String,
                             override val position: Position,
                             override var parent: Node? = null) : Expression {
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

interface Expression : DefinitionFunctionArgument

data class MethodCallExpression(val methodName: String,
                                val arguments: List<Expression>,
                                override val file: String,
                                override val position: Position,
                                override var parent: Node? = null) : Expression {
    init {
        arguments.forEach { it.parent = this }
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class BooleanLiteral(val value: Boolean,
                          override val file: String,
                          override val position: Position,
                          override var parent: Node? = null) : Expression {
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class IntegerLiteral(val value: Int,
                          override val file: String,
                          override val position: Position,
                          override var parent: Node? = null) : Expression {
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class FloatingPointLiteral(val value: Double,
                                override val file: String,
                                override val position: Position,
                                override var parent: Node? = null) : Expression {
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class StringLiteral(val value: String,
                         override val file: String,
                         override val position: Position,
                         override var parent: Node? = null) : Expression {
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class FieldAccess(val reference: VariableReference,
                       val index: Int = -1,
                       val accessedFields: List<String>,
                       override val file: String,
                       override val position: Position,
                       override var parent: Node? = null) : Expression {
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class DefinitionFunction(val name: String,
                              val arguments: List<DefinitionFunctionArgument> = listOf(),
                              override val file: String,
                              override val position: Position,
                              override var parent: Node? = null) : Node {
    init {
        arguments.forEach { it.parent = this }
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}


data class ReferenceByQualifiedName(val reference: QualifiedName,
                                    override val file: String,
                                    override val position: Position,
                                    override var parent: Node? = null) : DefinitionFunctionArgument {
    init {
        reference.parent = this
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}
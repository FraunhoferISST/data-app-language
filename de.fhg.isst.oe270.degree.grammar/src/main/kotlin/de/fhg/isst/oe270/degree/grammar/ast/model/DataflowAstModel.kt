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

interface Statement : Node

data class Block(val statements: Collection<Statement>,
                 override val file: String,
                 override val position: Position,
                 override var parent: Node? = null) : Statement {
    init {
        statements.forEach { it.parent = this }
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class ActivityCall(val activity: ActivityReference,
                        val inputVariables: Collection<Expression> = listOf(),
                        val outputVariables: Collection<VariableReference> = listOf(),
                        override val file: String,
                        override val position: Position,
                        override var parent: Node? = null) : Statement {
    init {
        activity.parent = this
        inputVariables.forEach { it.parent = this }
        outputVariables.forEach { it.parent = this}
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class IfStatement(val conditions: Collection<BoolExpression>,
                       val blocks: Collection<Block>,
                       val elseBlock : Block?,
                       override val file: String,
                       override val position: Position,
                       override var parent: Node? = null) : Statement {
    init {
        conditions.forEach { it.parent = this }
        blocks.forEach { it.parent = this }
        elseBlock?.parent = this
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

enum class BooleanComperator{
    LT, GT, LEQ, GEQ, EQ, NEQ;

    companion object {
        fun retrieveByOperator(op: String) : BooleanComperator {
            return when (op) {
                "<" -> LT
                ">" -> GT
                "<=" -> LEQ
                ">=" -> GEQ
                "==" -> EQ
                "!=" -> NEQ
                else -> throw IllegalArgumentException("Unknown boolean comperator '$op'.")
            }
        }
    }
}

enum class BooleanOperator{
    AND, OR;

    companion object {
        fun retrieveByOperator(op: String) : BooleanOperator {
            return when (op) {
                "&&" -> AND
                "||" -> OR
                else -> throw IllegalArgumentException("Unknown boolean opperator '$op'.")
            }
        }
    }
}

data class BoolExpression(val expression: BoolExpression? = null,
                          val left_expression: BoolExpression? = null,
                          val right_expression: BoolExpression? = null,
                          val comperator: BooleanComperator? = null,
                          val operator: BooleanOperator? = null,
                          val negated: Boolean = false,
                          val intLiteral: Int? = null,
                          val floatLiteral: Float? = null,
                          val stringLiteral: String? = null,
                          val boolLiteral: Boolean? = null,
                          val varReference: VariableReference? = null,
                          val fieldAccess: FieldAccess? = null,
                          val method: String? = null,
                          override val file: String,
                          override val position: Position,
                          override var parent: Node? = null) : Statement {
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

sealed class VariableAssignment(open val name: String,
                                override val file: String,
                                override val position: Position,
                                override var parent: Node? = null) : Statement

data class VariableAssignmentTypeInstantiation(override val name: String,
                                               val value: TypeInstantiation,
                                               override val file: String,
                                               override val position: Position,
                                               override var parent: Node? = null) : VariableAssignment(name, file, position, parent) {
    init {
        value.parent = this
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class VariableAssignmentArray(override val name: String,
                                   val values: List<Expression>,
                                   override val file: String,
                                   override val position: Position,
                                   override var parent: Node? = null) : VariableAssignment(name, file, position, parent) {
    init {
        values.forEach { it.parent = this }
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

enum class AssignmentOperator {
    ASSIGN,
    ADD
}

data class VariableAttributeAssignment(override val name: String,
                                       val index: Int,
                                       val attributes: List<Pair<String, Int>>,
                                       val operator: AssignmentOperator,
                                       val value: Expression,
                                       override val file: String,
                                       override val position: Position,
                                       override var parent: Node? = null) : VariableAssignment(name, file, position, parent) {
    init {
        value.parent = this
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class TypeInstantiation(val type: TypeReference,
                             val functions: Collection<DefinitionFunction>, //TODO does order matter?
                             override val file: String,
                             override val position: Position,
                             override var parent: Node? = null) : Node {
    init {
        type.parent = this
        functions.forEach { it.parent = this }
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}

data class ReturnStatement(val values: List<Expression>,
                           override val file: String,
                           override val position: Position,
                           override var parent: Node? = null) : Statement {
    init {
        values.forEach { it.parent = this }
    }
    override fun toString() = reflectiveToString()
    override fun hashCode() = reflectiveHashCode()
    override fun equals(other: Any?) = reflectiveEquals(other)
}
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
package de.fhg.isst.oe270.degree.grammar.ast

import de.fhg.isst.oe270.degree.grammar.DegreeParser
import de.fhg.isst.oe270.degree.grammar.ast.model.*
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.parsing.types.Point
import de.fhg.isst.oe270.degree.parsing.types.Position
import de.fhg.isst.oe270.degree.parsing.types.QualifiedName
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import java.util.*

/**
 * This is necessary since embedded activities are no longer defined in D° Syntax and do not provide start
 * and stop positions for this reason.
 *
 * Find a better solution, and maybe allow position/file data from yaml file.
 */
fun ParserRuleContext.toPosition() =
        Position(
            if (start != null)
                start.startPoint()
            else
                Point(-1,-1),
            if (stop != null)
                stop.endPoint()
            else
                Point(-1,-1)
        )
fun ParserRuleContext.getTokenSource(): String = start.tokenSource.sourceName

//<editor-fold desc="Helper">
fun Token.tokenText() = getTokenText(this)!!

fun getTokenText(token: Token?) = when (token?.type) {
    DegreeParser.STRING_LITERAL -> token.text
            .removeSurrounding("\"", "\"")
            .replace("\\b", "\b")
            .replace("\\t", "\t")
            .replace("\\n", "\n")
            .replace("\\f", "\u000C")
            .replace("\\r", "\r")
            .replace("\\\"", "\"")
            .replace("\\'", "\'")
            .replace("\\\\", "\\")
    else -> token?.text
}
//</editor-fold>


//<editor-fold desc="Common">
fun DegreeParser.Qualified_nameContext.toAst(): QualifiedName {
    //check if qualifier is set in code
    var qualifier = qualifier?.let { getTokenText(it) }

    return QualifiedName(name.tokenText(),
            if (qualifier !== null) qualifier else Configuration.CORE_NAME_SPACE, //if still no qualifier found use default one
            getTokenSource(),
            toPosition())
}

fun DegreeParser.Activity_referenceContext.toAst() =
        ActivityReference(reference.toAst(), getTokenSource(), toPosition())

fun DegreeParser.Activity_nameContext.toAst() = name.toAst()


fun DegreeParser.Type_referenceContext.toAst() =
        TypeReference(reference.toAst(), getTokenSource(), toPosition())

fun DegreeParser.Type_nameContext.toAst() = name.toAst()

fun DegreeParser.Variable_referenceContext.toAst() =
        VariableReference(reference.toAst(), if (index == null) -1 else index.text.toInt(), getTokenSource(), toPosition())

fun DegreeParser.Variable_nameContext.toAst() = name.tokenText()

fun DegreeParser.ExpressionContext.toAst(): Expression = when (this) {
    is DegreeParser.Expression_method_callContext -> this.toAst()
    is DegreeParser.Expression_field_accessContext -> this.toAst()
    is DegreeParser.Expression_variable_referenceContext -> this.toAst()
    is DegreeParser.Expression_string_literalContext -> this.toAst()
    else -> throw UnsupportedOperationException("Unsupported class: ${this.javaClass.canonicalName}")
}

fun DegreeParser.Expression_method_callContext.toAst() =
        MethodCallExpression(method_name.tokenText(),
                expressions.map { it.toAst() },
                getTokenSource(),
                toPosition())

fun DegreeParser.Expression_field_accessContext.toAst() =
        FieldAccess(reference.toAst(), if (index == null) -1 else index.text.toInt(), accessedFields.map { it.tokenText() }, getTokenSource(), toPosition())

fun DegreeParser.Expression_variable_referenceContext.toAst() =
        reference.toAst()

fun DegreeParser.Expression_string_literalContext.toAst() =
        StringLiteral(expression_value.tokenText(), getTokenSource(), toPosition())

fun DegreeParser.Definition_functionContext.toAst() =
        DefinitionFunction(name.tokenText(),
                arguments.map { it.toAst() },
                getTokenSource(),
                toPosition())

fun DegreeParser.Definition_function_argumentContext.toAst() = when (this) {
    is DegreeParser.Definition_function_argument_expressionContext -> this.toAst()
    is DegreeParser.Definition_function_argument_reference_by_qualified_nameContext -> this.toAst()
    else -> throw UnsupportedOperationException("Unsupported class: ${this.javaClass.canonicalName}")
}

fun DegreeParser.Definition_function_argument_expressionContext.toAst() =
        expression().toAst()

fun DegreeParser.Definition_function_argument_reference_by_qualified_nameContext.toAst() =
        ReferenceByQualifiedName(reference.toAst(), getTokenSource(), toPosition())
//</editor-fold>

//<editor-fold desc="Dataflow">
fun DegreeParser.BlockContext.toAst(): Block =
        Block(statements.map { it.toAst() }, getTokenSource(), toPosition())

fun DegreeParser.StatementContext.toAst() = when (this) {
    is DegreeParser.Statement_activity_callContext -> activity_call().toAst()
    is DegreeParser.Statement_if_statementContext -> if_statement().toAst()
    is DegreeParser.Statement_variable_assignmentContext -> variable_assignment().toAst()
    is DegreeParser.Statement_blockContext -> block().toAst()
    is DegreeParser.Statement_returnContext -> return_statement().toAst()
    else -> throw UnsupportedOperationException("Unsupported class: ${this.javaClass.canonicalName}")
}

fun DegreeParser.Activity_callContext.toAst(): ActivityCall =
        ActivityCall(activity.toAst(),
                inputs.map { it.toAst() },
                outputs.map { it.toAst() },
                getTokenSource(),
                toPosition())

fun DegreeParser.If_statementContext.toAst() =
        IfStatement(conditions.map { it.toAst() }, blocks.map { it.toAst() }, else_block?.toAst(), getTokenSource(), toPosition())

fun DegreeParser.Bool_expressionContext.toAst() : BoolExpression = BoolExpression(
        expr?.toAst(),
        left_expression?.toAst(),
        right_expression?.toAst(),
        if (comperator == null) null else BooleanComperator.retrieveByOperator(comperator.operator.text),
        if (operator == null) null else BooleanOperator.retrieveByOperator(operator.operator.text),
        if (negated == null) false else negated.tokenText() == "!",
        integer_value?.text?.toInt(),
        float_value?.text?.toFloat(),
        string_value?.text,
        bool_value?.text?.toBoolean(),
        reference?.toAst(),
        if (field_reference != null)
            FieldAccess(field_reference.toAst(), if (field_reference.index == null) -1 else field_reference.index.text.toInt(), if (accessedFields != null) accessedFields.map { it.tokenText() } else emptyList(), getTokenSource(), toPosition())
        else
            null,
        method?.text,
        getTokenSource(),
        toPosition()
        )

fun DegreeParser.Variable_assignmentContext.toAst() = when (this) {
    is DegreeParser.Variable_assignment_type_instantiationContext -> this.toAst()
    is DegreeParser.Variable_assignment_arrayContext -> this.toAst()
    is DegreeParser.Variable_attribute_assignmentContext -> this.toAst()
    else -> throw UnsupportedOperationException("Unsupported class: ${this.javaClass.canonicalName}")
}

fun DegreeParser.Variable_assignment_type_instantiationContext.toAst() =
        VariableAssignmentTypeInstantiation(name.toAst(), type.toAst(), getTokenSource(), toPosition())

fun DegreeParser.Variable_assignment_arrayContext.toAst() =
        VariableAssignmentArray(name.toAst(), array_values.expressions.map { it.toAst() }, getTokenSource(), toPosition())

fun DegreeParser.Variable_attribute_assignmentContext.toAst() =
        VariableAttributeAssignment(
                variable.name.toAst(),
                if (variable.index == null) -1 else variable.index.text.toInt(),
                attributes.map { Pair(it.attribute.text, if (it.index == null) -1 else it.index.text.toInt()) },
                when (operator.text) {
                    "=" -> AssignmentOperator.ASSIGN
                    "+=" -> AssignmentOperator.ADD
                    else -> throw IllegalStateException("Unknown operator '${operator.text}' used for variable assignment.")
                },
                variable_value.toAst(),
                getTokenSource(), toPosition())

fun DegreeParser.Type_instantiationContext.toAst() =
        TypeInstantiation(type.toAst(), functions.map { it.toAst() }, getTokenSource(), toPosition())

fun DegreeParser.Return_statementContext.toAst() =
        ReturnStatement(return_values.map { it.toAst() }, getTokenSource(), toPosition())
//</editor-fold>


//<editor-fold desc="DataApp">
fun DegreeParser.Data_app_fileContext.toAst(): DataApp =
        DataApp(data_app_config().toAst(),
                data_app_code().inputs.toAst(),
                data_app_code().toAst(),
                getTokenSource(),
                toPosition())

fun DegreeParser.Data_app_configContext.toAst(): Map<String, String> =
        // create a map from all configuration items
        with(keys.map { it.tokenText() }.zip(values.map { it.tokenText() }).toMap().toMutableMap()) {
            // ensure that some important elements are set
            if (!this.containsKey("namespace")) {
                this["namespace"] = "degree"
            }
            if (!this.containsKey("name")) {
                this["name"] = "DataApp_${UUID.randomUUID().toString().replace("-", "_")}"
            }
            if (!this.containsKey("version")) {
                this["version"] = "0.0.1-1-SNAPSHOT"
            }
            if (!this.containsKey("tags")) {
                this["tags"] = ""
            }
            if (!this.containsKey("execution")) {
                this["execution"] = "single"
            }
            if (!this.containsKey("periodicTime")) {
                this["periodicTime"] = "0"
            }
            if (!this.containsKey("contextType")) {
                this["usageControlObject"] = "D°"
            }
            // return the modified map as result
            this
        }

fun List<DegreeParser.Block_input_parameterContext>.toAst(): MutableMap<String, Pair<QualifiedName, List<DefinitionFunction>>> {
    val result = mutableMapOf<String, Pair<QualifiedName, List<DefinitionFunction>>>()

    this.map {
        val definitionFunctions = mutableListOf<DefinitionFunction>()
        for (ctx in it.functions)
            definitionFunctions.add(ctx.toAst())
        result[it.name.text] = Pair(it.reference.type_name().qualified_name().toAst(), definitionFunctions.toList())
    }

    return result
}

fun DegreeParser.Data_app_codeContext.toAst() =
        code.toAst()
//</editor-fold>

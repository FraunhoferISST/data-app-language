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
package de.fhg.isst.oe270.degree.grammar;

import de.fhg.isst.oe270.degree.grammar.ast.toAst
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.parsing.types.Position
import de.fhg.isst.oe270.degree.parsing.types.QualifiedName
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DegreeParserTest {

    private fun dynamicTest(code: String, parserRuleName: String, expectedParseTree: String,
                    expectErrors: Boolean = false, expectParserIsEOF: Boolean = true) : ParserRuleContext {
        val (parser, errors) = getParser(code)
        val parserRuleMethod = parser.javaClass.getMethod(parserRuleName)
        val parserRuleContext = parserRuleMethod.invoke(parser) as ParserRuleContext

        if(expectErrors) {
            assertTrue(!errors.isEmpty(), "Missing expected errors")
        } else {
            assertTrue(errors.isEmpty(), "Unexpected errors")
        }
        assertEquals(expectParserIsEOF, parser.currentTokenIsEOF(), "Expecting parser ${if (!expectParserIsEOF) "not" else ""} to be EOF")
        assertEquals(expectedParseTree, parserRuleContext.toParseTree().multiLineString())
        return parserRuleContext
    }


    //<editor-fold desc="Activities">
    @Test
    fun activities_file() {
    }

    @Test
    fun activities_namespace_block() {
    }

    @Test
    fun activity_definition() {
    }
    //</editor-fold>


    //<editor-fold desc="DataApp">
    @Test
    fun data_app_file() {
    }

    @Test
    fun data_app_config() {
    }

    @Test
    fun data_app_code() {
    }
    //</editor-fold>


    //<editor-fold desc="Policies">
    @Test
    fun policies_file() {
    }

    @Test
    fun validator_definition() {
    }

    @Test
    fun validator_instantiation() {
    }

    @Test
    fun activity_instantiation() {
    }

    @Test
    fun parameter_to_validator_mapping() {
    }
    //</editor-fold>


    //<editor-fold desc="Types">
    @Test
    fun types_file() {
    }

    @Test
    fun types_namespace_block() {
    }

    @Test
    fun type_definition() {
    }

    @Test
    fun type_definition_script_expression() {
    }

    @Test
    fun type_definition_degree_expression() {
    }
    //</editor-fold>


    //<editor-fold desc="DataFlow">
    @Test
    fun block() {
    }

    @Test
    fun statement() {
    }

    @Test
    fun activity_call() {
    }

    @Test
    fun if_statement() {
    }

    @Test
    fun variable_assignment() {
    }

    @Test
    fun type_instantiation() {
    }

    @Test
    fun array_initializer() {
    }

    @Test
    fun return_statement() {
    }
    //</editor-fold>


    //<editor-fold desc="Common">
    @Test
    fun `qualified_name without qualifier`() {
        val code = "nameWithoutQualifierButWithNumbers123"
        val expectedParseTree =
                """Qualified_name
                  |  IDENTIFIER[nameWithoutQualifierButWithNumbers123]
                  |""".trimMargin()
        val expectedAst = QualifiedName(code, Configuration.CORE_NAME_SPACE, stringSourceName,
                Position(1, 0, 1, code.length))
        val parserRuleContext = dynamicTest(code, "qualified_name", expectedParseTree) as DegreeParser.Qualified_nameContext
        val ast = parserRuleContext.toAst()
        assertEquals(ast, expectedAst)
    }

    @Test
    fun `qualified_name with default qualifier`() {
        val code = Configuration.CORE_NAME_SPACE + ".nameWithDefaultQualifierAndWithNumbers123"
        val expectedParseTree =
                """Qualified_name
                  |  IDENTIFIER[core]
                  |  DOT[.]
                  |  IDENTIFIER[nameWithDefaultQualifierAndWithNumbers123]
                  |""".trimMargin()
        val expectedAst = QualifiedName("nameWithDefaultQualifierAndWithNumbers123", Configuration.CORE_NAME_SPACE, stringSourceName,
                Position(1, 0, 1, code.length))
        val parserRuleContext = dynamicTest(code, "qualified_name", expectedParseTree) as DegreeParser.Qualified_nameContext
        val ast = parserRuleContext.toAst()
        assertEquals(ast, expectedAst)
    }

    @Test
    fun `qualified_name with qualifier`() {
        val code = "mySpecial123Qualifier.nameWithQualifierAndWithNumbers123"
        val expectedParseTree =
                """Qualified_name
                  |  IDENTIFIER[mySpecial123Qualifier]
                  |  DOT[.]
                  |  IDENTIFIER[nameWithQualifierAndWithNumbers123]
                  |""".trimMargin()
        val expectedAst = QualifiedName("nameWithQualifierAndWithNumbers123", "mySpecial123Qualifier", stringSourceName,
                Position(1, 0, 1, code.length))
        val parserRuleContext = dynamicTest(code, "qualified_name", expectedParseTree) as DegreeParser.Qualified_nameContext
        val ast = parserRuleContext.toAst()
        assertEquals(ast, expectedAst)
    }

    @Test
    fun `qualified_name with multiple qualifier - does not reach EOF`() {
        val code = "firstQualifier.secondQualifier.nameWithQualifiersAndWithNumbers123"
        val expectedParseTree =
                """Qualified_name
                  |  IDENTIFIER[firstQualifier]
                  |  DOT[.]
                  |  IDENTIFIER[secondQualifier]
                  |""".trimMargin()
        val expectedAst = QualifiedName("secondQualifier", "firstQualifier", stringSourceName,
                Position(1, 0, 1, 30))
        val parserRuleContext = dynamicTest(code, "qualified_name", expectedParseTree, expectParserIsEOF = false) as DegreeParser.Qualified_nameContext
        val ast = parserRuleContext.toAst()
        assertEquals(expectedAst, ast)
    }

    @Test
    fun array_dimension() {
    }

    @Test
    fun type_name() {
    }

    @Test
    fun variable_reference() {
    }

    @Test
    fun variable_name() {
    }


    //<editor-fold desc="Expressions">
    @Test
    fun expression() {
    }

    @Test
    fun or_expression() {
    }

    @Test
    fun and_expression() {
    }

    @Test
    fun equality_expression() {
    }

    @Test
    fun relational_expression() {
    }

    @Test
    fun additive_expression() {
    }

    @Test
    fun multiplicative_expression() {
    }

    @Test
    fun unary_expression() {
    }

    @Test
    fun value_expression() {
    }
    //</editor-fold>


    @Test
    fun definition_function() {
    }

    @Test
    fun definition_function_argument() {
    }
    //</editor-fold>
}


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
//package de.fhg.isst.oe270.degree.grammar.policies.compiler
//
//import kotlin.test.assertEquals
//import org.junit.Test as test
//
//class PoliciesParserTest {
//
//    //<editor-fold desc="parse PolicyDeclaration">
//    @test fun parsePolicyDeclaration_noNeeds_noProvides() {
//        val code = """Activity TestActivity;"""
//        val expectedParseTree =
//                """Policy_declaration
//                  |  T[Activity]
//                  |  Qualified_name
//                  |    T[TestActivity]
//                  |  T[;]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy_declaration()).multiLineString())
//    }
//
//    @test fun parsePolicyDeclaration_noNeededPolicies_noProvides() {
//        val code = """Activity TestActivity needs [];"""
//        val expectedParseTree =
//                """Policy_declaration
//                  |  T[Activity]
//                  |  Qualified_name
//                  |    T[TestActivity]
//                  |  T[needs]
//                  |  T[[]
//                  |  T[]]
//                  |  T[;]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy_declaration()).multiLineString())
//    }
//
//    @test fun parsePolicyDeclaration_oneNeededPolicy_noProvides() {
//        val code = """Activity TestActivity needs [ FirstNeededPolicy ];"""
//        val expectedParseTree =
//                """Policy_declaration
//                  |  T[Activity]
//                  |  Qualified_name
//                  |    T[TestActivity]
//                  |  T[needs]
//                  |  T[[]
//                  |  Policy_list
//                  |    Policy
//                  |      Qualified_name
//                  |        T[FirstNeededPolicy]
//                  |  T[]]
//                  |  T[;]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy_declaration()).multiLineString())
//    }
//
//    @test fun parsePolicyDeclaration_twoNeededPolicies_noProvides() {
//        val code = """Activity TestActivity needs [FirstNeededPolicy, SecondNeededPolicy ];"""
//        val expectedParseTree =
//                """Policy_declaration
//                  |  T[Activity]
//                  |  Qualified_name
//                  |    T[TestActivity]
//                  |  T[needs]
//                  |  T[[]
//                  |  Policy_list
//                  |    Policy
//                  |      Qualified_name
//                  |        T[FirstNeededPolicy]
//                  |    T[,]
//                  |    Policy
//                  |      Qualified_name
//                  |        T[SecondNeededPolicy]
//                  |  T[]]
//                  |  T[;]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy_declaration()).multiLineString())
//    }
//
//    @test fun parsePolicyDeclaration_noNeeds_noProvidedPolicies() {
//        val code = """Activity TestActivity provides [ ];"""
//        val expectedParseTree =
//                """Policy_declaration
//                  |  T[Activity]
//                  |  Qualified_name
//                  |    T[TestActivity]
//                  |  T[provides]
//                  |  T[[]
//                  |  T[]]
//                  |  T[;]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy_declaration()).multiLineString())
//    }
//
//    @test fun parsePolicyDeclaration_noNeeds_oneProvidedPolicy() {
//        val code = """Activity TestActivity provides [ FirstProvidedPolicy];"""
//        val expectedParseTree =
//                """Policy_declaration
//                  |  T[Activity]
//                  |  Qualified_name
//                  |    T[TestActivity]
//                  |  T[provides]
//                  |  T[[]
//                  |  Policy_list
//                  |    Policy
//                  |      Qualified_name
//                  |        T[FirstProvidedPolicy]
//                  |  T[]]
//                  |  T[;]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy_declaration()).multiLineString())
//    }
//
//    @test fun parsePolicyDeclaration_noNeeds_twoProvidedPolicies() {
//        val code = """Activity TestActivity provides [ FirstProvidedPolicy,SecondProvidedPolicy ];"""
//        val expectedParseTree =
//                """Policy_declaration
//                  |  T[Activity]
//                  |  Qualified_name
//                  |    T[TestActivity]
//                  |  T[provides]
//                  |  T[[]
//                  |  Policy_list
//                  |    Policy
//                  |      Qualified_name
//                  |        T[FirstProvidedPolicy]
//                  |    T[,]
//                  |    Policy
//                  |      Qualified_name
//                  |        T[SecondProvidedPolicy]
//                  |  T[]]
//                  |  T[;]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy_declaration()).multiLineString())
//    }
//
//    @test fun parsePolicyDeclaration_twoNeededPolicies_oneProvidedPolicy() {
//        val code = """Activity TestActivity
//            |needs [FirstNeededPolicy,SecondNeededPolicy]
//            |provides [ FirstProvidedPolicy ];""".trimMargin()
//        val expectedParseTree =
//                """Policy_declaration
//                  |  T[Activity]
//                  |  Qualified_name
//                  |    T[TestActivity]
//                  |  T[needs]
//                  |  T[[]
//                  |  Policy_list
//                  |    Policy
//                  |      Qualified_name
//                  |        T[FirstNeededPolicy]
//                  |    T[,]
//                  |    Policy
//                  |      Qualified_name
//                  |        T[SecondNeededPolicy]
//                  |  T[]]
//                  |  T[provides]
//                  |  T[[]
//                  |  Policy_list
//                  |    Policy
//                  |      Qualified_name
//                  |        T[FirstProvidedPolicy]
//                  |  T[]]
//                  |  T[;]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy_declaration()).multiLineString())
//    }
//    //</editor-fold>
//
//    //<editor-fold desc="parse Policy">
//    @test fun parsePolicy_noParenthesis() {
//        val code = """MyPolicy"""
//        val expectedParseTree =
//                """Policy
//                  |  Qualified_name
//                  |    T[MyPolicy]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy()).multiLineString())
//    }
//
//    @test fun parsePolicy_noArguments() {
//        val code = """MyPolicy()"""
//        val expectedParseTree =
//                """Policy
//                  |  Qualified_name
//                  |    T[MyPolicy]
//                  |  T[(]
//                  |  T[)]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy()).multiLineString())
//    }
//
//    @test fun parsePolicy_oneArgument() {
//        val code = """MyPolicy("firstarg")"""
//        val expectedParseTree =
//                """Policy
//                  |  Qualified_name
//                  |    T[MyPolicy]
//                  |  T[(]
//                  |  Argument_list
//                  |    Argument
//                  |      T["firstarg"]
//                  |  T[)]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy()).multiLineString())
//    }
//
//    @test fun parsePolicy_twoArguments() {
//        val code = """MyPolicy("firstarg", "secondarg")"""
//        val expectedParseTree =
//                """Policy
//                  |  Qualified_name
//                  |    T[MyPolicy]
//                  |  T[(]
//                  |  Argument_list
//                  |    Argument
//                  |      T["firstarg"]
//                  |    T[,]
//                  |    Argument
//                  |      T["secondarg"]
//                  |  T[)]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).policy()).multiLineString())
//    }
//    //</editor-fold>
//
//    //<editor-fold desc="parse ArgumentList">
//    @test fun parseArgumentList_oneArgument() {
//        val code = """"asd\\f""""
//        val expectedParseTree =
//                """Argument_list
//                  |  Argument
//                  |    T["asd\\f"]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).argument_list()).multiLineString())
//    }
//
//    @test fun parseArgumentList_twoArguments() {
//        val code = """"a\"sdf", "jklo\nte\bst""""
//        val expectedParseTree =
//                """Argument_list
//                  |  Argument
//                  |    T["a\"sdf"]
//                  |  T[,]
//                  |  Argument
//                  |    T["jklo\nte\bst"]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).argument_list()).multiLineString())
//    }
//
//    @test fun parseArgumentList_threeArguments() {
//        val code = """"asdf\'","jklo\ntest", "blaaa""""
//        val expectedParseTree =
//                """Argument_list
//                  |  Argument
//                  |    T["asdf\'"]
//                  |  T[,]
//                  |  Argument
//                  |    T["jklo\ntest"]
//                  |  T[,]
//                  |  Argument
//                  |    T["blaaa"]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).argument_list()).multiLineString())
//    }
//    //</editor-fold>
//
//    //<editor-fold desc="parse Argument">
//    @test fun parseArgument_StringLiteral() {
//        val code = """"asdf test string\\ lba \n test \r aaaa""""
//        val expectedParseTree =
//                """Argument
//                  |  T["asdf test string\\ lba \n test \r aaaa"]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).argument()).multiLineString())
//    }
//    //</editor-fold>
//
//    //<editor-fold desc="parse QualifiedName">
//    @test fun parseQualifiedName_noQualifiers() {
//        val code = """MyName"""
//        val expectedParseTree =
//                """Qualified_name
//                  |  T[MyName]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).qualified_name()).multiLineString())
//    }
//
//    @test fun parseQualifiedName_oneQualifier() {
//        val code = """qualified.MyName"""
//        val expectedParseTree =
//                """Qualified_name
//                  |  T[qualified]
//                  |  T[.]
//                  |  T[MyName]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).qualified_name()).multiLineString())
//    }
//
//    @test fun parseQualifiedName_twoQualifiers() {
//        val code = """double.qualified.MyName"""
//        val expectedParseTree =
//                """Qualified_name
//                  |  T[double]
//                  |  T[.]
//                  |  T[qualified]
//                  |  T[.]
//                  |  T[MyName]
//                  |""".trimMargin()
//        assertEquals(expectedParseTree, toParseTree(parserForCode(code).qualified_name()).multiLineString())
//    }
//    //</editor-fold>
//}
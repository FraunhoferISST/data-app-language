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
//import kotlin.test.assertTrue
//import org.junit.Test as test
//
//class ASTMappingTest {
//
//    @test fun mapPolicyDeclarationList_singleActivityNoQualifiers_noNeeds_noProvides() {
//        val code = """Activity TestActivity;"""
//                .trimMargin("|")
//        val parsingResult = PoliciesParserFacade.parse(code)
//        val expectedAst = PolicyDeclarationList(listOf(
//                PolicyDeclaration(QualifiedName("TestActivity"))
//        ))
//        assertTrue { parsingResult.isCorrect() }
//        assertEquals(expectedAst, parsingResult.getAst())
//    }
//
//    @test fun mapPolicyDeclarationList_singleActivity_noNeeds_noProvides() {
//        val code = """Activity my.first.TestActivity;"""
//                .trimMargin("|")
//        val parsingResult = PoliciesParserFacade.parse(code)
//        val expectedAst = PolicyDeclarationList(listOf(
//                PolicyDeclaration(QualifiedName("TestActivity", listOf("my", "first")))
//        ))
//        assertTrue { parsingResult.isCorrect() }
//        assertEquals(expectedAst, parsingResult.getAst())
//    }
//
//    @test fun mapPolicyDeclarationList() {
//        val code = """Activity my.first.TestActivity
//                     |needs [ my.policy.ReadFile("path/to/file.txt"), my.policy.ReadDatabase("http://my.database.de", "3456"), WriteFile() ]
//                     |provides [ my.policy.Anonymized ];"""
//                .trimMargin("|")
//        val parsingResult = PoliciesParserFacade.parse(code)
//        val expectedAst = PolicyDeclarationList(listOf(
//                PolicyDeclaration(
//                        QualifiedName("TestActivity", listOf("my", "first")),
//                        listOf(Policy(QualifiedName("ReadFile", listOf("my", "policy")), listOf(Argument("path/to/file.txt"))),
//                                Policy(QualifiedName("ReadDatabase", listOf("my", "policy")),
//                                        listOf(Argument("http://my.database.de"), Argument("3456"))),
//                                Policy(QualifiedName("WriteFile"))
//                        ),
//                        listOf(Policy(QualifiedName("Anonymized", listOf("my", "policy")))))))
//        assertTrue { parsingResult.isCorrect() }
//        assertEquals(expectedAst, parsingResult.getAst())
//    }
//
////    @test fun mapSimpleFileWithPositions() {
////        val code = """var a = 1 + 2
////                     |a = 7 * (2 / 3)""".trimMargin("|")
////        val ast = PoliciesParserFacade.parse(code).root!!.toAst(considerPosition = true)
//////        val expectedAst = PolicyDeclarationList(listOf(
//////                VarDeclaration("a",
//////                        SumExpression(
//////                                IntLit("1", pos(1,8,1,9)),
//////                                IntLit("2", pos(1,12,1,13)),
//////                                pos(1,8,1,13)),
//////                        pos(1,0,1,13)),
//////                Assignment("a",
//////                        MultiplicationExpression(
//////                                IntLit("7", pos(2,4,2,5)),
//////                                DivisionExpression(
//////                                        IntLit("2", pos(2,9,2,10)),
//////                                        IntLit("3", pos(2,13,2,14)),
//////                                        pos(2,9,2,14)),
//////                                pos(2,4,2,15)),
//////                        pos(2,0,2,15))),
//////                pos(1,0,2,15))
//////        assertEquals(expectedAst, ast)
////    }
////
////    @test fun mapCastInt() {
////        val code = "a = 7 as Int"
////        val ast = PoliciesParserFacade.parse(code).root!!.toAst()
//////        val expectedAst = PolicyDeclarationList(listOf(Assignment("a", TypeConversion(IntLit("7"), IntType()))))
//////        assertEquals(expectedAst, ast)
////    }
////
////    @test fun mapCastDecimal() {
////        val code = "a = 7 as Decimal"
////        val ast = PoliciesParserFacade.parse(code).root!!.toAst()
//////        val expectedAst = PolicyDeclarationList(listOf(Assignment("a", TypeConversion(IntLit("7"), DecimalType()))))
//////        assertEquals(expectedAst, ast)
////    }
////
////    @test fun mapPrint() {
////        val code = "print(a)"
////        val ast = PoliciesParserFacade.parse(code).root!!.toAst()
//////        val expectedAst = PolicyDeclarationList(listOf(Print(VarReference("a"))))
//////        assertEquals(expectedAst, ast)
////    }
//
//}
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
//import de.fhg.isst.oe270.degree.grammar.policies.PoliciesLexer as PL
//import kotlin.test.assertEquals
//import org.junit.Test as test
//
//class PoliciesLexerTest {
//
//    //<editor-fold desc="lex PolicyDeclaration">
//    @test fun lexPolicyDeclaration_noNeeds_noProvides() {
//        val code = """Activity TestActivity;"""
//        val expectedTokens = listOfTokens(PL.KEYWORD_ACTIVITY, PL.IDENTIFIER, PL.SEMI, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicyDeclaration_noNeededPolicies_noProvides() {
//        val code = """Activity TestActivity needs [];"""
//        val expectedTokens = listOfTokens(PL.KEYWORD_ACTIVITY, PL.IDENTIFIER,
//                PL.KEYWORD_NEEDS, PL.LBRACK, PL.RBRACK,
//                PL.SEMI, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicyDeclaration_oneNeededPolicy_noProvides() {
//        val code = """Activity TestActivity needs [ FirstNeededPolicy ];"""
//        val expectedTokens = listOfTokens(PL.KEYWORD_ACTIVITY, PL.IDENTIFIER,
//                PL.KEYWORD_NEEDS, PL.LBRACK, PL.IDENTIFIER, PL.RBRACK,
//                PL.SEMI, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicyDeclaration_twoNeededPolicies_noProvides() {
//        val code = """Activity TestActivity needs [FirstNeededPolicy, SecondNeededPolicy ];"""
//        val expectedTokens = listOfTokens(PL.KEYWORD_ACTIVITY, PL.IDENTIFIER,
//                PL.KEYWORD_NEEDS, PL.LBRACK, PL.IDENTIFIER, PL.COMMA, PL.IDENTIFIER, PL.RBRACK,
//                PL.SEMI, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicyDeclaration_noNeeds_noProvidedPolicies() {
//        val code = """Activity TestActivity provides [ ];"""
//        val expectedTokens = listOfTokens(PL.KEYWORD_ACTIVITY, PL.IDENTIFIER,
//                PL.KEYWORD_PROVIDES, PL.LBRACK, PL.RBRACK,
//                PL.SEMI, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicyDeclaration_noNeeds_oneProvidedPolicy() {
//        val code = """Activity TestActivity provides [ FirstProvidedPolicy];"""
//        val expectedTokens = listOfTokens(PL.KEYWORD_ACTIVITY, PL.IDENTIFIER,
//                PL.KEYWORD_PROVIDES, PL.LBRACK, PL.IDENTIFIER, PL.RBRACK,
//                PL.SEMI, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicyDeclaration_noNeeds_twoProvidedPolicies() {
//        val code = """Activity TestActivity provides [ FirstProvidedPolicy,SecondProvidedPolicy ];"""
//        val expectedTokens = listOfTokens(PL.KEYWORD_ACTIVITY, PL.IDENTIFIER,
//                PL.KEYWORD_PROVIDES, PL.LBRACK, PL.IDENTIFIER, PL.COMMA, PL.IDENTIFIER, PL.RBRACK,
//                PL.SEMI, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicyDeclaration_twoNeededPolicies_oneProvidedPolicy() {
//        val code = """Activity TestActivity
//            |needs [FirstNeededPolicy,SecondNeededPolicy]
//            |provides [ FirstProvidedPolicy ];""".trimMargin()
//        val expectedTokens = listOfTokens(PL.KEYWORD_ACTIVITY, PL.IDENTIFIER,
//                PL.KEYWORD_NEEDS, PL.LBRACK, PL.IDENTIFIER, PL.COMMA, PL.IDENTIFIER, PL.RBRACK,
//                PL.KEYWORD_PROVIDES, PL.LBRACK, PL.IDENTIFIER, PL.RBRACK,
//                PL.SEMI, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//    //</editor-fold>
//
//    //<editor-fold desc="lex Policy">
//    @test fun lexPolicy_noParenthesis() {
//        val code = """MyPolicy"""
//        val expectedTokens = listOfTokens(PL.IDENTIFIER, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicy_noArguments() {
//        val code = """MyPolicy()"""
//        val expectedTokens = listOfTokens(PL.IDENTIFIER, PL.LPAREN, PL.RPAREN, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicy_oneArgument() {
//        val code = """MyPolicy("firstarg")"""
//        val expectedTokens = listOfTokens(PL.IDENTIFIER, PL.LPAREN, PL.STRING_LITERAL, PL.RPAREN, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexPolicy_twoArguments() {
//        val code = """MyPolicy("firstarg", "secondarg")"""
//        val expectedTokens = listOfTokens(PL.IDENTIFIER, PL.LPAREN,
//                PL.STRING_LITERAL, PL.COMMA, PL.STRING_LITERAL,
//                PL.RPAREN, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//    //</editor-fold>
//
//    //<editor-fold desc="lex ArgumentList">
//    @test fun lexArgumentList_oneArgument() {
//        val code = """"asd\\f""""
//        val expectedTokens = listOfTokens(PL.STRING_LITERAL, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexArgumentList_twoArguments() {
//        val code = """"a\"sdf", "jklo\nte\bst""""
//        val expectedTokens = listOfTokens(PL.STRING_LITERAL, PL.COMMA, PL.STRING_LITERAL, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexArgumentList_threeArguments() {
//        val code = """"asdf\'","jklo\ntest", "blaaa""""
//        val expectedTokens = listOfTokens(PL.STRING_LITERAL, PL.COMMA, PL.STRING_LITERAL, PL.COMMA, PL.STRING_LITERAL, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//    //</editor-fold>
//
//    //<editor-fold desc="lex Argument">
//    @test fun lexArgument_StringLiteral() {
//        val code = """"asdf test string\\ lba \n test \r aaaa""""
//        val expectedTokens = listOfTokens(PL.STRING_LITERAL, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//    //</editor-fold>
//
//    //<editor-fold desc="lex QualifiedName">
//    @test fun lexQualifiedName_noQualifiers() {
//        val code = """MyName"""
//        val expectedTokens = listOfTokens(PL.IDENTIFIER, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexQualifiedName_oneQualifier() {
//        val code = """qualified.MyName"""
//        val expectedTokens = listOfTokens(PL.IDENTIFIER, PL.DOT, PL.IDENTIFIER, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//
//    @test fun lexQualifiedName_twoQualifiers() {
//        val code = """double.qualified.MyName"""
//        val expectedTokens = listOfTokens(PL.IDENTIFIER, PL.DOT, PL.IDENTIFIER, PL.DOT, PL.IDENTIFIER, PL.EOF)
//        assertEquals(expectedTokens, tokens(lexerForCode(code)))
//    }
//    //</editor-fold>
//}
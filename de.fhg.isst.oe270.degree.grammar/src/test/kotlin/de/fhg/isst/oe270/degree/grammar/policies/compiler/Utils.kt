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
//import de.fhg.isst.oe270.degree.grammar.policies.PoliciesLexer
//import de.fhg.isst.oe270.degree.grammar.policies.PoliciesParser
//import org.antlr.v4.runtime.CharStreams
//import org.antlr.v4.runtime.CommonTokenStream
//import java.de.fhg.isst.oe270.degree.utils.*
//
//fun lexerForCode(code: String) = PoliciesLexer(CharStreams.fromString(code))
//
////fun lexerForResource(resourceName: String) = PoliciesLexer(CharStreams.fromStream(PoliciesLexer::class.java.classLoader.getResourceAsStream(resourceName)))
//
//fun tokens(lexer: PoliciesLexer): List<String> {
//    val tokens = LinkedList<String>()
//    do {
//        val t = lexer.nextToken()
//        when (t.type) {
//            -1 -> tokens.add("EOF")
////            else -> if (t.type != PoliciesLexer.WS) tokens.add(lexer.ruleNames[t.type - 1])
//            else -> if (t.type != PoliciesLexer.WS) tokens.add(lexer.vocabulary.getSymbolicName(t.type))
//        }
//    } while (t.type != -1)
//    return tokens
//}
//
//fun listOfTokens(vararg tokenTypes: Int) : List<String> {
//    return tokenTypes.map { PoliciesLexer.VOCABULARY.getSymbolicName(it) }
//}
//
//fun parserForCode(code: String) = PoliciesParser(CommonTokenStream(lexerForCode(code)))
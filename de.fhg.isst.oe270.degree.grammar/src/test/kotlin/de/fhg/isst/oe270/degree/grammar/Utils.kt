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
package de.fhg.isst.oe270.degree.grammar

import de.fhg.isst.oe270.degree.parsing.types.Point
import de.fhg.isst.oe270.degree.parsing.types.Position
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.util.*

fun DegreeParser.currentTokenIsEOF() = currentToken.type == DegreeParser.EOF

const val stringSourceName = "<String>"

fun getParser(code: String) : Pair<DegreeParser, List<Error>> {
    val errors = LinkedList<Error>()
    val errorListener = object : ANTLRErrorListener {
        override fun syntaxError(recognizer: Recognizer<*, *>, offendingSymbol: Any, line: Int, charPositionInLine: Int, msg: String, e: RecognitionException?) {
            val point = Point(line, charPositionInLine)
            errors.add(Error(msg, stringSourceName, Position(point, point)))
        }

        override fun reportAttemptingFullContext(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, conflictingAlts: BitSet?, configs: ATNConfigSet?) {
            //not needed
        }
        override fun reportAmbiguity(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, exact: Boolean, ambigAlts: BitSet?, configs: ATNConfigSet?) {
            //not needed
        }
        override fun reportContextSensitivity(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, prediction: Int, configs: ATNConfigSet?) {
            //not needed
        }
    }

    val lexer = DegreeLexer(CharStreams.fromString(code, stringSourceName))
    lexer.removeErrorListeners()
    lexer.addErrorListener(errorListener)
    val parser = DegreeParser(CommonTokenStream(lexer))
    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)
    return (parser to errors)
}
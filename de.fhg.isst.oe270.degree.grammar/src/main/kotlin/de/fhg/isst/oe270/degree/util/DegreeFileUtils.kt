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
package de.fhg.isst.oe270.degree.util

import de.fhg.isst.oe270.degree.grammar.DegreeLexer
import de.fhg.isst.oe270.degree.grammar.DegreeParser
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.LoggerFactory

import java.io.IOException

/**
 * Utils to interact with D° source files.
 */
object DegreeFileUtils {

    /**
     * The used logger.
     */
    private val LOGGER = LoggerFactory.getLogger(DegreeFileUtils::class.java.simpleName)

    /**
     * Get a degree parser for a given string.
     *
     * @param text the string to parse
     * @return a matching D° parser
     */
    @JvmStatic
    fun getDegreeParserFromText(text: String): DegreeParser? {
        return getDegreeParserFromString(text, false)
    }

    /**
     * Get a degree parser for a given file.
     *
     * @param filePath the file to parse
     * @return a matching D° parser
     */
    @JvmStatic
    fun getDegreeParserFromFile(filePath: String): DegreeParser? {
        return getDegreeParserFromString(filePath, true)
    }

    /**
     * Get a degree parser for a given string or file.
     *
     * @param string the string/file to parse
     * @param isFile indicator if string is a file or D° source itself
     * @return a matching D° parser
     */
    @JvmStatic
    fun getDegreeParserFromString(string: String, isFile: Boolean): DegreeParser? {
        val charStream: CharStream =
                if (isFile) {
                    try {
                        CharStreams.fromFileName(string)
                    } catch (e: IOException) {
                        LOGGER.error("Error loading file \"$string\"", e)
                        return null
                    }
                } else {
                    CharStreams.fromString(string)
                }
        val degreeLexer = DegreeLexer(charStream)
        val commonTokenStream = CommonTokenStream(degreeLexer)
        return DegreeParser(commonTokenStream)
    }
}

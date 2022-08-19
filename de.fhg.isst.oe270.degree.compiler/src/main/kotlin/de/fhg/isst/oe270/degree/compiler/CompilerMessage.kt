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
package de.fhg.isst.oe270.degree.compiler

import de.fhg.isst.oe270.degree.grammar.Error
import de.fhg.isst.oe270.degree.parsing.types.Position

/**
 * This class encapsulates a message produced by a programming language processor (whether interpreted or compiled).
 */
data class CompilerMessage(val kind: Kind,
                           val message: String,
                           val file: String? = null,
                           val position: Position? = null) {
    constructor(error: Error, kind: Kind = Kind.ERROR) : this(kind, error.message, error.file, error.position)

    //TODO wenn logging ordentlich eingebaut ist loglevel verwenden?
    enum class Kind constructor(private val type: String) {
        //Problem which prevents the tool's normal completion.
        ERROR("error"),
        //Problem which does not usually prevent the tool from completing normally.
        WARNING("warning"),
        //Informative message from the tool.
        INFO("info"),
        //Diagnostic which does not fit within the other kinds.
        DEBUG("debug")
    }
}

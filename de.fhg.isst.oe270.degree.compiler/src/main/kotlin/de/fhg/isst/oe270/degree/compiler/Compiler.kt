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

interface Compiler {
//    val ROLE = Compiler::class.java.name

    /**
     * Performs the compilation of the project. Clients must implement this method.
     *
     * @param configuration   the configuration description of the compilation to perform
     * @return the result of the compilation returned by the language processor
     * @throws CompilerException
     */
    @Throws(CompilerException::class)
    fun compile(): CompilerResult
}
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
package de.fhg.isst.oe270.degree.parsing.utils

import de.fhg.isst.oe270.degree.parsing.types.QualifiedName
// import nukleus.core.Identifier
import nukleus.core.Identifier

/**
 * These utils allow the transformation between different types.
 */
object TransformationUtils {

    /**
     * Transforms a qualified name (grammar element) into a qualified identifier (compiler element)
     *
     * @param qualifiedName qualified name to be transformed
     */
    @JvmStatic
    fun toIdentifier(qualifiedName: QualifiedName): Identifier {
        if (qualifiedName.qualifier.isBlank())
            return Identifier(qualifiedName.name)
        return Identifier(qualifiedName.qualifier + Identifier.SEPARATOR + qualifiedName.name)
    }

}

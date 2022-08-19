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
package de.fhg.isst.oe270.degree.core.policies.text

import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import de.fhg.isst.oe270.degree.util.SubSystemUtils
import nukleus.core.Identifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TextConstraintsTests {

    private val typeTaxonomy : TypeTaxonomy = TypeTaxonomy.getInstance()

    private val contentA = "Lorem"

    private val contentB = "Lorem ipsum"

    private val maxLengthVal = "5"

    init {
        if (typeTaxonomy.size() == 0) {
            SubSystemUtils.updateSubSystems()
            typeTaxonomy.load(Paths.get(Configuration.CORE_TYPES_FILE_PATH))
        }
    }

    @Test
    fun `Test that constraint core_MaxLength rejects missing input`() {
        val content = typeTaxonomy.create(Identifier.of("core.Text"))
        content.write(contentA)
        // create input scope
        val inputScope = PolicyInputScope()
        inputScope.add("content", content)

        // create and call constraint
        val constraint = MaxLength()
        val result = constraint.acceptPrecondition(inputScope)

        // check for correct result
        assertFalse(result,
                "Validation was successful although input is missing.")
    }

    @Test
    fun `Test that constraint core_MaxLength rejects wrong input types`() {
        val content = typeTaxonomy.create(Identifier.of("core.Text"))
        content.write(contentA)
        val maxLength = typeTaxonomy.create(Identifier.of("core.Error"))
        maxLength.write(maxLengthVal)
        // create input scope
        val inputScope = PolicyInputScope()
        inputScope.add("content", content)
        inputScope.add("maxLength", maxLength)

        // create and call constraint
        val constraint = MaxLength()
        val result = constraint.acceptPrecondition(inputScope)

        // check for correct result
        assertFalse(result,
                "Validation was successful although input types do not match.")
    }

    @Test
    fun `Test that constraint core_MaxLength rejects too long content`() {
        val content = typeTaxonomy.create(Identifier.of("core.Text"))
        content.write(contentB)
        val maxLength = typeTaxonomy.create(Identifier.of("core.UnsignedInt"))
        maxLength.write(maxLengthVal)
        // create input scope
        val inputScope = PolicyInputScope()
        inputScope.add("content", content)
        inputScope.add("maxLength", maxLength)

        // create and call constraint
        val constraint = MaxLength()
        val result = constraint.acceptPrecondition(inputScope)

        // check for correct result
        assertFalse(result,
                "Validation was successful although input is too long.")
    }

    @Test
    fun `Test that constraint core_MaxLength accepts content with matching length`() {
        val content = typeTaxonomy.create(Identifier.of("core.Text"))
        content.write(contentA)
        val maxLength = typeTaxonomy.create(Identifier.of("core.UnsignedInt"))
        maxLength.write(maxLengthVal)
        // create input scope
        val inputScope = PolicyInputScope()
        inputScope.add("content", content)
        inputScope.add("maxLength", maxLength)

        // create and call constraint
        val constraint = MaxLength()
        val result = constraint.acceptPrecondition(inputScope)

        // check for correct result
        assertTrue(result,
                "Validation failed although input is not too long.")
    }

}
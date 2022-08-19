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
package de.fhg.isst.oe270.degree.core.policies

import de.fhg.isst.oe270.degree.core.policies.date.UseNotAfter
import de.fhg.isst.oe270.degree.core.policies.date.UseNotBefore
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import de.fhg.isst.oe270.degree.util.SubSystemUtils
import nukleus.core.CompositeInstance
import nukleus.core.Identifier
import nukleus.core.Instance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DateConstraintsTests {

    private val typeTaxonomy : TypeTaxonomy = TypeTaxonomy.getInstance()

    init {
        if (typeTaxonomy.size() == 0) {
            SubSystemUtils.updateSubSystems()
            typeTaxonomy.load(Paths.get(Configuration.CORE_TYPES_FILE_PATH))
        }
    }

    private fun generateDate(future : Boolean) : Instance {
        val year = typeTaxonomy.newInstance(Identifier.of("core.UnsignedInt"))
        if (future) {
            year.write((Calendar.getInstance().get(Calendar.YEAR) + 5).toString())
        } else {
            year.write((Calendar.getInstance().get(Calendar.YEAR) - 5).toString())
        }
        val date = typeTaxonomy.newInstance(Identifier.of("LocalDateTime")) as CompositeInstance
        date.write(Identifier.of("Year"), year.read())
        return date
    }

    @Test
    fun `Test that constraint core_UseNotBefore rejects missing input`() {
        val inputScope = PolicyInputScope()

        val constraint = UseNotBefore()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result,
                "Validation did not fail although the required input was missing.")
    }

    @Test
    fun `Test that constraint core_UseNotBefore rejects wrong input type`() {
        val input = typeTaxonomy.newInstance(Identifier.of("core.Text"))
        input.write("12:00 01.01.1990")
        val inputScope = PolicyInputScope()
        inputScope.add("timestamp", input)

        val constraint = UseNotBefore()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result,
                "Validation was successful although the input type is not matching the required one.")
    }

    @Test
    fun `Test that constraint core_UseNotBefore rejects if the min time is in the future`() {
        val inputScope = PolicyInputScope()
        inputScope.add("timestamp", generateDate(true))

        val constraint = UseNotBefore()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result,
                "Validation was successful although the minimum date is after the current one.")
    }

    @Test
    fun `Test that constraint core_UseNotBefore accepts if the min time is in the past`() {
        val inputScope = PolicyInputScope()
        inputScope.add("timestamp", generateDate(false))

        val constraint = UseNotBefore()
        val result = constraint.acceptPrecondition(inputScope)

        assertTrue(result,
                "Validation was not successful although the minimum date is before the current one.")
    }

    @Test
    fun `Test that constraint core_UseNotAfter rejects missing input`() {
        val inputScope = PolicyInputScope()

        val constraint = UseNotAfter()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result,
                "Validation did not fail although the required input was missing.")
    }

    @Test
    fun `Test that constraint core_UseNotAfter rejects wrong input type`() {
        val input = typeTaxonomy.newInstance(Identifier.of("core.Text"))
        input.write("12:00 01.01.1990")
        val inputScope = PolicyInputScope()
        inputScope.add("timestamp", input)

        val constraint = UseNotAfter()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result,
                "Validation was successful although the input type is not matching the required one.")
    }

    @Test
    fun `Test that constraint core_UseNotAfter rejects if the max time is in the past`() {
        val inputScope = PolicyInputScope()
        inputScope.add("timestamp", generateDate(false))

        val constraint = UseNotAfter()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result,
                "Validation was successful although the maximum date is before the current one.")
    }

    @Test
    fun `Test that constraint core_UseNotAfter accepts if the max time is in the future`() {
        val inputScope = PolicyInputScope()
        inputScope.add("timestamp", generateDate(true))

        val constraint = UseNotAfter()
        val result = constraint.acceptPrecondition(inputScope)

        assertTrue(result,
                "Validation was not successful although the maximum date is after the current one.")
    }

}
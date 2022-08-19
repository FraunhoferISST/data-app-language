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
package de.fhg.isst.oe270.degree.core.policies.tags

import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration

import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadOnlyEntity
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import de.fhg.isst.oe270.degree.util.SubSystemUtils
import nukleus.core.Identifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TagConstraintsTests {

    private val typeTaxonomy : TypeTaxonomy = TypeTaxonomy.getInstance()

    private val tagsContextModule = ExecutionContext.getInstance().getModule("TagsContextModule")

    private val tagA = "DEBUG"

    private val tagB = "PRODUCTION"

    init {
        if (typeTaxonomy.size() == 0) {
            SubSystemUtils.updateSubSystems()
            typeTaxonomy.load(Paths.get(Configuration.CORE_TYPES_FILE_PATH))
        }
    }

    private fun addTag(tag : String) {
        tagsContextModule.addContextEntity(tag, ReadOnlyEntity(tag, tag))
    }

    private fun removeTag(tag : String) {
        tagsContextModule.removeContextEntity(tag)
    }

    @Test
    fun `Test that constraint core_DenyTag rejects missing input`() {
        // create input scope
        val inputScope = PolicyInputScope()

        addTag(tagA)

        // create and call constraint
        val constraint = DenyTag()
        val result = constraint.acceptPrecondition(inputScope)

        removeTag(tagA)

        // check for correct result
        assertFalse(result,
                "Validation was successful although input is missing.")
    }

    @Test
    fun `Test that constraint core_DenyTag rejects wrong input types`() {
        // create input scope
        val tag = typeTaxonomy.create(Identifier.of("core.Error"))
        tag.write(tagA)
        val inputScope = PolicyInputScope()
        inputScope.add("tag", tag)

        addTag(tagA)

        // create and call constraint
        val constraint = DenyTag()
        val result = constraint.acceptPrecondition(inputScope)

        removeTag(tagA)

        // check for correct result
        assertFalse(result,
                "Validation was successful although input type is not matching.")
    }

    @Test
    fun `Test that constraint core_DenyTag accepts non forbidden tags`() {
        // create input scope
        val tag = typeTaxonomy.create(Identifier.of("core.Tag"))
        tag.write(tagB)
        val inputScope = PolicyInputScope()
        inputScope.add("tag", tag)

        addTag(tagA)

        // create and call constraint
        val constraint = DenyTag()
        val result = constraint.acceptPrecondition(inputScope)

        removeTag(tagA)

        // check for correct result
        assertTrue(result,
                "Validation failed although given tag does not match the forbidden one.")
    }

    @Test
    fun `Test that constraint core_DenyTag rejects matching tags`() {
        // create input scope
        val tag = typeTaxonomy.create(Identifier.of("core.Tag"))
        tag.write(tagA)
        val inputScope = PolicyInputScope()
        inputScope.add("tag", tag)

        addTag(tagA)

        // create and call constraint
        val constraint = DenyTag()
        val result = constraint.acceptPrecondition(inputScope)

        removeTag(tagA)

        // check for correct result
        assertFalse(result,
                "Validation was successful although given tag matches the forbidden one.")
    }

    @Test
    fun `Test that constraint core_RequireTag rejects missing input`() {
        // create input scope
        val inputScope = PolicyInputScope()

        addTag(tagA)

        // create and call constraint
        val constraint = RequireTag()
        val result = constraint.acceptPrecondition(inputScope)

        removeTag(tagA)

        // check for correct result
        assertFalse(result,
                "Validation was successful although input is missing.")
    }

    @Test
    fun `Test that constraint core_RequireTag rejects wrong input types`() {
        // create input scope
        val tag = typeTaxonomy.create(Identifier.of("core.Error"))
        tag.write(tagA)
        val inputScope = PolicyInputScope()
        inputScope.add("tag", tag)

        addTag(tagA)

        // create and call constraint
        val constraint = RequireTag()
        val result = constraint.acceptPrecondition(inputScope)

        removeTag(tagA)

        // check for correct result
        assertFalse(result,
                "Validation was successful although input type is not matching.")
    }

    @Test
    fun `Test that constraint core_RequireTag accepts matching tags`() {
        // create input scope
        val tag = typeTaxonomy.create(Identifier.of("core.Tag"))
        tag.write(tagA)
        val inputScope = PolicyInputScope()
        inputScope.add("tag", tag)

        addTag(tagA)

        // create and call constraint
        val constraint = RequireTag()
        val result = constraint.acceptPrecondition(inputScope)

        removeTag(tagA)

        // check for correct result
        assertTrue(result,
                "Validation failed although given tag matches the required one.")
    }

    @Test
    fun `Test that constraint core_RequireTag rejects non-matching tags`() {
        // create input scope
        val tag = typeTaxonomy.create(Identifier.of("core.Tag"))
        tag.write(tagB)
        val inputScope = PolicyInputScope()
        inputScope.add("tag", tag)

        addTag(tagA)

        // create and call constraint
        val constraint = RequireTag()
        val result = constraint.acceptPrecondition(inputScope)

        removeTag(tagA)

        // check for correct result
        assertFalse(result,
                "Validation was successful although given tag does not match the required one.")
    }

}
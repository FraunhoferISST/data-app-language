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
package de.fhg.isst.oe270.degree.compiler.generators.java.templates.blocks

import de.fhg.isst.oe270.degree.compiler.CompilerMessage
import de.fhg.isst.oe270.degree.compiler.generators.java.templates.JavaTemplateProcessor
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*

class IncludeTemplateProcessor(val value: String, private val compilerMessages : MutableList<CompilerMessage>) {

    companion object {
        /**
         * Template used to create <include> entries used inside <includes>.
         */
        const val INCLUDE_ENTRY_TEMPLATE = JavaTemplateProcessor.JAVA_TEMPLATES + "includeEntry.vm"
    }

    private val velocityContext = VelocityContext()

    init {
        // validate inputs
        assert(!value.isBlank())

        val properties = Properties().also {
            it.setProperty("resource.loader", "class")
            it.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader")
        }

        Velocity.init(properties)

        // prepare template creation
        velocityContext.put("value", value)
    }

    /**
     * Generate the <dependency> entry for the pom which should be used in <dependencies>
     *
     * @return Valid representation of a maven dependency which can be inserted into a pom.
     */
    fun createIncludeEntry(): String {
        return TemplateProcessor.processTemplate(INCLUDE_ENTRY_TEMPLATE, velocityContext, compilerMessages)
    }

}
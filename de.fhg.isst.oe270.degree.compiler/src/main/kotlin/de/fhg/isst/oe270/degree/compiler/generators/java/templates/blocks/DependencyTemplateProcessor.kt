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
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.io.StringWriter
import java.util.*

/**
 * Velocity processor used to create dependency entries for data app pom's.
 *
 * @param groupId maven's grouId used for the dependency
 * @param artifactId  maven's artifactId used for the dependency
 * @param version version of the maven dependency
 * @param scope scope of the maven dependency. Defaults to 'compile'
 * @param systemPath Optional path to a jar file if the dependency is not registered in maven's repository.
 *                   This is the case for D°-extensions. Defaults to null
 * @param compilerMessages If there are errors, they are logged to the compiler messages
 */
class DependencyTemplateProcessor(val groupId: String,
                                  val artifactId: String,
                                  val version: String,
                                  private var scope: String = "compile",
                                  private val systemPath: String? = null,
                                  private val compilerMessages : MutableList<CompilerMessage>) {

    companion object {

        /**
         * Template used to create <dependency> entries used inside <dependencies>.
         */
        const val DEPENDENCY_ENTRY_TEMPLATE = JavaTemplateProcessor.JAVA_TEMPLATES + "dependencyEntry.vm"

        /**
         * Template used to create <dependency> entries used inside <dependencyManagement>.
         */
        const val DEPENDENCY_MANAGEMENT_ENTRY_TEMPLATE = JavaTemplateProcessor.JAVA_TEMPLATES + "dependencyManagementEntry.vm"

        /**
         * Template used to create <property> entries used inside <properties>.
         */
        const val PROPERTY_ENTRY_TEMPLATE = JavaTemplateProcessor.JAVA_TEMPLATES + "propertyEntry.vm"

    }

    private val velocityContext = VelocityContext()

    init {
        // validate inputs
        assert(arrayOf("compile", "provided", "runtime", "test", "system", "import").contains(scope))

        val properties = Properties().also {
            it.setProperty("resource.loader", "class")
            it.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader")
        }

        Velocity.init(properties)

        // prepare template creation
        velocityContext.put("key", "$artifactId.system.path")
        velocityContext.put("value", systemPath)

        velocityContext.put("groupId", groupId)
        velocityContext.put("artifactId", artifactId)
        velocityContext.put("version", version)


        if (scope == "system") {
            installSystemJar()
            scope = "compile"
        }

        velocityContext.put("scope", scope)
    }

    /**
     * In case a system dependency is given, this function installs it into the local m2 repository and makes it
     * available as "normal" maven dependency.
     */
    private fun installSystemJar() {
        val request = DefaultInvocationRequest()
        request.goals = listOf("install:install-file")
        val properties = Properties()
        properties["groupId"] = groupId
        properties["artifactId"] = artifactId
        properties["version"] = version
        properties["file"] = systemPath
        properties["packaging"] = "jar"
        request.properties = properties

        val invoker = DefaultInvoker()
        // deactivate output
        invoker.setOutputHandler {  }
        val result = invoker.execute(request)

        // check for errors
        if (result.exitCode != 0) {
            compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.ERROR,
                            "Error during installation of a D° extension into the maven repository: ${result.executionException.cause}",
                            systemPath
                    )
            )
        }
    }

    /**
     * Generate the <dependency> entry for the pom which should be used in <dependencies>
     *
     * @return Valid representation of a maven dependency which can be inserted into a pom.
     */
    fun createDependencyEntry(): String {
        return TemplateProcessor.processTemplate(DEPENDENCY_ENTRY_TEMPLATE, velocityContext, compilerMessages)
    }

    /**
     * Generate the <dependency> entry for the pom which should be used in <dependencyManagement>
     *
     * @return Valid representation of a maven dependency which can be inserted into a pom.
     */
    fun createDependencyManagementEntry(): String {
        return TemplateProcessor.processTemplate(DEPENDENCY_MANAGEMENT_ENTRY_TEMPLATE, velocityContext, compilerMessages)
    }

    fun createPropertyEntry(): String {
        return TemplateProcessor.processTemplate(PROPERTY_ENTRY_TEMPLATE, velocityContext, compilerMessages)
    }

}
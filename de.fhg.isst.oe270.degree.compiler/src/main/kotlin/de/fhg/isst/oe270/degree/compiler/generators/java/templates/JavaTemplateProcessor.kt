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
package de.fhg.isst.oe270.degree.compiler.generators.java.templates

import de.fhg.isst.oe270.degree.compiler.CompilerConfiguration
import de.fhg.isst.oe270.degree.compiler.CompilerMessage
import de.fhg.isst.oe270.degree.compiler.generators.java.templates.blocks.DependencyTemplateProcessor
import de.fhg.isst.oe270.degree.compiler.generators.java.templates.blocks.IncludeTemplateProcessor
import de.fhg.isst.oe270.degree.compiler.generators.java.templates.blocks.PropertyTemplateProcessor
import de.fhg.isst.oe270.degree.grammar.ast.model.DataApp
import de.fhg.isst.oe270.degree.runtime.java.data.app.CliDataApp
import de.fhg.isst.oe270.degree.runtime.java.data.app.TcpIpDataApp
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.io.File
import java.io.StringWriter
import java.util.*

/**
 * This class processes all templates for java generation.
 * The processed templates are much easier than the generated code which is the reason for the
 * differentiation between code generator and template processor.
 * The output of this class in combination with the {@link JavaAppGenerator} is a valid executable java application.
 */
class JavaTemplateProcessor (private val generatedCodeDir : String,
                             private val compilerConfiguration: CompilerConfiguration,
                             private val compilerMessages : MutableList<CompilerMessage>,
                             dataApp: DataApp) {

    companion object {

        /**
         * All java templates can be found here.
         */
        const val JAVA_TEMPLATES = "templates/Java/"
        /**
         * Location of the parent.pom template.
         */
        const val PARENT_POM_TEMPLATE = JAVA_TEMPLATES + "parentPom.vm"
        /**
         * Location of the application.properties template.
         */
        const val APPLICATION_PROPERTIES_TEMPLATE = JAVA_TEMPLATES + "applicationProperties.vm"

    }

    /**
     * Context of the template engine.
     */
    private val velocityContext = VelocityContext()

    init {
        val properties = Properties().also {
            it.setProperty("resource.loader", "class");
            it.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        }

        Velocity.init(properties)
        // fill template engine with data for this data app
        velocityContext.put("namespace", dataApp.configurationItems[CliDataApp.NAMESPACE_KEY])
        velocityContext.put("name", dataApp.configurationItems[CliDataApp.NAME_KEY])
        velocityContext.put("version", dataApp.configurationItems[CliDataApp.VERSION_KEY])
        velocityContext.put("modules", "")
        velocityContext.put("properties", "")
        velocityContext.put("dependency_management", "")
        velocityContext.put("dependencies", "")
        velocityContext.put("plugin_management", "")
        velocityContext.put("plugins", "")
        velocityContext.put("repositories", "")
        velocityContext.put("plugin_repositories", "")
        velocityContext.put("includes", "")
        velocityContext.put("applicationProperties",
                "###################################\n" +
                "# Injected application properties #\n" +
                "###################################\n")
        // process TcpIpDataApp specific fields
        if (dataApp.configurationItems.containsKey(TcpIpDataApp.PORT_KEY)) {
            velocityContext.put("server_port", "server.port="+dataApp.configurationItems[TcpIpDataApp.PORT_KEY])
        }
    }

    /**
     * Add an include to the pom.
     *
     * @param fileName file name which will be added as an include
     */
    fun addInclude(fileName: String) {
        val processor = IncludeTemplateProcessor(fileName, compilerMessages)

        velocityContext.put("includes",
                (velocityContext.get("includes") as String) + processor.createIncludeEntry())
    }

    fun addProperty(key: String, value: String) {
        val processor = PropertyTemplateProcessor(key, value, compilerMessages)

        velocityContext.put("applicationProperties",
                (velocityContext.get("applicationProperties") as String) + processor.createEntry())
    }

    /**
     * Add a dependency (and its dependencyManagement entry) to the pom.
     *
     * @param groupId maven's grouId used for the dependency
     * @param artifactId  maven's artifactId used for the dependency
     * @param version version of the maven dependency
     * @param scope scope of the maven dependency. Defaults to 'compile'
     * @param systemPath Optional path to a jar file if the dependency is not registered in maven's repository.
     *                   This is the case for D°-extensions. Defaults to null
     */
    fun addDependency(groupId: String, artifactId: String, version: String, scope: String = "compile", systemPath: String? = null) {
        val processor = DependencyTemplateProcessor(groupId, artifactId, version, scope, systemPath, compilerMessages)

        velocityContext.put("dependency_management",
                (velocityContext.get("dependency_management") as String) + processor.createDependencyManagementEntry())
        velocityContext.put("dependencies",
                (velocityContext.get("dependencies") as String) + processor.createDependencyEntry())
        //velocityContext.put("properties",
        //        (velocityContext.get("properties") as String) + processor.createPropertyEntry())
    }

    /**
     * Entrypoint for processing different templates which will be stored as individual files.
     *
     * @param template name of the used template
     * @param destination location of the destination file, where the result is stored.
     * @return true, if the template processing was successful, false otherwise.
     */
    fun processTemplate(template : String, destination : String) : Boolean {
        val stringWriter = StringWriter()
        if (!Velocity.mergeTemplate(template, Charsets.UTF_8.displayName(), velocityContext, stringWriter)) {
            compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.ERROR,
                            "Could not process the template.",
                            template)
            )
            return false
        }
        // write processed template to file
        val destinationFile = File(generatedCodeDir + File.separator + destination)
        destinationFile.printWriter().use { it.print(stringWriter) }

        return true
    }

}

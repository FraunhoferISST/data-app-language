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

import de.fhg.isst.degree.types.gen.degree.*
import de.fhg.isst.oe270.degree.compiler.generators.DataAppTypes
import de.fhg.isst.oe270.degree.compiler.generators.java.JavaAppGenerator
import de.fhg.isst.oe270.degree.compiler.generators.java.templates.JavaTemplateProcessor
import de.fhg.isst.oe270.degree.runtime.java.data.app.CliDataApp
import de.fhg.isst.oe270.degree.types.RuntimeDefinitionRegistry
import nukleus.core.Identifier
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class DegreeToJavaCompiler(
        compilerConfiguration: CompilerConfiguration,
        compilerMessages: MutableList<CompilerMessage>)
    : AbstractDegreeCompiler(compilerConfiguration, compilerMessages) {

    public var javaTemplateProcessor : JavaTemplateProcessor? = null

    init {

    }

    override fun generateCode() {
        // the generated code goes here
        val generatedCodeDirPath = compilerConfiguration.dataAppSourceRootDir + File.separator + "generated"
        val generatedSrcDirPath = generatedCodeDirPath + File.separator + "src" + File.separator + "main" + File.separator + "java"
        val generatedResourcesDirPath = generatedCodeDirPath + File.separator + "src" + File.separator + "main" + File.separator + "resources"
        val generatedCodeDir = File(generatedCodeDirPath)
        // (remove & re-)create folder
        if (generatedCodeDir.exists()) {
            if (!generatedCodeDir.deleteRecursively()) {
                compilerMessages.add(
                        CompilerMessage(CompilerMessage.Kind.ERROR,
                                "Could not clean directory for generated code."))
                return
            }
        }
        generatedCodeDir.mkdirs()
        File(generatedSrcDirPath).mkdirs()
        File(generatedResourcesDirPath).mkdirs()

        // instantiate necessary engines/generators
        javaTemplateProcessor = JavaTemplateProcessor(generatedCodeDirPath, compilerConfiguration, compilerMessages, dataApp!!)
        val javaAppGenerator = JavaAppGenerator(generatedCodeDirPath, compilerConfiguration, compilerMessages, dataApp!!, this)

        // actual code generation
        if (!javaAppGenerator.generateDataApp()) {
            return
        }

        // register startup policies
        val startupPolicies = dataApp!!.configurationItems[CliDataApp.STARTUP_POLICIES_KEY] ?: ""

        val runtimeDefinitionRegistry = RuntimeDefinitionRegistry.getInstance()
        val policyRegistry = runtimeDefinitionRegistry.policyRegistry()
        if (startupPolicies.isNotEmpty()) {
            startupPolicies.split(",").map { it.trim() }.map { policy ->
                val identifier = Identifier.of(policy)

                // add startup policies to export
                val policyObject = runtimeDefinitionRegistry.lookup(identifier)
                if (policyObject is ConstraintInstance) {
                    val polDef = policyObject.definition.lookup()
                    for (j in 0 until polDef.attribute.size())
                        addTypeToExportSystem(Identifier.of(polDef.attribute[j].type_.read()))

                    // constraint and its definition needs to be added
                    addConstraintInstanceToExport(policyObject)
                } else {
                    // this is a policy instance which cannot define input types but can contain an arbitrary amount of
                    // policies and constraints
                    addPolicyInstanceToExport(policyObject as PolicyInstance)
                }
            }
        }

        // check if we need to transfor files from the project to the generated files
        if (javaAppGenerator.dataAppType.equals(DataAppTypes.IDS_HTTP)) {
            if (Files.exists(Paths.get(compilerConfiguration.dataAppSourceRootDir + File.separator + "infomodel.json"))) {
                Files.copy(
                    Paths.get(compilerConfiguration.dataAppSourceRootDir + File.separator + "infomodel.json"),
                    Paths.get(generatedResourcesDirPath + File.separator + "infomodel.json")
                )
            } else {
                compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.ERROR,
                        "Missing infomodel.json. The file is mandatory for IDS HTTP apps."))
            }
        }

        // inject the custom runtime system into the generated code
        val runtime = exportRuntimeDefinitionRegistry.serialize().toString()
        Files.write(Paths.get(generatedResourcesDirPath + File.separator + "registry.json"), runtime.toByteArray())
        val policyDefCount = exportRuntimeDefinitionRegistry.policyRegistry()
                .instanceSet(Policy().type).size
        val constraintDefCount = exportRuntimeDefinitionRegistry.policyRegistry()
                .instanceSet(Constraint().type).size
        val activityDefCount = exportRuntimeDefinitionRegistry.activityRegistry()
                .instanceSet(Activity().type).size
        val policyInstanceCount = exportRuntimeDefinitionRegistry.policyRegistry()
                .instanceSet(PolicyInstance().type).size
        val constraintInstanceCount = exportRuntimeDefinitionRegistry.policyRegistry()
                .instanceSet(ConstraintInstance().type).size
        val activityInstanceCount = exportRuntimeDefinitionRegistry.activityRegistry()
                .instanceSet(ActivityInstance().type).size
        val activityCount = activityDefCount + activityInstanceCount
        val policyCount = policyDefCount + policyInstanceCount
        val constraintCount = constraintDefCount + constraintInstanceCount
        compilerMessages.add(
                CompilerMessage(CompilerMessage.Kind.INFO,
                        "Exported a custom element registry with ${exportRuntimeDefinitionRegistry.size()} elements for the data app."))
        compilerMessages.add(
                CompilerMessage(CompilerMessage.Kind.INFO,
                        "The custom registry contains " +
                                "$activityCount ${if (activityCount == 1) "activity" else "activities"} (" +
                                "$activityDefCount ${if (activityDefCount == 1) "definition" else "definitions"} / " +
                                "$activityInstanceCount ${if (activityInstanceCount == 1) "instance" else "instances"})."))
        compilerMessages.add(
                CompilerMessage(CompilerMessage.Kind.INFO,
                        "The custom registry contains " +
                                "$policyCount ${if (policyCount == 1) "policy" else "policies"} (" +
                                "$policyDefCount ${if (policyDefCount == 1) "definition" else "definitions"} / " +
                                "$policyInstanceCount ${if (policyInstanceCount == 1) "instance" else "instances"})."))
        compilerMessages.add(
                CompilerMessage(CompilerMessage.Kind.INFO,
                        "The custom registry contains " +
                                "$constraintCount ${if (constraintCount == 1) "constraint" else "constraints"} (" +
                                "$constraintDefCount ${if (constraintDefCount == 1) "definition" else "definitions"} / " +
                                "$constraintInstanceCount ${if (constraintInstanceCount == 1) "instance" else "instances"})."))

        compilerMessages.add(
                CompilerMessage(CompilerMessage.Kind.INFO,
                        "Injected ${compilerConfiguration.applicationProperties.size} application " +
                                if (compilerConfiguration.applicationProperties.size == 1) "property" else "properties" +
                                " into the application."))

        // register the data app's inputs in the exported type system
        dataApp!!.inputs.map { (_, type) -> addTypeToExportSystem(Identifier.of(type.first.toString())) }

        // inject the custom type system into the generated code
        val types = exportTypeSystem.serialize().toString()
        Files.write(Paths.get(generatedResourcesDirPath + File.separator + "types.json"), types.toByteArray())
        compilerMessages.add(
                CompilerMessage(CompilerMessage.Kind.INFO,
                        "Exported a custom type system with ${exportTypeSystem.size()} types for the data app."))

        // inject application properties if available
        for (entry in compilerConfiguration.applicationProperties.entries) {
            javaTemplateProcessor!!.addProperty(entry.key as String, entry.value as String)
        }

        // inject the nukleus policies if they are available
        if (compilerConfiguration.nukleusPoliciesResource != null) {
            Files.copy(compilerConfiguration.nukleusPoliciesResource!!.toPath(),
                    Paths.get(generatedResourcesDirPath + File.separator + "nukleus.policies.yaml"))
            javaTemplateProcessor!!.addInclude("nukleus.policies.yaml")
        }

        if (!javaTemplateProcessor!!.processTemplate(JavaTemplateProcessor.PARENT_POM_TEMPLATE, "pom.xml")) {
            return
        }
        if (!javaTemplateProcessor!!.processTemplate(JavaTemplateProcessor.APPLICATION_PROPERTIES_TEMPLATE, "src/main/resources/application.properties")) {
            return
        }

        // check if errors occurred before the actual compilation into an executable jar starts
        this.compilerMessages.forEach { message ->
            if (message.kind == CompilerMessage.Kind.ERROR) {
                return
            }
        }

        // run maven to build the final Data App jar
        val request = DefaultInvocationRequest()
        request.pomFile = File(generatedCodeDirPath + File.separator + "pom.xml")
        request.goals = listOf("clean", "package")

        val invoker = DefaultInvoker()
        val result = invoker.execute(request)

        // check for errors
        if (result.exitCode != 0) {
            compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.ERROR,
                            "Error during compilation of generated Java code.",
                            "" //TODO
                    )
            )
        }
    }

    override fun addDependency(groupId: String, artifactId: String, version: String, scope: String, systemPath: String?) {
        javaTemplateProcessor!!.addDependency(groupId, artifactId, version, scope, systemPath)
    }
}

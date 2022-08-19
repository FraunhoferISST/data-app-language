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

import de.fhg.isst.oe270.degree.compiler.Entrypoint.startCompile
import de.fhg.isst.oe270.degree.compiler.agents.JarLoaderAgent
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.types.RuntimeDefinitionRegistry
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import nukleus.core.Nukleus
import nukleus.core.custom.DegreeCustomization
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.jar.JarFile
import kotlin.system.exitProcess

object Entrypoint {
    private fun createDefaultCompilerConfiguration(compilerProperties: Properties, compilerConfiguration: CompilerConfiguration) {
        // load data from static configuration
        with(compilerConfiguration.sourceRootDirs) {
            add(Configuration.SUBSYSTEM_FOLDER)
            add(Configuration.SUBSYSTEM_EXTENSIONS_FOLDER)
        }
        // process values which can not be set by the user
        compilerConfiguration.compilerVersion = compilerProperties.getProperty("compiler.version")

        // process standard values
        loadCompilerConfiguration(compilerProperties)
    }

    private fun loadCompilerConfiguration(externalConfig: Properties) {
        externalConfig.propertyNames().asSequence().asIterable().forEach { property ->
            when (property as String) {

            }
        }
    }

    private fun createCommandLineOptions(): Options {
        val options = Options()

        options.addOption("v", "version", false, "print the version of this D° compiler")
        options.addOption("h", "help", false, "print this message")
        options.addOption("c", "config-file", false, "path to external compiler configuration")

        return options
    }

    private fun processCommandline(options: Options, args: Array<String>, compilerConfiguration: CompilerConfiguration) {
        val commandLine = DefaultParser().parse(options, args)
        // print version information
        if (commandLine.hasOption('v')) {
            print("D° compiler version: ${compilerConfiguration.compilerVersion}")
            exitProcess(0)
        }
        // print help
        if (commandLine.hasOption('h')) {
            val formatter = HelpFormatter()
            formatter.printHelp("degreec [options] [sourcedir]", options)
            exitProcess(0)
        }
        // load external configuration
        if (commandLine.hasOption("c")) {
            val externalConfig = commandLine.getOptionValue("c")
            if (externalConfig == null) {
                print("No external configuration file specified.")
                exitProcess(-1)
            }
            val externalProperties = Properties()
            externalProperties.load(File(externalConfig).inputStream())
            loadCompilerConfiguration(externalProperties)
        }
        // sanity checks
        // only one argument (aka data app source root dir) is allowed
        if (commandLine.args.size > options.options.size) {
            print("Too many arguments. Use 'java -jar degreec.jar help' for usage information.")
            exitProcess(-1)
        }
        if (commandLine.args.isEmpty()) {
            print("Missing arguments. Use 'java -jar degreec.jar help' for usage information.")
            exitProcess(-1)
        }
        // Set (data app) source root dir
        commandLine.args.map {
            compilerConfiguration.sourceRootDirs.add(it)
            compilerConfiguration.dataAppSourceRootDir = it
        }
    }

    private fun configureCompiler(compilerConfiguration: CompilerConfiguration, compilerMessages: LinkedList<CompilerMessage>) {
        // at first all jar files which are contained in the source root dirs will be added to the classpath
        compilerConfiguration.sourceRootDirs.map { dir ->
            File(dir).listFiles()?.map { file ->
                if (FilenameUtils.getExtension(file.name).toLowerCase() == "jar") {
                    if (!JarLoaderAgent.addJarToClasspath(file.absolutePath)) {
                        compilerMessages.add(CompilerMessage(
                                CompilerMessage.Kind.ERROR,
                                "Could not load JAR-file '${file.name}'.",
                                file.name
                        ))
                    } else {
                        compilerMessages.add(CompilerMessage(
                                CompilerMessage.Kind.INFO,
                                "Registered JAR-file '${file.name}'."
                        ))
                        // check if there are any yaml files inside the loaded jar
                        val classLoader = JarLoaderAgent.CLASS_LOADERS.last()
                        val jar = JarFile(classLoader.urLs[0].toString().split("file:/").last())
                        val typeFiles = jar.entries().toList().filter { it.name.endsWith(".types.yaml") }.map { it.name }
                        if (typeFiles.isNotEmpty())
                            compilerConfiguration.typeYamlResources[classLoader] = typeFiles
                        val registryFiles = jar.entries().toList().filter { it.name.endsWith(".registry.yaml") }.map { it.name }
                        if (registryFiles.isNotEmpty())
                            compilerConfiguration.registryYamlResources[classLoader] = registryFiles

                        //Scanner(JarLoaderAgent.classLoaders.last().getResourceAsStream("demonstrator.types.yaml")).useDelimiter("\\A").next()
                    }
                }
            }
        }
    }

    private fun logCompilerMessages(logger: Logger, compilerMessages: Collection<CompilerMessage>, vararg compilerMessageKindsToLog: CompilerMessage.Kind = CompilerMessage.Kind.values()) {
        compilerMessages
                .filter { it.kind in compilerMessageKindsToLog }
                .map {
                    val logMethod: (Logger, String) -> Unit = when (it.kind) {
                        CompilerMessage.Kind.ERROR -> Logger::error
                        CompilerMessage.Kind.WARNING -> Logger::warn
                        CompilerMessage.Kind.INFO -> Logger::info
                        CompilerMessage.Kind.DEBUG -> Logger::debug
                    }
                    var message = ""
                    if (it.file != null) {
                        message = "${it.file}:${it.position?.start ?: ""} "
                    }
                    message += it.message
                    logMethod to message
                }
                .forEach { (logMethod, message) ->
                    logMethod.invoke(logger, message)
                }
    }

    fun startCompile(args: Array<String>): Boolean {
        val compilerConfiguration = CompilerConfiguration()
        // create compiler messages container for whole compilation process
        val compilerMessages = LinkedList<CompilerMessage>()
        // set the default namespace
        Nukleus.custom = DegreeCustomization()
        //Nukleus.logging("logging.properties");
        // load compiler.properties and initialize compiler configuration
        val compilerProperties = Properties()
        compilerProperties.load(DegreeToJavaCompiler::class.java.classLoader.getResourceAsStream("compiler.properties"))
        createDefaultCompilerConfiguration(compilerProperties, compilerConfiguration)
        // process the command line
        processCommandline(createCommandLineOptions(), args, compilerConfiguration)

        // all properties are read now and the command line is processed
        // so the "final" configuration can be done
        configureCompiler(compilerConfiguration, compilerMessages)

        val compiler = DegreeToJavaCompiler(compilerConfiguration, compilerMessages)
        try {
            compiler.compile()
        } catch (e: Exception) {
            compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.ERROR,
                            "A critical error occured during compilation: $e\n" +
                                    e.stackTrace.joinToString(separator = "\n") { it.toString() }))
        } finally {
            val logger: Logger = LoggerFactory.getLogger(DegreeToJavaCompiler::class.java.simpleName)
            logCompilerMessages(logger, compilerMessages)
        }
        // cleanup
        TypeTaxonomy.resetInstance()
        RuntimeDefinitionRegistry.resetInstance()

        return true//compilerMessages.any { it.kind == CompilerMessage.Kind.ERROR }
    }
}

fun main(args: Array<String>) {
    startCompile(args)
}

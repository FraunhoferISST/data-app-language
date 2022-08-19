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

import de.fhg.isst.degree.types.gen.core.DegreeTypeSystem
import de.fhg.isst.degree.types.gen.degree.*
import de.fhg.isst.degree.types.gen.degree.Activity
import de.fhg.isst.oe270.degree.activities.annotations.ActivityAnnotation
import de.fhg.isst.oe270.degree.activities.api.ActivityApi
import de.fhg.isst.oe270.degree.compiler.agents.JarLoaderAgent
import de.fhg.isst.oe270.degree.grammar.DegreeParserFacade
import de.fhg.isst.oe270.degree.grammar.ast.model.DataApp
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi
import de.fhg.isst.oe270.degree.registry.instances.execution.container.EmbeddedExecutionContainer
import de.fhg.isst.oe270.degree.registry.instances.execution.container.JavaExecutionContainer
import de.fhg.isst.oe270.degree.runtime.java.data.app.CliDataApp
import de.fhg.isst.oe270.degree.runtime.java.data.app.context.SpringContext
import de.fhg.isst.oe270.degree.types.RuntimeDefinitionRegistry
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import de.fhg.isst.oe270.degree.util.SubSystemUtils
import io.github.classgraph.ClassGraph
import nukleus.core.*
import nukleus.core.exception.TypeSystemException
import org.codehaus.plexus.util.DirectoryScanner
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Paths
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.HashMap
import kotlin.reflect.full.createInstance


abstract class AbstractDegreeCompiler(
        protected val compilerConfiguration: CompilerConfiguration,
        protected val compilerMessages: MutableList<CompilerMessage>
) : Compiler {

    companion object {
        const val SINGLE_EXECUTION_CONFIG_KEY = "single"
        const val PERIODIC_EXECUTION_CONFIG_KEY = "periodic"
    }

    @Suppress("unused")
    private val logger = LoggerFactory.getLogger(AbstractDegreeCompiler::class.java.simpleName)

    /**
     * This is the data app which will be compiled.
     */
    var dataApp : DataApp? = null

    /**
     * The type taxonomy contains all data types which can be used within the data app.
     */
    private val typeTaxonomy : TypeTaxonomy

    /**
     * The subsystem scope contains all elements (activities, constraints, policies, instances of these) which
     * can be used within the data app.
     */
    private val runtimeDefinitionRegistry : RuntimeDefinitionRegistry

    protected val exportTypeSystem : TypeTaxonomy

    protected val exportRuntimeDefinitionRegistry : RuntimeDefinitionRegistry

    val componentScanPackages = HashSet<String>()

    val additionalAnnotations = HashSet<String>()
    
    init {
        // ensure that all sub systems are up to date
        SubSystemUtils.updateSubSystems()
        // retrieve the type taxonomy
        typeTaxonomy = TypeTaxonomy.getInstance()
        // create the temporary type taxonomy that will be exported to the compiled data app
        exportTypeSystem = TypeTaxonomy.createTempInstance()
        // retrieve the various subsystems
        runtimeDefinitionRegistry = RuntimeDefinitionRegistry.getInstance()
        // create the temporary runtime definition registry that will be exported to the compiled data app
        exportRuntimeDefinitionRegistry = RuntimeDefinitionRegistry()
    }

    /**
     * Add an policy instance and its definition to the elements that will be exported to the generated app.
     *
     * @param polInst policy intsance that will be added
     */
    fun addPolicyInstanceToExport(polInst: PolicyInstance) {
        // process nested elements
        for (i in 0 until polInst.mappedElements.size()) {
            if (polInst.mappedElements[i].value.lookup().type == polInst.type) {
                addPolicyInstanceToExport(polInst.mappedElements[i].value.lookup() as PolicyInstance)
            } else {
                addConstraintInstanceToExport(polInst.mappedElements[i].value.lookup() as ConstraintInstance)
            }
        }
        // process the instance itself
        addElementToExportSystem(Identifier.of(polInst.definition.lookup().identity.reference))
        addElementToExportSystem(Identifier.of(polInst.name.read()))
    }

    /**
     * Add an constraint instance and its definition to the elements that will be exported to the generated app.
     * Also add the jar containing its implementation to the generated app.
     *
     * @param conInst constraint intsance that will be added
     */
    fun addConstraintInstanceToExport(conInst: ConstraintInstance) {
        val definition = conInst.definition.lookup()

        addElementToExportSystem(Identifier.of(definition.name.read()))
        addElementToExportSystem(Identifier.of(conInst.name.read()))
        addJarDependency(definition.name.read())

        // add used data types to export
        for (i in 0 until definition.attribute.size()) {
            addTypeToExportSystem(Identifier.of(definition.attribute[i].type_.read()))
        }
    }

    /**
     * Add a jar containing the implementation of an element with given identifier to the generated data app.
     *
     * @param ident identifier of the searched element
     */
    private fun addJarDependency(ident: String) {
        // get jar
        if (runtimeDefinitionRegistry.retrieveContainer(Identifier.of(ident)) is JavaExecutionContainer<*>) {
            val jar = (runtimeDefinitionRegistry.retrieveContainer(Identifier.of(ident)) as JavaExecutionContainer<*>)
                    .apiObject.javaClass.protectionDomain.codeSource
            if (jar.location.toString().endsWith(".jar") && !JarLoaderAgent.JAR_DEPENDENCIES.contains(jar.location)) {
                JarLoaderAgent.JAR_DEPENDENCIES.add(jar.location)
                val path = Paths.get(jar.location.toURI())

                // process manifest data if available
                val manifestResources = (runtimeDefinitionRegistry.retrieveContainer(Identifier.of(ident)) as JavaExecutionContainer<*>)
                        .apiObject.javaClass.classLoader.getResources("META-INF/MANIFEST.MF")
                for (manifestResource in manifestResources) {
                    val manifest = Manifest(manifestResource.openStream())

                    if (manifest.mainAttributes.getValue("componentScanPackages") != null) {
                        val packages = manifest.mainAttributes.getValue("componentScanPackages").split(";")
                        packages.forEach {
                            componentScanPackages.add(it.trim())
                        }
                    }

                    if (manifest.mainAttributes.getValue("additionalAnnotations") != null) {
                        val annotations = manifest.mainAttributes.getValue("additionalAnnotations").split(";")
                        annotations.forEach {
                            additionalAnnotations.add(it.trim())
                        }
                    }
                }
                // add the jar as maven dependency
                addDependency("degree.extension", path.fileName.toString(), "1.0.0", "system", path.toString())
            }
        }
    }

    /**
     * Add an activity instance and its definition to the elements that will be exported to the generated app.
     * Also add the jar containing its implementation to the generated app.
     *
     * @param actInstIdent identifier of the activity instance to add
     */
    fun addActivityInstanceToExport(actInstIdent: Identifier) {
        val identifier: Identifier
        var activityInstance = runtimeDefinitionRegistry.lookup(actInstIdent)
        if (activityInstance is Activity) {
            identifier = Identifier.of("shadow_${
                if (actInstIdent.isQualified) {
                    actInstIdent.toString()
                } else {
                    "core." + actInstIdent.toString()
                }
            }")
            activityInstance = runtimeDefinitionRegistry.lookup(identifier)
        } else {
            identifier = actInstIdent
        }

        val activityDef = (activityInstance as ActivityInstance).definition.lookup()

        addElementToExportSystem(Identifier.of(activityDef.name.read()))
        addElementToExportSystem(identifier)
        addJarDependency(activityDef.name.read())
    }

    /**
     * Add an element (activity or policy; definition or instance) with given identifier to set of types that will be
     * exported into the generated data app.
     *
     * @param ident identifier of the element to add
     */
    private fun addElementToExportSystem(ident: Identifier) {
        // prevent duplicates
        if (exportRuntimeDefinitionRegistry.contains(ident))
            return

        val element = runtimeDefinitionRegistry.lookup(ident)
        exportRuntimeDefinitionRegistry.write(ident, element)
    }

    /**
     * Add a type with given identifier to set of types that will be exported into the generated data app.
     *
     * @param ident identifier of the type to add
     */
    fun addTypeToExportSystem(ident: Identifier) {
        // prevent duplicates
        if (exportTypeSystem.contains(ident))
            return

        val type = try {
            typeTaxonomy.lookup(ident)
        } catch (e: TypeSystemException) {
            DegreeTypeSystem.get().lookup(ident)
        } // TODO as soon as deploying language extensions is finally working, we can use imports instead of this cascade

        // process all supertypes
        type.supertypes.forEach {
            addTypeToExportSystem(it.identifier)
        }

        // add the type itself
        exportTypeSystem.register(type)

        // check all attributes
        if (type.isComposite) {
            (type as CompositeType).attributes.map { addTypeToExportSystem(it.value.type.identifier) }
        }
    }

    /**
     * Get all filepaths of files which provide elements to the compiler.
     * This will (at least) read the files within the D° user directory
     *
     * @return Set of all source file filepaths which are relevant for the compiler
     */
    private fun getSourceFiles(): Set<String> {
        return compilerConfiguration.sourceRootDirs
                .map { getSourceFilesForSourceRootDir(it) }
                .flatten()
                .toSet()
    }

    /**
     * Get all filepaths of files within the Data App's directory.
     *
     * @return Set of all source file filepaths which are placed within the Data App's directory
     */
    private fun getDataAppSourceFiles(): Set<String> {
        return getSourceFilesForSourceRootDir(compilerConfiguration.dataAppSourceRootDir)
                .toSet()
    }

    /**
     * Get all files which are relevant for D° from a given folder.
     * The files are filtered by their extensions.
     *
     * @param sourceRootDir Directory which will be scanned
     * @return Set of filepaths to all files within the given directory
     */
    private fun getSourceFilesForSourceRootDir(sourceRootDir: String): Set<String> {
        val scanner = DirectoryScanner()
        scanner.setBasedir(sourceRootDir)

        if (compilerConfiguration.includes.isNotEmpty()) {
            scanner.setIncludes(compilerConfiguration.includes.toTypedArray())
        } else {
            scanner.setIncludes(arrayOf("**/*.degree", "**/*.yaml", "**/*.applicationProperties"))
        }
        scanner.setExcludes(compilerConfiguration.excludes.toTypedArray())
        scanner.scan()

        return scanner.includedFiles.map { File(sourceRootDir, it).path }.toSet()
    }

    /**
     * Find all classes which are annotated either with D°-Activity annotations or D°-Policy annotations.
     * The whole classpath is searched for these files.
     *
     * @return Returns a hashMap which has one entry for each searched annotation. The values of these hashMaps are
     * also hashMaps which contain entries of the form (annotation.qualified_name, instance).
     */
    private fun findAnnotatedClasses() : HashMap<String, HashMap<String, *>> {
        val annotatedActivities = HashMap<String, ActivityApi>()
        val annotatedPolicies = HashMap<String, EmbeddedPolicyApi>()
        val classGraph = ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .ignoreParentClassLoaders()
                .acceptPackages("*")
        for (classLoader in JarLoaderAgent.CLASS_LOADERS)
            classGraph.addClassLoader(classLoader)
        val scanResult = classGraph.scan()
        // the fully qualified name as string is used here instead of ActivityAnnotation::class.java.canonicalName because the
        // API documentation states problems with different class loaders that way
        // get activities
        scanResult.getClassesWithAnnotation("de.fhg.isst.oe270.degree.activities.annotations.ActivityAnnotation")
                .loadClasses().map { it ->
            val activityName = (it.annotations.find { it is ActivityAnnotation } as ActivityAnnotation).qualifiedName
            annotatedActivities[activityName] = it.kotlin.createInstance() as ActivityApi
        }
        // get policies
        scanResult.getClassesWithAnnotation("de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation")
                .loadClasses().map { it ->
            val policyName = (it.annotations.find { it is PolicyAnnotation } as PolicyAnnotation).qualifiedName
            annotatedPolicies[policyName] = it.kotlin.createInstance() as EmbeddedPolicyApi
        }

        val result = HashMap<String, HashMap<String, *>> ()
        result["annotatedActivities"] = annotatedActivities
        result["annotatedPolicies"] = annotatedPolicies

        return result
    }

    /**
     * Prepares the type system for usage.
     *
     * Also presents information about loaded elements to the user.
     */
    private fun setupTypeTaxonomy() {
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded ${TypeTaxonomy.getInstance().size()} data type definitions in total."))
    }

    /**
     * Prepares the activity subsystem for usage.
     *
     * Given a map of classes which are annotated with D° Activity annotations, they will be mapped to the
     * corresponding definitions.
     *
     * Also presents information about loaded elements to the user.
     *
     * @param annotatedActivities Map which contains (name, instance) pairs of annotated java D°-activities
     */
    @Throws(Exception::class)
    private fun setupActivityRegistry(annotatedActivities: HashMap<String, ActivityApi>) {
        val activityRegistry = runtimeDefinitionRegistry.activityRegistry()
        val activityDefinitionCount = activityRegistry.instanceSet(Activity().type).size
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded $activityDefinitionCount activity definitions in total."))

        // find all java activities which are annotated with @Annotation
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Found ${annotatedActivities.size} annotated java activities in total."))

        // create "shadow" activity instances from definitions
        val activityDefinitions: Set<Instance> = runtimeDefinitionRegistry.instanceSet(Identifier.of("degree.Activity"))
        for (instance in activityDefinitions) {
            val activity = instance as Activity
            val activityInstance = ActivityInstance()
            val name = de.fhg.isst.degree.types.gen.core.Identifier()
            name.write(activity.name.read())
            activityInstance[Identifier.of("name")] = name
            val definition = activityInstance.definition
            definition.write(activity.identity.linkValue())
            activityInstance[Identifier.of("definition")] = definition

            val shadowIdentifier = Identifier.of("shadow_" +
                    if (activity.name.read().contains('.'))
                        activity.name.read()
                    else
                        "core." + activity.name.read())
            runtimeDefinitionRegistry.create(shadowIdentifier, activityInstance)

            if (activity.executionContainer.read() == "") {
                runtimeDefinitionRegistry.registerContainer(shadowIdentifier, EmbeddedExecutionContainer())
            }
        }

        // map implemented java activities to definitions
        var mappedActivities = 0
        annotatedActivities.forEach {
            val instanceName = Identifier.of(it.key)
            // check if there is an activity definition within the scope which matches the current annotation
            if (runtimeDefinitionRegistry.contains(instanceName)) {
                runtimeDefinitionRegistry.registerContainer(instanceName, JavaExecutionContainer(it.value))
                mappedActivities++
            }
            else {
                compilerMessages.add(CompilerMessage(CompilerMessage.Kind.WARNING,
                        "Could not find activity definition for annotated java activity $instanceName."))
            }
        }
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Mapped $mappedActivities of ${annotatedActivities.size} annotated java activities to activity definitions."))

        // search for D°-Activities which are defined by D°-code
        var embeddedActivities = 0
        (activityRegistry.instanceSet(Activity().type)).forEach {
            if ((it as Activity).codeBlock != null && it.codeBlock.read().isNotBlank()) {
                runtimeDefinitionRegistry.registerContainer(Identifier.of(it.name.read()), EmbeddedExecutionContainer())
                embeddedActivities++
            }
        }
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "$embeddedActivities of ${activityRegistry.instanceSet(Activity().type).size} activity definitions are purely defined within Degree."))
    }

    /**
     * Prepares the policy subsystem for usage.
     *
     * Given a map of classes which are annotated with D° Policy/Constraint annotations, they will be mapped to the
     * corresponding definitions.
     *
     * Also presents information about loaded elements to the user.
     *
     * @param annotatedPolicies Map which contains (name, instance) pairs of annotated java D°-policies/constraints
     */
    @Throws(Exception::class)
    fun setupPolicyRegistry(annotatedPolicies: HashMap<String, EmbeddedPolicyApi>) {
        val policyRegistry = runtimeDefinitionRegistry.policyRegistry()
        val policyCount = policyRegistry.instanceSet(Policy().type).size
        val constraintCount = policyRegistry.instanceSet(Constraint().type).size

        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded ${policyCount + constraintCount} policy entities in total. " +
                        "They contain $constraintCount constraints and " +
                        "$policyCount policies."))

        // find all "embedded" policies which are annotated with @Annotation
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Found ${annotatedPolicies.size} annotated java policies & constraints in total."))

        // map implemented java policies to definitions
        var mappedPolicies = 0
        annotatedPolicies.forEach {
            val instanceName = Identifier.of(it.key)
            if (policyRegistry.contains(instanceName)) {
                runtimeDefinitionRegistry.registerContainer(instanceName, JavaExecutionContainer(it.value))
                mappedPolicies++
            } else {
                compilerMessages.add(CompilerMessage(CompilerMessage.Kind.WARNING,
                        "Could not find policy definition for annotated java policy $instanceName."))
            }
        }
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Mapped $mappedPolicies of ${annotatedPolicies.size} annotated java policies and constraint to policy definitions."))
    }

    /**
     * Prepares the activity-, constraint-, and policy-instance subsystem for usage.
     *
     * Also presents information about loaded elements to the user.
     */
    @Throws(Exception::class)
    private fun setupInstanceRegistries() {
        val policyInstanceCount = runtimeDefinitionRegistry.policyRegistry()
                .instanceSet(PolicyInstance().type).size
        val constraintInstanceCount = runtimeDefinitionRegistry.policyRegistry()
                .instanceSet(ConstraintInstance().type).size
        val activityInstanceCount = runtimeDefinitionRegistry.activityRegistry()
                .instanceSet(ActivityInstance().type).size
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded ${policyInstanceCount + constraintInstanceCount} constraint & policy instances in total. " +
                        "They contain $constraintInstanceCount constraints and " +
                        "$policyInstanceCount policies."))
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded $activityInstanceCount activity instances in total."))
    }

    /**
     * This function compiles a Data App.
     * It reads all relevant source files, initializes all sub systems,
     * performs some validations and triggers the actual code generation.
     */
    @Throws(CompilerException::class)
    override fun compile(): CompilerResult {
        SpringContext.setCompileMode(true)
        // load all files relevant for this process
        parseToAST(getSourceFiles().plus(getDataAppSourceFiles()))
        // find all relevant annotated classes
        val annotatedClasses = findAnnotatedClasses()
        try {
            // setup extensible systems
            initSubSystems(annotatedClasses)
        } catch (e: Exception) {
            compilerMessages.add(CompilerMessage(CompilerMessage.Kind.ERROR, "Error during the initialization of subsystems: $e"))
        }
        // prior code generation some checks are performed on the AST
        validateAST()
        // if there were no errors until now we can generate the code
        if (compilerMessages.none { it.kind == CompilerMessage.Kind.ERROR }) {
            generateCode()
        } else {
            compilerMessages.add(CompilerMessage(CompilerMessage.Kind.ERROR, "Source files contain error(s). Cannot generate code"))
        }
        // if docker integration is enables we need to create a container on the specified docker host
        //if (compilerConfiguration.dockerEnable) {
            // TODO
        //}

        return CompilerResult(compilerMessages.any { it.kind == CompilerMessage.Kind.ERROR }, compilerMessages)
    }

    /**
     * Initialize all subsystems and load the available elements.
     */
    @Suppress("unchecked_cast")
    @Throws(CompilerException::class)
    fun initSubSystems(annotatedClasses: HashMap<String, HashMap<String, *>>) {
        setupTypeTaxonomy()
        setupActivityRegistry(annotatedClasses["annotatedActivities"] as HashMap<String, ActivityApi>)
        setupPolicyRegistry(annotatedClasses["annotatedPolicies"] as HashMap<String, EmbeddedPolicyApi>)
        setupInstanceRegistries()
    }

    /**
     * Load and process a set of source files.
     * Subsystem files will be loaded into the corresponding systems while
     * D° source code will be compiled.
     *
     * @param sourceFiles Set of filepaths which will be processed
     */
    @Throws(CompilerException::class)
    fun parseToAST(sourceFiles: Set<String>) {
        if (sourceFiles.isEmpty()) {
            compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.WARNING,
                            "Nothing to compile")
            )
            return
        }

        // group sources by file extension
        sourceFiles.groupBy { File(it).extension.toLowerCase() }
                // map source files to File objects
                .mapValues { it -> it.value.map { File(it) } }
                // parse files to ASTs
                .forEach { entry ->
                    when(entry.key) {
                        Configuration.SUBSYSTEM_FILE_EXTENSION.toLowerCase() -> parseSubsystemFiles(entry.value)
                        Configuration.DATA_APP_FILE_EXTENSION.toLowerCase() -> parseDataAppFiles(entry.value)
                        Configuration.PROPERTIES_FILE_EXTENSION.toLowerCase() -> {
                            parsePropertiesFiles(entry.value)
                            compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                                    "Loaded ${compilerConfiguration.applicationProperties.size} application properties in total."))
                        }
                        else -> entry.value.forEach { file ->
                            compilerMessages.add(CompilerMessage(CompilerMessage.Kind.WARNING,
                                    "Ignored because of unknown file extension (" + file.extension + ")",
                                    file.canonicalPath))
                        }
                    }
                }
    }

    /**
     * Load an arbitrary amount of files which contain properties for an D° application.
     *
     * @param sourceFiles List of files which will be loaded and injected into the compiled application
     */
    private fun parsePropertiesFiles(sourceFiles: List<File>) {
        sourceFiles.forEach{
            val properties = Properties()
            properties.load(it.inputStream())

            properties.forEach { key, value ->
                if (compilerConfiguration.applicationProperties.containsKey(key)) {
                    compilerMessages.add(CompilerMessage(CompilerMessage.Kind.WARNING,
                            "Application property with key '$key' is already set with value " +
                                    "${compilerConfiguration.applicationProperties[key]}. Given value '$value' by file ${it.name}" +
                                    " will be ignored.",
                            it.name))
                } else {
                    compilerConfiguration.applicationProperties[key] = value
                }
            }

            logger.info("Loaded ${properties.size} application properties from file '${it.name}'.")
        }
    }

    /**
     * Load an arbitrary amount of files which contain definitions of D° elements.
     *
     * @param sourceFiles List of files which will be loaded into the D° subsystems
     */
    private fun parseSubsystemFiles(sourceFiles: List<File>) {
        sourceFiles.forEach {
            // nukleus policies
            if (it.name == "nukleus.policies.yaml") {
                // mark this file for injection into the data app
                compilerConfiguration.nukleusPoliciesResource = it
            }
            // type files
            else if (it.nameWithoutExtension.endsWith(Configuration.TYPES_IDENTIFIER)) {
                val fileName = it.toString().replace("\\", File.separator).replace("/", File.separator).split(File.separator).last()
                if (!TypeTaxonomy.isFileLoaded(fileName)) {
                    val loadedElements = typeTaxonomy.size()
                    typeTaxonomy.load(it.toPath())
                    TypeTaxonomy.addLoadedFile(fileName)
                    compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                            "Loaded ${TypeTaxonomy.getInstance().size() - loadedElements} data type definitions " +
                                    "from file '${
                                        it.toPath().toString().replace("\\", File.separator)
                                                .replace("/", File.separator)
                                    }'."))
                }
            }
            // language element files
            else if (it.nameWithoutExtension.endsWith(Configuration.SUBSYSTEM_IDENTIFIER)) {
                val fileName = it.toString().replace("\\", File.separator).replace("/", File.separator).split(File.separator).last()
                if (!RuntimeDefinitionRegistry.isFileLoaded(fileName)) {
                    val sizes = getCategorizedRuntimeRegistrySizes()

                    runtimeDefinitionRegistry.load(Format.yaml.parse(it.toPath()))
                    RuntimeDefinitionRegistry.addLoadedFile(fileName)

                    createCompilerMessagesRuntimeDefinitionFileLoaded(sizes, it.toPath().toString()
                            .replace("\\", File.separator).replace("/", File.separator))
                }
            }
        }

        // load types from yaml-files inside jars
        compilerConfiguration.typeYamlResources.forEach { (classLoader, files) ->
            files.forEach { file ->
                val fileName = file.replace("\\", File.separator).replace("/", File.separator).split(File.separator).last()
                if (!TypeTaxonomy.isFileLoaded(fileName)) {
                    val loadedElements = typeTaxonomy.size()

                    typeTaxonomy.deserialize(
                            Format.yaml.parse(
                                    Scanner(classLoader.getResourceAsStream(file)!!).useDelimiter("\\A").next()
                            ))
                    TypeTaxonomy.addLoadedFile(fileName)
                    compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                            "Loaded ${typeTaxonomy.size() - loadedElements} data type definitions " +
                                    "from file '${
                                        (classLoader as URLClassLoader).urLs[0].toString()
                                                .replace("\\", File.separator).replace("/", File.separator)
                                                .drop(6) + "!" + File.separator + fileName
                                    }'."))
                }
            }
        }

        // load registry elements from yaml-files inside jars
        compilerConfiguration.registryYamlResources.forEach { (classLoader, files) ->
            files.forEach { file ->
                val fileName = file.replace("\\", File.separator).replace("/", File.separator).split(File.separator).last()
                if (!RuntimeDefinitionRegistry.isFileLoaded(fileName)) {
                    val sizes = getCategorizedRuntimeRegistrySizes()

                    runtimeDefinitionRegistry.load(
                            Format.yaml.parse(
                                    Scanner(classLoader.getResourceAsStream(file)!!).useDelimiter("\\A").next()
                            ))
                    RuntimeDefinitionRegistry.addLoadedFile(fileName)

                    createCompilerMessagesRuntimeDefinitionFileLoaded(sizes, (
                            classLoader as URLClassLoader).urLs[0].toString().replace("\\", File.separator)
                            .replace("/", File.separator).drop(6) + "!" + File.separator + fileName)
                }
            }
        }

    }

    /**
     * Creates compiler messages which show the size differences of different element groups in the
     * runtime definition registry, after a file was loaded.
     *
     * The expected order of elements is defined as follows:
     * [0] - Number of activity definitions
     * [1] - Number of activity instances
     * [2] - Number of policy definitions
     * [3] - Number of policy instances
     * [4] - Number of constraint definitions
     * [5] - Number of constraint instances
     * [6] - Number of elements in total
     */
    private fun createCompilerMessagesRuntimeDefinitionFileLoaded(oldSizes: IntArray, fileName: String) {
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded ${runtimeDefinitionRegistry.size() - oldSizes[6]} language elements from file '$fileName'."))
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded activity definitions: ${
                    runtimeDefinitionRegistry.activityRegistry()
                            .instanceSet(Activity().type).size - oldSizes[0]
                }."))
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded activity instances: ${
                    runtimeDefinitionRegistry.activityRegistry()
                            .instanceSet(ActivityInstance().type).size - oldSizes[1]
                }."))
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded constraint definitions: ${
                    runtimeDefinitionRegistry.policyRegistry()
                            .instanceSet(Constraint().type).size - oldSizes[4]
                }."))
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded constraint instances: ${
                    runtimeDefinitionRegistry.policyRegistry()
                            .instanceSet(ConstraintInstance().type).size - oldSizes[5]
                }."))
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded policy definitions: ${
                    runtimeDefinitionRegistry.policyRegistry()
                            .instanceSet(Policy().type).size - oldSizes[2]
                }."))
        compilerMessages.add(CompilerMessage(CompilerMessage.Kind.INFO,
                "Loaded policy instances: ${
                    runtimeDefinitionRegistry.policyRegistry()
                            .instanceSet(PolicyInstance().type).size - oldSizes[3]
                }."))
    }

    /**
     * Helper to get the number of different elements in the runtimeDefinitionRegistry.
     * The order of elements is defined as follows:
     * [0] - Number of activity definitions
     * [1] - Number of activity instances
     * [2] - Number of policy definitions
     * [3] - Number of policy instances
     * [4] - Number of constraint definitions
     * [5] - Number of constraint instances
     * [6] - Number of elements in total
     *
     * @return an IntArray that contains all relevant sizes in a defined order
     */
    private fun getCategorizedRuntimeRegistrySizes(): IntArray {
        return intArrayOf(
                runtimeDefinitionRegistry.activityRegistry().instanceSet(Activity().type).size,
                runtimeDefinitionRegistry.activityRegistry().instanceSet(ActivityInstance().type).size,
                runtimeDefinitionRegistry.policyRegistry().instanceSet(Policy().type).size,
                runtimeDefinitionRegistry.policyRegistry().instanceSet(PolicyInstance().type).size,
                runtimeDefinitionRegistry.policyRegistry().instanceSet(Constraint().type).size,
                runtimeDefinitionRegistry.policyRegistry().instanceSet(ConstraintInstance().type).size,
                runtimeDefinitionRegistry.size()
        )
    }

    /**
     * Retrieve a type with given identifier from scope's type system.
     *
     * @param scope Scope which type system will be used
     * @param ident The identifier which will be transformed
     * @return Type which is identified by ident
     */
    protected open fun transformIdentifierToType(scope: Scope, ident: Identifier): Type {
        return scope.typeSystem.lookup(ident)
    }

    /**
     * Retrieve a type with given identifier from scope's type system.
     *
     * @param scope Scope which type system will be used
     * @param ident The identifier which will be transformed
     * @return Type which is identified by ident
     */
    protected open fun transformIdentifierToType(scope: Scope, ident: String): Type {
        return transformIdentifierToType(scope, Identifier.of(ident))
    }

    /**
     * Take a list of D° source code files, parse them and create the AST which will be compiled.
     *
     * The execution aborts if there isn't exactly one file with D° source code.
     *
     * @param sourceFiles List of files which contain data apps and will be parsed by this function
     */
    private fun parseDataAppFiles(sourceFiles: List<File>) {
        // sanity check the input
        if (sourceFiles.size > 1) {
            compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.ERROR,
                            "Found multiple Data Apps in this project.",
                            sourceFiles.joinToString { it.name })
            )
            return }
        if (sourceFiles.isEmpty()) {
            compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.ERROR,
                            "Found no Data App in this project.")
            )
            return
        }
        // parse file
        val parsedDataApp = DegreeParserFacade.parseDataApp(sourceFiles[0])
        // save errors
        parsedDataApp.errors.forEach{compilerMessages.add(CompilerMessage(it))}
        // retrieve the data app
        if (parsedDataApp.root != null) {
            dataApp = parsedDataApp.root
        } else {
            compilerMessages.add(
                    CompilerMessage(CompilerMessage.Kind.ERROR,
                            "Could not parse Data App.",
                            sourceFiles[0].name)
            )
            return
        }
    }

    /**
     * Do some validation and transformation on the AST.
     *
     * This includes:
     * - Validation that startup policies are closed.
     */
    @Throws(CompilerException::class)
    fun validateAST() {
        // validation that startup policies are closed
        val policyRegistry = runtimeDefinitionRegistry.policyRegistry()
        //val policyInstanceRegistry = PolicyInstanceRegistry.getInstance()
        val policies = HashMap<String, Instance>()
        val startupPolicies = dataApp!!.configurationItems[CliDataApp.STARTUP_POLICIES_KEY] ?: ""

        // find all policies within the registry
        if (startupPolicies.isNotEmpty()) {
            startupPolicies.split(",").map{it.trim()}.forEach { policy ->
                val identifier = Identifier.of(policy)
                val policyType = policyRegistry.declaredType(identifier)

                if (!(policyRegistry.contains(identifier) &&
                                (policyType == Identifier.of("degree.PolicyInstance") ||
                                 policyType == Identifier.of("degree.ConstraintInstance"))
                                )) {
                    compilerMessages.add(CompilerMessage(CompilerMessage.Kind.ERROR,
                            "Required startup policy '$policy' is not known to the compiler."))
                } else {
                    policies[policy] = policyRegistry.read(Identifier.of(policy))
                }
            }
        }

        // this map will hold nested policy entities which are part of policy instances
        val nestedPolicies = HashMap<String, Instance>()

        // iterate over all relevant policies (root and nested)
        while (policies.isNotEmpty()) {
            policies.map { entry ->
                val value = entry.value
                if (!when (value) {
                            is PolicyInstance -> value.isClosed()
                            is ConstraintInstance -> value.isClosed()
                            else -> false
                        }) {
                    compilerMessages.add(CompilerMessage(CompilerMessage.Kind.ERROR,
                            "Startup policy '${entry.key}' has unbound attributes."))
                }
                // check for nested values
                if (value is PolicyInstance) {
                    value.mappedElements.split().map { nestedEntry ->
                        nestedPolicies["${entry.key}->${nestedEntry.key}"] = nestedEntry.value
                    }
                }
            }
            // progress to next level of policies
            policies.clear()
            policies.putAll(nestedPolicies)
            nestedPolicies.clear()
        }
    }

    abstract fun generateCode()

    abstract fun addDependency(groupId: String, artifactId: String, version: String, scope: String = "compile", systemPath: String? = null)
}

/**
 * Determines if a PolicyInstance is closed.
 * A PolicyInstance is only closed if all contained elements (policies and constraints)
 * are closed themselves.
 *
 * Because of the new type system it was necessary to move this function here.
 *
 * It is a extension function for now but that may change in a future version.
 *
 * @return true if the PolicyInstance is closed, false otherwise
 */
fun PolicyInstance.isClosed(): Boolean {
    return mappedElements.size() == definition.lookup().dependencies.size() &&
            !mappedElements.split().map {
                when (val value = it.value.lookup()) {
                    is PolicyInstance -> value.isClosed()
                    is ConstraintInstance -> value.isClosed()
                    else -> false
                }
            }.contains(false)
}

/**
 * Determines if a ConstraintInstance is closed.
 * A ConstraintInstance is only closed if all contained parameters are bound to values.
 * Startup policies must be closed 'by themselves' while constraints which are bound to activities
 * can use the parameters of the activity to finally get closed.
 *
 * Because of the new type system it was necessary to move this function here.
 *
 * It is a extension function for now but that may change in a future version.
 *
 * @return true if the ConstraintInstance is closed, false otherwise
 */
fun ConstraintInstance.isClosed(): Boolean {
    return mappedElements.size() == definition.lookup().attribute.size()
}

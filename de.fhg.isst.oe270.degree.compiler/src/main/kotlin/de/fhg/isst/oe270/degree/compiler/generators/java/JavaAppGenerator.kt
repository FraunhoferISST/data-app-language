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
package de.fhg.isst.oe270.degree.compiler.generators.java

import de.fhg.isst.degree.types.gen.degree.*
import de.fhg.isst.degree.types.gen.degree.Activity
import de.fhg.isst.oe270.degree.activities.execution.InputScope
import de.fhg.isst.oe270.degree.activities.execution.OutputScope
import de.fhg.isst.oe270.degree.compiler.AbstractDegreeCompiler
import de.fhg.isst.oe270.degree.compiler.CompilerConfiguration
import de.fhg.isst.oe270.degree.compiler.CompilerMessage
import de.fhg.isst.oe270.degree.compiler.DegreeToJavaCompiler
import de.fhg.isst.oe270.degree.compiler.generators.DataAppTypes
import de.fhg.isst.oe270.degree.compiler.generators.ExecutionTypes
import de.fhg.isst.oe270.degree.grammar.DegreeParserFacade
import de.fhg.isst.oe270.degree.grammar.ast.model.*
import de.fhg.isst.oe270.degree.grammar.ast.model.Block
import de.fhg.isst.oe270.degree.grammar.ast.model.BooleanLiteral
import de.fhg.isst.oe270.degree.grammar.ast.model.DataApp
import de.fhg.isst.oe270.degree.grammar.ast.model.Expression
import de.fhg.isst.oe270.degree.grammar.ast.model.FieldAccess
import de.fhg.isst.oe270.degree.grammar.ast.model.Statement
import de.fhg.isst.oe270.degree.grammar.ast.model.StringLiteral
import de.fhg.isst.oe270.degree.parsing.grammar.interfaces.Node
import de.fhg.isst.oe270.degree.parsing.types.QualifiedName
import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadOnlyEntity
import de.fhg.isst.oe270.degree.runtime.java.data.app.*
import de.fhg.isst.oe270.degree.runtime.java.data.app.execution.Executor
import de.fhg.isst.oe270.degree.runtime.java.exceptions.DegreeException
import de.fhg.isst.oe270.degree.runtime.java.exceptions.parameters.DegreeMissingInputException
import de.fhg.isst.oe270.degree.runtime.java.exceptions.policies.DegreePolicyValidationException
import de.fhg.isst.oe270.degree.runtime.java.manager.VariableManager
import de.fhg.isst.oe270.degree.runtime.java.usage.control.`object`.UsageControlObject
import de.fhg.isst.oe270.degree.types.RuntimeDefinitionRegistry
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import net.sourceforge.jenesis4java.*
import net.sourceforge.jenesis4java.Type
import net.sourceforge.jenesis4java.impl.MCodeWriter
import nukleus.core.*
import nukleus.core.PrimitiveType
import org.apache.commons.lang.StringEscapeUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.PropertySource
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.*
import javax.annotation.PostConstruct
import kotlin.collections.HashMap
import kotlin.collections.HashSet


/**
 * Main class of D°-Compiler regarding the generation of java code.
 * Entry point of code generation is {@link generateDataApp()}.
 * Creates full java application from D°-AST.
 */

class JavaAppGenerator(
    private val generatedCodeDir: String,
    private val compilerConfiguration: CompilerConfiguration,
    private val compilerMessages: MutableList<CompilerMessage>,
    private val dataApp: DataApp,
    val compiler: DegreeToJavaCompiler
) {

    /**
     * this imports are needed in every data app
     */
    private val imports = mutableSetOf<String>(
        UUID::class.java.canonicalName,
        HashMap::class.java.canonicalName,
        SpringApplication::class.java.canonicalName,
        ApplicationContext::class.java.canonicalName,
        InputScope::class.java.canonicalName,
        OutputScope::class.java.canonicalName,
        ActivityInstance::class.java.canonicalName,
        Instance::class.java.canonicalName,
        Identifier::class.java.canonicalName,
        VariableManager::class.java.canonicalName,
        DegreePolicyValidationException::class.java.canonicalName,
        DegreeMissingInputException::class.java.canonicalName,
        ExecutionContext::class.java.canonicalName,
        ReadOnlyEntity::class.java.canonicalName,
        InterruptedException::class.java.canonicalName,
        Executor::class.java.canonicalName,
        PostConstruct::class.java.canonicalName,
        DependsOn::class.java.canonicalName,
        PropertySource::class.java.canonicalName
    )

    private val thrownExceptions = listOf<String>(
        DegreePolicyValidationException::class.java.simpleName,
        DegreeMissingInputException::class.java.simpleName
    )

    private val LOGGER = LoggerFactory.getLogger("JavaAppGenerator")

    /**
     * The extensible type taxonomy.
     */
    private val typeTaxonomy = TypeTaxonomy.getInstance()

    private val runtimeDefinitionRegistry = RuntimeDefinitionRegistry.getInstance()

    /**
     * The activity registry subsystem of runtime definition registry.
     */
    private val activityRegistry = runtimeDefinitionRegistry.activityRegistry()

    /**
     * The virtual machine is the base object for code generation.
     */
    private val virtualMachine = VirtualMachine.getVirtualMachine()

    /**
     * Name of the Data App that will be generated.
     */
    private val dataAppName = dataApp.configurationItems[CliDataApp.NAME_KEY]

    /**
     * Version of the data app.
     */
    private val dataAppVersion = dataApp.configurationItems[CliDataApp.VERSION_KEY]

    /**
     * (Java) namespace of the Data App that will be generated.
     */
    private val dataAppNamespace = dataApp.configurationItems[CliDataApp.NAMESPACE_KEY]

    /**
     * Inside the generated Data App the type taxonomy will be stored in a variable with this name.
     */
    private val typeTaxonomyVar = "TYPE_TAXONOMY"

    /**
     * Inside the generated Data App the (String, ActivityAPI)-Activity map will be stored in a variable with this name.
     */
    private val activityRegistryVar = "ACTIVITY_INSTANCE_REGISTRY"

    /**
     * Inside the generated Data App the Sandbox will be used as execution environment for all Activities.
     */
    private val sandboxVar = "SANDBOX"

    /**
     * Inside the generated Data App a single Output scope is used to return final results. This is the variable name
     * for that specific OutputScope.
     */
    private val returnOutputScopeVar = "returnOutputScope"

    /**
     * This map contains a mapping between Blocks (D° element) and Methods (generated java element).
     */
    private val blockToMethodMap = HashMap<Block, ClassMethod>()

    /**
     * For each variable which is used in a Data App the unique identifier which is used in the generated
     * java code is stored inside this map.
     */
    private var scopeAwareVariableToUuidMapList = mutableListOf<HashMap<String, UUID>>()

    /**
     * Since activities which are defined within D° (by using D° code) are treated like a data app
     * on its own we need to change between stacks.
     */
    private val scopeAwareVariableToUuidStack = Stack<List<HashMap<String, UUID>>>()

    /**
     * For each variable which is used in a Data App the unique identifier of the type is stored inside this map.
     */
    private var scopeAwareVariableToTypeMapList = mutableListOf<HashMap<String, Identifier>>()

    /**
     * In addition to the information which variables are known to a given scope it is necessary
     * to know which ones exist as valid variables and do not need to be defined prior use.
     */
    private var scopeAwareVariableInstances = mutableListOf<HashSet<String>>()

    /**
     * Since activities which are defined within D° (by using D° code) are treated like a data app
     * on its own we need to change between stacks.
     */
    private val scopeAwareVariableToTypeStack = Stack<List<HashMap<String, Identifier>>>()

    /**
     * Each call to an activity has a corresponding InputScope and a OutputScope. This map contains the mapping
     * ActivityCall->InputScopeIdentifier. The Identifier is used in the generated java code.
     */
    private val activityToInputScopeMap = HashMap<ActivityCall, UUID>()

    /**
     * Each call to an activity has a corresponding InputScope and a OutputScope. This map contains the mapping
     * ActivityCall->OutputScopeIdentifier. The Identifier is used in the generated java code.
     */
    private val activityToOutputScopeMap = HashMap<ActivityCall, UUID>()

    /**
     * This set keeps track of all UUIDs which have been used in the generated Data App.
     */
    private val usedUuids = HashSet<UUID>()

    /**
     * This counter is used to keep track the number of anonymous elements
     */
    private var anonymousCounter = 0

    /**
     * This value is used to determine which kind of data app will be generated.
     * It effects the generated code as well as used base classes.
     */
    public var dataAppType = DataAppTypes.CLI

    /**
     * This value is used to determine which kind of execution behaviour will be generated.
     * It effects the generated code.
     */
    private var executionType = ExecutionTypes.SINGLE

    /**
     * This may hold the main entry point for the compiled data app.
     */
    private var processMethodContainer: net.sourceforge.jenesis4java.Block? = null

    private val generationTime = LocalDateTime.now()

    private fun addScopeAwareLevel() {
        scopeAwareVariableToUuidMapList.add(HashMap())
        scopeAwareVariableToTypeMapList.add(HashMap())
        scopeAwareVariableInstances.add(HashSet())
    }

    private fun removeScopeAwareLevel() {
        scopeAwareVariableToUuidMapList = scopeAwareVariableToUuidMapList.dropLast(1).toMutableList()
        scopeAwareVariableToTypeMapList = scopeAwareVariableToTypeMapList.dropLast(1).toMutableList()
        scopeAwareVariableInstances = scopeAwareVariableInstances.dropLast(1).toMutableList()
    }

    private fun pushScopeAwareStackLevel() {
        scopeAwareVariableToUuidStack.push(scopeAwareVariableToUuidMapList)
        scopeAwareVariableToUuidMapList = mutableListOf()
        scopeAwareVariableToUuidMapList.add(HashMap())
        scopeAwareVariableToTypeStack.push(scopeAwareVariableToTypeMapList)
        scopeAwareVariableToTypeMapList = mutableListOf()
        scopeAwareVariableToTypeMapList.add(HashMap())

        scopeAwareVariableInstances.add(HashSet())
    }

    private fun popScopeAwareStackLevel() {
        scopeAwareVariableToUuidMapList = scopeAwareVariableToUuidStack.pop() as MutableList<HashMap<String, UUID>>
        scopeAwareVariableToTypeMapList =
            scopeAwareVariableToTypeStack.pop() as MutableList<HashMap<String, Identifier>>

        scopeAwareVariableInstances.dropLast(1)
    }

    /**
     * This is the entry point for the generation of java Data Apps.
     * The code below is purely generated while the
     *
     * @return true if the code generation was successful, false otherwise
     * @see de.fhg.isst.oe270.degree.compiler.DegreeToJavaCompiler.generateCode
     */
    fun generateDataApp(): Boolean {
        // before we start the generation, we need to determine which kind of Data App has to be generated
        determineGeneratedDataAppType()
        // we also need to determine the desired execution behaviour
        determineExecutionType()

        generateTypeDependentPom()

        // we need to push our very first variable map to our stacks
        addScopeAwareLevel()

        // the compilation unit is the construct which lies above the class in hierarchy
        val compilationUnit =
            virtualMachine.newCompilationUnit(generatedCodeDir + File.separator + "src" + File.separator + "main" + File.separator + "java")
        compilationUnit.setNamespace(dataAppNamespace)

        // this class holds the Data App
        val dataAppClass = createDataAppClass(compilationUnit)

        // the constructor
        generateDataAppInitMethod(dataAppClass)

        // the main method
        generateSpringMainFunction(dataAppClass)

        // the actual data app logic
        generateDataAppLogicFun(dataAppClass, dataApp)
        // generation of the actual functionality for this data app
        if (!generateDataAppLogic(dataAppClass, dataApp)) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Could not generate code for the Data App.",
                    dataApp.file,
                    dataApp.code.position
                )
            )
            return false
        }
        // final code generation step which can only performed after the logic was generated
        generateScanAnnotations(dataAppClass)
        addAdditionalAnnotations(dataAppClass)
        addImports(dataAppClass, imports.toList()) // set of realy required imports in known by now

        // this statement is only for clearness
        removeScopeAwareLevel()

        // write out java code
        compilationUnit.encode()

        return true
    }

    private fun generateTypeDependentPom() {
        when (dataAppType) {
            DataAppTypes.HTTP,
            DataAppTypes.IDS_HTTP -> {
                // add dependencies for automatic generation of swagger UI and swagger-annotations
                compiler.addDependency("org.springdoc", "springdoc-openapi-ui", "1.5.3")
            }
        }
    }

    private fun addAdditionalAnnotations(dataAppClass: PackageClass) {
        compiler.additionalAnnotations.forEach { qualifiedAnnotation ->
            dataAppClass.addAnnotation(qualifiedAnnotation)
        }
    }

    /**
     * During the generation of the application logic, arbitrary D°-extensions can be used.
     * This extensions may require to scan specific packages for components.
     * The set of packages to scan is only clear after the logic was generated.
     */
    private fun generateScanAnnotations(dataAppClass: PackageClass) {
        if (compiler.componentScanPackages.size > 0) {
            imports.add(ComponentScan::class.java.canonicalName)
            val componentScan = dataAppClass.addAnnotation("ComponentScan")
            componentScan.addAnnotationAttribute(
                "basePackages", virtualMachine.newFree(
                    compiler.componentScanPackages.joinToString(prefix = "{ \"", separator = "\", \"", postfix = "\" }")
                )
            )
        }
    }

    /**
     * The function will determine which kind of Data App has to be generated for the given
     * AST. The decision is based on the configuration part of the data app.
     */
    private fun determineGeneratedDataAppType() {
        // set the default value
        dataAppType = DataAppTypes.CLI
        // check the existence of different configuration items which can change the generated type
        if (dataApp.configurationItems.containsKey(TcpIpDataApp.PORT_KEY)) {
            // we need to create some kind of TcpIpDataApp but since it is abstract we need to check which subtype
            // is matching
            if (dataApp.configurationItems.containsKey(HttpDataApp.URL_KEY)) {
                // we only checked for the url key since a jwt signing token is optional
                // we need to create a HttpDataApp
                dataAppType = DataAppTypes.HTTP
                if (dataApp.configurationItems.containsKey(IdsHttpDataApp.IDS_KEY)) {
                    dataAppType = DataAppTypes.IDS_HTTP
                }
                return
            } else {
                // could not determine the correct Data App type
                LOGGER.error("Ambiguous Data App configuration does not allow to determine the type that needs to be generated.")
                throw IllegalStateException("Could not determine Data App type for generation.")
            }
        }
    }

    private fun determineExecutionType() {
        // sanitize configuration
        if (!dataApp.configurationItems.containsKey(CliDataApp.EXECUTION_BEHAVIOUR_KEY)) {
            throw IllegalStateException("There is no specified execution behaviour for this Data App")
        }
        // the decision is based on a single configuration item
        when (dataApp.configurationItems[CliDataApp.EXECUTION_BEHAVIOUR_KEY]) {
            AbstractDegreeCompiler.SINGLE_EXECUTION_CONFIG_KEY -> {
                executionType = ExecutionTypes.SINGLE
            }
            AbstractDegreeCompiler.PERIODIC_EXECUTION_CONFIG_KEY -> {
                executionType = ExecutionTypes.PERIODIC
                if (!dataApp.configurationItems.containsKey(CliDataApp.PERIODIC_TIME_KEY)) {
                    throw IllegalStateException("Could not determine periodic time for periodic execution behaviour")
                }
                executionType.deltaTime = Integer.parseInt(dataApp.configurationItems[CliDataApp.PERIODIC_TIME_KEY])
            }
            else -> {
                throw IllegalStateException(
                    "Could not determine execution behaviour for given value" +
                            " '${dataApp.configurationItems[CliDataApp.EXECUTION_BEHAVIOUR_KEY]}'."
                )
            }
        }
    }

    /**
     * The generation of app logic code starts here.
     * Will create an (empty) echo-service if the app does not contain any code.
     *
     * @param dataAppClass the class which will contain the app logic
     * @param dataApp D°-AST of data app
     * @return true if the code generation was successful, false otherwise
     */
    private fun generateDataAppLogic(dataAppClass: PackageClass, dataApp: DataApp): Boolean {
        val processMethod = processMethodContainer!!
        // if the code block of the data app is empty just return the input
        if (dataApp.code.statements.isEmpty()) {
            processMethod.newReturn()
                .setExpression(virtualMachine.newVar("new OutputScope(input.getValues()).toJson()"))
                .setComment(
                    Comment.SINGLE_LINE,
                    "The data app does not provide any logic. Creating an \"echo service\" now."
                )
        } else {
            // wrap all in try catch block to catch specific degree exceptions
            val processMethodTry = processMethod.newTry()
            // actual app logic
            generateDataAppStatement(dataAppClass, processMethodTry, dataApp.code, true, true)
            // catch clauses
            val processMethodCatch = generateCatch(processMethodTry, "Because of error(s) the execution was aborted.")
            // we need to create return statements for single execution and log outputs for periodic ones
            when (executionType) {
                ExecutionTypes.SINGLE -> {
                    processMethodCatch.newReturn().expression = virtualMachine.newInvoke("errorOutputScope.toJson")
                    processMethod.newReturn().expression = virtualMachine.newVar("$returnOutputScopeVar.toJson()")
                }
                ExecutionTypes.PERIODIC -> {
                    processMethodCatch.newStmt(
                        virtualMachine.newInvoke("this", "logError").addVariableArg("errorOutputScope.toJson()")
                    )
                    processMethod.newStmt(
                        virtualMachine.newInvoke("this", "logInfo").addVariableArg("$returnOutputScopeVar.toJson()")
                    )
                }
            }
        }
        return true
    }

    /**
     * Checks if a referenced variable is existing. If the variable does not exist a compiler message error is created.
     * This method should only be called if the variable existence is mandatory.
     *
     * @param varName the human readable name of the variable
     * @param parameterType indicator if this variable is an input or output variable. Default value is "input"
     * @return true if the variable is present, false otherwise
     */
    private fun validateVariableExistence(varName: String, parameterType: String = "input"): Boolean {
        scopeAwareVariableToUuidMapList.asReversed().map { variableToUuidMap ->
            if (variableToUuidMap.containsKey(varName)) {
                return true
            }
        }
        compilerMessages.add(
            CompilerMessage(
                CompilerMessage.Kind.ERROR,
                "Unknown $parameterType $varName."
            )
        )//TODO missing file/pos information
        return false
    }

    /**
     * This function generates java code for a given D°-Block. The function will trigger the code generation for all nested statements.
     * Each D°-Block is encapsulated by a private java method. Since Blocks are used for grouping/scoping inside D° the functions will return void
     * and have no input parameters.
     *
     * @param pckClass the class containing all java code
     * @param method the method which will contain the generated code
     * @param block the D°-AST of a block-statement
     * @param hasParent used for scoping. Indicator if the block is a statement inside another block or if it is the root block.
     */
    private fun generateDataAppBlock(
        pckClass: PackageClass,
        method: net.sourceforge.jenesis4java.Block,
        block: Statement,
        hasParent: Boolean
    ) : net.sourceforge.jenesis4java.Expression {
        // generate a unique valid method name
        val blockMethodName = "block_" + uuidToJavaIdentifier(createFreeUuid())
        // generate the function
        val blockMethod = generateDataAppBlockFun(
            pckClass,
            blockMethodName,
            emptyList(),
            virtualMachine.newType(Type.BOOLEAN)!!,
            Access.AccessType.PRIVATE,
            hasParent,
            thrownExceptions
        )
        // store the pair (D°-Block, Java-Method) for later use
        blockToMethodMap[block as Block] = blockMethod
        // generate code for all nested statements inside the block and check if a return statement was found
        var hasReturn = false
        block.statements.forEach {
            generateDataAppStatement(pckClass, blockMethod, it)
            if (it is ReturnStatement)
                hasReturn = true
        }
        // generate a return statement which indicates that the execution should be continued
        if (!hasReturn) {
            blockMethod.newReturn().expression = virtualMachine.newBoolean(false)
        }
        // create call to block method and attach it to the method
        return virtualMachine.newInvoke("this", blockMethodName)
                .addVariableArg("variableManager").addVariableArg("returnOutputScope")

    }

    /**
     * For each call to an activity it is necessary to validate the existence ot the activity, since the grammar allows each valid identifier as activity call.
     *
     * @param activityCall the D°-AST of the checked activity call
     * @return the actual D° Activity if the activity exists, null otherwise
     */
    private fun validateActivityExistence(activityCall: ActivityCall): Activity? {
        // build the correct identifier for activity
        val activityIdentifier =
            Identifier.of(activityCall.activity.name.qualifier + "." + activityCall.activity.name.name)
        // this will hold the result
        var activity: Activity? = null
        // try to get the activity
        try {
            activity = (activityRegistry.read(activityIdentifier) as ActivityInstance?)!!.definition.lookup()
        } catch (e: Exception) {
            // Since there is no matching activity instance, we can try to retrive a matching definition and check if
            // it can be executed
            try {
                activity = activityRegistry.read(activityIdentifier) as Activity
                // we must ensure that the activity provides an executable execution container
                if (activity.executionContainer.read() == "noop") {
                    throw IllegalStateException()
                }
            } catch (e: Exception) {
                // since the activity is not found a compiler message error is generated
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Data app tries to call unknown activity $activityIdentifier.",
                        activityCall.file,
                        activityCall.position
                    )
                )
            }

        }

        return activity
    }

    /**
     * If an activity is called it is necessary to validate the input and output parameters.
     *
     * @param activityCall the D°-AST of the activity call
     * @param activity the actual D°-Activity
     * @see validateInputParameters
     * @see validateOutputParameters
     */
    private fun validateActivityCallParameters(activityCall: ActivityCall, activity: Activity, name: String?) {
        // inputs
        val inputParameters = activity.inputParameters?.split()
        if (!validateInputParameters(activityCall.inputVariables, inputParameters!!)) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Calling activity ${activity.name.read()} " +
                            if (name == null) "(call to definition)" else "(call to instance: $name)" +
                                    " not possible because of invalid input parameters.",
                    activityCall.file,
                    activityCall.position
                )
            )
        }
        // outputs
        val outputParameters = activity.outputParameters?.split()
        if (!validateOutputParameters(activityCall.outputVariables, outputParameters!!)) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Calling activity ${activity.name.read()} " +
                            if (name == null) "(call to definition)" else "(call to instance: $name)" +
                                    " not possible because of invalid output parameters.",
                    activityCall.file,
                    activityCall.position
                )
            )
        }
    }

    /**
     * Creates a (java) input scope for given activity call matching the corresponding activity definition
     * and appends the java statements to a given method.
     *
     * @param activityCall D°-AST for an activity call
     * @param activity the definition of a D°-activity
     * @param method the method which contains the activity call
     * @return the variable name of the new input scope
     */
    private fun generateInputScopeForActivityCall(
        activityCall: ActivityCall,
        activity: Activity,
        method: net.sourceforge.jenesis4java.Block
    ): String {
        // generate variable uuid & name
        val inputScopeUuid = createFreeUuid()
        val inputScopeVarName = "inputScope_${uuidToJavaIdentifier(inputScopeUuid)}"
        // store information about input scope in (activityCall, uuid) map
        activityToInputScopeMap[activityCall] = inputScopeUuid
        val inputScopeStmt = virtualMachine.newInvoke("InputScope $inputScopeVarName = new InputScope")
        method.newStmt(inputScopeStmt)
        // generate accessing code for each input parameter of the activity call
        generateExpressionAccessForInputScope(activityCall, activity, inputScopeVarName, method)

        return inputScopeVarName
    }

    /**
     * Try to find the a variable's uuid by its name.
     * Traverses the different levels of visibility.
     *
     * @param name the human readable name of the variable
     * @return the uuid of the variable
     */
    private fun retrieveVariableUuidByName(name: String): UUID {
        scopeAwareVariableToUuidMapList.asReversed().map {
            if (it.containsKey(name)) {
                return it[name]!!
            }
        }
        throw IllegalStateException("Tried to retrieve unknown variable $name.")
    }

    /**
     * Try to find the a variable's type by its name.
     * Traverses the different levels of visibility.
     *
     * @param name the human readable name of the variable
     * @return the type of the variable
     */
    private fun retrieveVariableTypeByName(name: String): Identifier {
        scopeAwareVariableToTypeMapList.asReversed().map {
            if (it.containsKey(name)) {
                return it[name]!!
            }
        }
        throw IllegalStateException("Tried to retrieve type of unknown variable $name.")
    }

    /**
     * Determines if a variable exists within the data app by querying the scope aware uuid map list
     *
     * @param name the searched variable
     * @return true if the variable exists, false otherwise
     */
    private fun checkVariableExistsByUuid(name: String): Boolean {
        scopeAwareVariableToUuidMapList.asReversed().map {
            if (it.containsKey(name)) {
                return true
            }
        }

        return false
    }

    /**
     * Determines if a variable is already used within the current scope or if it is necessary to define the instance.
     *
     * @param name the searched variable
     * @return true if the variable exists, false otherwise
     */
    private fun checkVariableIsDefinedInCurrentScope(name: String): Boolean {
        return scopeAwareVariableInstances.last().contains(name)
    }

    /**
     * Determines if a variable exists within the data app by querying the scope aware uuid map list
     *
     * @param name the searched variable
     * @return true if the variable exists, false otherwise
     */
    private fun checkVariableExistsByType(name: String): Boolean {
        scopeAwareVariableToTypeMapList.asReversed().map {
            if (it.containsKey(name)) {
                return true
            }
        }

        return false
    }

    /**
     * This function generates code for each input parameter an activity call has and determines which code needs
     * to be generated, based on the expression type.
     *
     * @param activityCall D°-AST for an activity call
     * @param activity the definition of a D°-activity
     * @param inputScopeVarName the name of the input scope for the activity call
     * @param method the method which contains the activity call
     */
    private fun generateExpressionAccessForInputScope(
        activityCall: ActivityCall,
        activity: Activity,
        inputScopeVarName: String,
        method: net.sourceforge.jenesis4java.Block
    ) {
        activityCall.inputVariables.forEachIndexed { idx, it ->
            run {
                // create statement which adds a new entry to the input scope
                val inputParamStmt = virtualMachine.newInvoke(inputScopeVarName, "add")
                when (it) {
                    is MethodCallExpression -> {
                        // TODO since D°-Activities return multiple values at this point the output of one activity cannot be the direct input of another one
                        // hard skip this param because of missing functionality
                        compilerMessages.add(
                            CompilerMessage(
                                CompilerMessage.Kind.ERROR, "Activity calls are " +
                                        "not supported as input parameters right now."
                            )
                        )
                        return@run
                    }
                    is VariableReference -> {
                        inputParamStmt.addArg(activity.inputParameters.split()[idx].name.read())
                        if (it.index < 0)
                            inputParamStmt.addArg(
                                virtualMachine.newInvoke("variableManager", "readVariable")
                                    .addVariableArg("UUID.fromString(\"${retrieveVariableUuidByName(it.name)}\")")
                            )
                        else
                            inputParamStmt.addArg(
                                virtualMachine.newInvoke("variableManager", "readVariable")
                                    .addVariableArg("UUID.fromString(\"${retrieveVariableUuidByName(it.name)}\").get(${it.index})")
                            )
                    }
                    is StringLiteral -> {
                        // TODO hard skip this param because of missing functionality
                        compilerMessages.add(
                            CompilerMessage(
                                CompilerMessage.Kind.ERROR, "String literals are " +
                                        "not supported as input parameters right now."
                            )
                        )
                        return@run
                    }
                    is FieldAccess -> {
                        inputParamStmt.addArg(activity.inputParameters.split()[idx].name.read())
                        var callChainStmt: net.sourceforge.jenesis4java.Expression =
                            virtualMachine.newInvoke("variableManager", "readVariable")
                                .addVariableArg("UUID.fromString(\"${retrieveVariableUuidByName(it.reference.name)}\")")
                        if (it.reference.index > -1) {
                            callChainStmt = virtualMachine.newInvoke(callChainStmt, "get").addArg(it.reference.index)
                        }
                        if (it.accessedFields.isNotEmpty())
                            callChainStmt =
                                virtualMachine.newCast(virtualMachine.newType("CompositeInstance"), callChainStmt)
                        it.accessedFields.forEachIndexed { idx, field ->
                            callChainStmt = virtualMachine.newInvoke(callChainStmt, "read")
                                .addArg(virtualMachine.newFree("new Identifier(\"$field\").purge()"))
                            if (idx < it.accessedFields.size - 1) {
                                callChainStmt =
                                    virtualMachine.newCast(virtualMachine.newType("CompositeInstance"), callChainStmt)
                            }
                        }
                    }
                    else -> {
                        compilerMessages.add(
                            CompilerMessage(
                                CompilerMessage.Kind.ERROR, "Unknown type of input " +
                                        "parameter for activity call found."
                            )
                        )
                        return@run
                    }
                }
                // attach the new statement to the method
                method.newStmt(inputParamStmt)
            }
        }
    }

    /**
     * Generate an output scope for an activity call.
     *
     * @param activityCall the activity call which needs an output scope
     * @return the variable name of the new output scope
     */
    private fun generateOutputScopeForActivityCall(activityCall: ActivityCall): String {
        // create variable name and store information about the output scope in a (activityCall, uuid) map
        val outputScopeUuid = createFreeUuid()
        activityToOutputScopeMap[activityCall] = outputScopeUuid

        return "outputScope_${uuidToJavaIdentifier(outputScopeUuid)}"
    }

    /**
     * Checks if a given type is compatible to an existing variable with given name.
     *
     * @param variableName name of the variable which should be checked
     * @param variableType type which is checked on compatibility
     * @param node the node which contains the statement. Required for accurate compiler messages
     * @param variableClassifier a classifier for the variable which is used in compiler messages to be more meaningful. Default value is "variable"
     * @return true if the types are compatible, false otherwise
     */
    private fun checkVariableTypeCompatibility(
        variableName: String,
        variableType: Identifier,
        node: Node,
        variableClassifier: String = "variable"
    ): Boolean {
        if (retrieveVariableTypeByName(variableName) != variableType) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Incompatible types for $variableClassifier '$variableName'. Expected " +
                            "'${retrieveVariableTypeByName(variableName)}' but found '$variableType'.",
                    node.file,
                    node.position
                )
            )
            return false
        }

        return true
    }

    /**
     * Validates that a given type is compatible to a D°-parameter definition (e.g. Activity)-
     *
     * @param definition this is the D°-definition of a parameter
     * @param instanceType this is the type which is checked for compatibility
     * @param parameter this is the expression which contains the type (used for compiler messages)
     * @param paramType identifier which determines if the parameter is an input or output parameter. Default value is "input"
     */
    private fun validateParameterType(
        definition: TaggedParameter,
        instanceType: Identifier,
        parameter: Expression,
        paramType: String = "input"
    ): Boolean {
        if (Identifier(definition.type_.read()) != instanceType) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Type mismatch on $paramType parameter ${definition.name.read()}: " +
                            "Found $instanceType but expected ${definition.type_.read()}.",
                    parameter.file,
                    parameter.position
                )
            )
            return false
        }
        return true
    }

    /**
     * Retrieves the uuid for a given variable name. If the variable is unknown a new variable is generated
     * in the compiler and corresponding java code is generated.
     *
     * @param variableName name of the variable which uuid should be retrieved
     * @param variableType type of the new variable
     * @param node the node which contains the variable
     * @param method the java method which will contain the (maybe) generated code
     */
    private fun retrieveUuidForVariable(
        variableName: String,
        variableType: Identifier,
        node: Node,
        method: net.sourceforge.jenesis4java.Block
    ): UUID? {
        var uuid: UUID? = null
        // if the variable is already known a type check is necessary
        if (checkVariableExistsByType(variableName)) {
            if (!checkVariableTypeCompatibility(variableName, variableType, node)) {
                return uuid
            }
            // nothing to-do here since the variable already exists and the types match
            uuid = retrieveVariableUuidByName(variableName)
        } else {
            // since this is ja new variable it needs to be registered
            uuid = createFreeUuid()
            //if this is a new variable it needs to be registered in the variableManager
            method.newStmt(
                virtualMachine.newInvoke("variableManager", "registerVariable")
                    .addVariableArg("UUID.fromString(\"$uuid\")")
            )
        }

        return uuid
    }

    /**
     * This function takes the output scope which is the result of an activity call and stores all parameters which are
     * output of the activity call in the variable manager.
     *
     * @param activityCall the D°-AST of an activity call
     * @param activity D°-Activity definition which describes the called activity
     * @param outputScopeVarName name of the output scope variable which is created by the activity call
     * @param method the method containing the generated code
     */
    private fun generateVariableStorageAfterActivityCall(
        activityCall: ActivityCall,
        activity: Activity,
        outputScopeVarName: String,
        method: net.sourceforge.jenesis4java.Block
    ) {
        activityCall.outputVariables.forEachIndexed { idx, it ->
            val variableName = it.name
            val variableType = Identifier.of(activity.outputParameters.split()[idx].type_.read())
            val uuid: UUID? = retrieveUuidForVariable(variableName, variableType, activityCall, method)
            // write the value
            var setVarFunction = "initializeVariable"
            if (checkVariableExistsByUuid(variableName)) {
                setVarFunction = "updateVariable"
            }
            // initialize the variable in the data app
            method.newStmt(
                virtualMachine.newInvoke("variableManager", setVarFunction)
                    .addVariableArg("UUID.fromString(\"$uuid\")")
                    .addVariableArg("$outputScopeVarName.get(\"${activity.outputParameters.split()[idx].name.read()}\")")
            )
            // store information about the new variable (type and identifier) in (string, Identifier) and (string, uuid) maps
            scopeAwareVariableToTypeMapList.last()[variableName] = variableType!!
            scopeAwareVariableToUuidMapList.last()[variableName] = uuid!!
        }
    }

    /**
     * This function generates code for an activity call. It processes (and checks) input and output scope.
     *
     * @param activityCall D°-AST of the activityCall
     * @param method Java-method which will contain the generated code
     */
    private fun generateDataAppActivityCall(
        pckClass: PackageClass,
        method: net.sourceforge.jenesis4java.Block,
        activityCall: ActivityCall
    ) {
        // resolve activity definition in order to register used types
        val instanceOrDef = runtimeDefinitionRegistry.lookup(Identifier.of(activityCall.activity.name.toString()))
        val activityInstance: ActivityInstance =
            if (runtimeDefinitionRegistry.lookup(Identifier.of(activityCall.activity.name.toString())) is ActivityInstance) {
                instanceOrDef as ActivityInstance
            } else {
                val shadowIdentifier = Identifier.of(
                    "shadow_" +
                            if (activityCall.activity.name.toString().contains('.'))
                                activityCall.activity.name.toString()
                            else
                                "core." + activityCall.activity.name.toString()
                )
                runtimeDefinitionRegistry.lookup(shadowIdentifier) as ActivityInstance
            }
        val activityDef = activityInstance.definition.lookup()
        for (i in 0 until activityDef.inputParameters.size())
            compiler.addTypeToExportSystem(Identifier.of(activityDef.inputParameters[i].type_.read()))
        for (i in 0 until activityDef.outputParameters.size())
            compiler.addTypeToExportSystem(Identifier.of(activityDef.outputParameters[i].type_.read()))
        // the same has to be performed for the attached policies
        for (i in 0 until activityInstance.policies.size()) {
            val policyObject = activityInstance.policies[i].value.lookup()
            if (policyObject is ConstraintInstance) {
                val polDef = policyObject.definition.lookup()
                for (j in 0 until polDef.attribute.size())
                    compiler.addTypeToExportSystem(Identifier.of(polDef.attribute[j].type_.read()))

                // constraint and its definition needs to be added
                compiler.addConstraintInstanceToExport(policyObject)
            } else {
                // this is a policy instance which cannot define input types but can contain an arbitrary amount of
                // policies and constraints
                compiler.addPolicyInstanceToExport(policyObject as PolicyInstance)
            }
        }

        // add used activity and its definition to export runtime definition registry
        compiler.addActivityInstanceToExport(Identifier.of(activityCall.activity.name.toString()))

        // in case the activity is unknown simply skip the generation for this call
        val activity: Activity? = validateActivityExistence(activityCall)
        if (activity == null) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Could not resolve activity call.",
                    activityCall.file,
                    activityCall.position
                )
            )
            return
        }

        // try to get an activity name
        val activityObject = activityRegistry.read(
            Identifier.of(
                activityCall.activity.name.qualifier + "." + activityCall.activity.name.name
            )
        )
        val activityName = if (activityObject is ActivityInstance) {
            (activityObject as ActivityInstance?)?.name?.read()
        } else {
            null
        }
        // validate all parameters
        validateActivityCallParameters(activityCall, activity, activityName)
        // create input scope
        val inputScopeVarName = generateInputScopeForActivityCall(activityCall, activity, method)
        // output scope variable
        val outputScopeVarName = generateOutputScopeForActivityCall(activityCall)
        // the actual activity call
        generateDataAppActivitySandboxCall(method, activity, activityName, inputScopeVarName, outputScopeVarName)
        if (activity.codeBlock != null &&
            activity.codeBlock.read().isNotBlank()
        ) {
            // the functionality for this activity is defined within D°
            // therefore we need to compile the code block of the activity
            // but only if the code has not been generated yet
            if (pckClass.getMethods(activity.name.read().replace('.', '_')).isEmpty()) {
                // create a completely new variable visibility stack for this embedded D° activity
                pushScopeAwareStackLevel()
                // generate the method
                val activityMethod = generateDataAppBlockFun(
                    pckClass,
                    activity.name.read().replace('.', '_'),
                    listOf(
                        Pair(virtualMachine.newType(InputScope::class.java.simpleName), "input")
                    ),
                    virtualMachine.newType(OutputScope::class.java.simpleName)!!,
                    Access.PUBLIC,
                    false,
                    thrownExceptions
                )
                activity.inputParameters.split().map { parameter ->
                    compiler.addTypeToExportSystem(Identifier.of(parameter.type_.read()))
                    embedInputVariable(
                        parameter.name.read(), parameter.type_.read(),
                        activityMethod, activityCall
                    )
                }
                // we need to compile the actual activity code now
                val parsedBlock = DegreeParserFacade.parseBlock(activity.codeBlock!!.read())
                parsedBlock.errors.forEach { compilerMessages.add(CompilerMessage(it)) }
                if (parsedBlock.errors.isNotEmpty()) {
                    return
                }
                parsedBlock.root!!.statements.map { statement ->
                    generateDataAppStatement(pckClass, activityMethod, statement)
                }
                // restore the original scope since we are done here
                popScopeAwareStackLevel()
            }
        }
        // process (store in variable manager) the return parameters
        generateVariableStorageAfterActivityCall(activityCall, activity, outputScopeVarName, method)
    }

    private fun generateDataAppActivitySandboxCall(
        method: net.sourceforge.jenesis4java.Block,
        activity: Activity,
        activityInstanceName: String?,
        inputScopeVarName: String,
        outputScopeVarName: String
    ) {
        // add imports
        imports.add(LinkedList::class.java.canonicalName)
        imports.add(MappedPolicyInstanceMap::class.java.canonicalName)
        imports.add(DegreeException::class.java.canonicalName)
        imports.add(Identifier::class.java.canonicalName)

        val effectiveActivityName = activityInstanceName ?: ("shadow_" +
                if (activity.name.read().contains('.'))
                    activity.name.read()
                else
                    "core." + activity.name.read())

        method.newStmt(
            virtualMachine.newInvoke("OutputScope $outputScopeVarName = $sandboxVar", "callActivity")
                .addVariableArg("(ActivityInstance) $activityRegistryVar.read(Identifier.of(\"${effectiveActivityName}\"))")
                .addVariableArg(inputScopeVarName)
                .addVariableArg(
                    "new LinkedList<MappedPolicyInstanceMap> (((ActivityInstance)$activityRegistryVar.read" +
                            "(Identifier.of(\"${effectiveActivityName}\"))).getPolicies().split())"
                )
        )
        val policyCheck = method.newIf(virtualMachine.newFree("$outputScopeVarName == null"))
        policyCheck.newStmt(
            logError(
                "An error occurred during the runtime-validation of the policies for activity " +
                        "'${effectiveActivityName}'. Execution aborted."
            )
        )

        policyCheck.newThrow(
            virtualMachine.newInvoke("new DegreePolicyValidationException").addArg(
                "An error occurred during " +
                        "the runtime-validation of the policies for activity '${effectiveActivityName}'. Execution aborted."
            )
        )
        val errorCheck = method.newIf(
            virtualMachine.newFree(
                "$outputScopeVarName.getValues().containsKey(\"error\") && " +
                        "$outputScopeVarName.get(\"error\").getType().getIdentifier().equals(Identifier.of(\"error\"))"
            )
        )
        errorCheck.newThrow(
            virtualMachine.newInvoke("new DegreeException")
                .addVariableArg("\"An error during the execution of activity '${effectiveActivityName}' occurred. Error: \" + $outputScopeVarName.get(\"error\").read()")
        )
    }

    /**
     * Generates java code for a given if-[else-if]*-else? structure in D°.
     *
     * @param pckClass the class containing all java code
     * @param method Java-AST of the block which will contain the generated code
     * @param ifStmt D°-AST for an if statement
     */
    private fun generateDataAppIfStatement(
        pckClass: PackageClass,
        method: net.sourceforge.jenesis4java.Block,
        ifStmt: IfStatement
    ) {
        // the if
        val javaIfStmt = method.newIf(generateBoolExpression(ifStmt.conditions.first()))
        generateDataAppStatement(pckClass, javaIfStmt, ifStmt.blocks.first())
        // all elseIfs
        ifStmt.conditions.mapIndexed { idx, _ ->
            if (idx > 0) {
                val elseIfStmt = javaIfStmt.newElseIf(generateBoolExpression(ifStmt.conditions.elementAt(idx)))
                generateDataAppStatement(pckClass, elseIfStmt, ifStmt.blocks.elementAt(idx))
            }
        }
        // the else
        if (ifStmt.elseBlock != null) {
            val elseStmt = javaIfStmt.`else`
            generateDataAppStatement(pckClass, elseStmt, ifStmt.elseBlock!!)
        }
    }

    /**
     * This function generates boolean expressions which are used within if statements.
     *
     * @param expr The D°-Expression which will be transformed into a valid java expression.
     */
    private fun generateBoolExpression(expr: BoolExpression): net.sourceforge.jenesis4java.Expression {
        if (expr.negated) {
            return virtualMachine.newUnary(Unary.NOT, generateBoolExpression(expr.expression!!))
        } else if (expr.comperator != null) {
            val comperator = when (expr.comperator) {
                BooleanComperator.EQ -> Binary.EQUAL_TO
                BooleanComperator.LT -> Binary.LESS
                BooleanComperator.GT -> Binary.GREATER
                BooleanComperator.LEQ -> Binary.LESS_EQUAL
                BooleanComperator.GEQ -> Binary.GREATER_EQUAL
                BooleanComperator.NEQ -> Binary.NOT_EQUAL
                null -> {
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.ERROR,
                            "Comparator in boolean-binary-expression must not be null.",
                            expr.file,
                            expr.position
                        )
                    )
                    throw IllegalStateException()
                }
            }
            val left = generateBoolExpression(expr.left_expression!!)
            val right = generateBoolExpression(expr.right_expression!!)

            if (comperator == Binary.EQUAL_TO) {
                if (left is Freeform) {// && (left.code == "\"true\"" || left.code == "\"false\"")) {
                    val stringWriter = StringWriter()
                    val printWriter = PrintWriter(stringWriter)
                    val codeWriter = MCodeWriter(printWriter)
                    right.toCode(codeWriter)

                    return virtualMachine.newFree(left.code + ".equals(${stringWriter})")
                } else if (right is Freeform) {// && (right.code == "\"true\"" || right.code == "\"false\"")) {
                    val stringWriter = StringWriter()
                    val printWriter = PrintWriter(stringWriter)
                    val codeWriter = MCodeWriter(printWriter)
                    left.toCode(codeWriter)

                    return virtualMachine.newFree("$stringWriter.equals(${right.code})")
                }
            }

            return virtualMachine.newBinary(
                comperator,
                generateBoolExpression(expr.left_expression!!),
                generateBoolExpression(expr.right_expression!!)
            )
        } else if (expr.operator != null) {
            val operator = when (expr.operator) {
                BooleanOperator.AND -> Binary.AND
                BooleanOperator.OR -> Binary.OR
                null -> {
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.ERROR,
                            "Operator in boolean-binary-expression must not be null.",
                            expr.file,
                            expr.position
                        )
                    )
                    throw IllegalStateException()
                }
            }

            val left = generateBoolExpression(expr.left_expression!!)
            val right = generateBoolExpression(expr.right_expression!!)

            return virtualMachine.newBinary(operator, left, right)
        } else if (expr.intLiteral != null) {
            return virtualMachine.newInt(expr.intLiteral!!)
        } else if (expr.floatLiteral != null) {
            return virtualMachine.newFloat(expr.floatLiteral!!)
        } else if (expr.stringLiteral != null) {
            return virtualMachine.newFree(expr.stringLiteral!!)
        } else if (expr.boolLiteral != null) {
            return if (expr.boolLiteral!!) {
                virtualMachine.newTrue()
            } else {
                virtualMachine.newFalse()
            }
        } else if (expr.varReference != null) {
            if (checkVariableExistsByUuid(expr.varReference!!.name)) {
                return if (expr.varReference!!.index == -1)
                    virtualMachine.newInvoke(
                        virtualMachine.newInvoke("variableManager", "readVariable")
                            .addVariableArg("UUID.fromString(\"${retrieveVariableUuidByName(expr.varReference!!.name)}\")"),
                        "read"
                    )
                else
                    virtualMachine.newInvoke(
                        virtualMachine.newInvoke("variableManager", "readVariable")
                            .addVariableArg("UUID.fromString(\"${retrieveVariableUuidByName(expr.varReference!!.name)}\")"),
                        "get(${expr.varReference!!.index}).read"
                    )
            } else {
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Expression refers to unknown variable '${expr.varReference!!.name}'.",
                        expr.file,
                        expr.position
                    )
                )
                throw IllegalStateException()
            }
        } else if (expr.fieldAccess != null) {
            // generate code for the field access
            if (validateFieldAccessCallChain(expr.fieldAccess!!) == null) {
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Invalid field access '${expr.fieldAccess!!}' in expression.",
                        expr.file,
                        expr.position
                    )
                )
                throw IllegalStateException()
            } else {
                var generatedExpression: net.sourceforge.jenesis4java.Expression =
                    virtualMachine.newInvoke("variableManager", "readVariable")
                        .addVariableArg("UUID.fromString(\"${retrieveVariableUuidByName(expr.fieldAccess!!.reference.name)}\")")
                if (expr.fieldAccess!!.index > -1) {
                    generatedExpression =
                        virtualMachine.newInvoke(generatedExpression, "get").addArg(expr.fieldAccess!!.index)
                }
                if (expr.fieldAccess!!.accessedFields.isNotEmpty())
                    generatedExpression =
                        virtualMachine.newCast(virtualMachine.newType("CompositeInstance"), generatedExpression)
                expr.fieldAccess!!.accessedFields.forEachIndexed { index, it ->
                    generatedExpression = virtualMachine.newInvoke(generatedExpression, "get")
                        .addArg(virtualMachine.newFree("new Identifier(\"$it\").purge()"))
                    if (index < expr.fieldAccess!!.accessedFields.size - 1)
                        generatedExpression =
                            virtualMachine.newCast(virtualMachine.newType("CompositeInstance"), generatedExpression)
                }

                // determine if a build-in function is used
                if (expr.method != null) {
                    val method = try {
                        BuildInFunctions.valueOf(expr.method!!.toUpperCase())
                    } catch (e: IllegalArgumentException) {
                        compilerMessages.add(
                            CompilerMessage(
                                CompilerMessage.Kind.ERROR,
                                "Tried to use an unknown function '${expr.method}'.",
                                expr.file,
                                expr.position
                            )
                        )
                        throw IllegalStateException()
                    }
                    when (method) {
                        BuildInFunctions.SIZE -> {
                            generatedExpression = virtualMachine.newInvoke(generatedExpression, "size")
                        }
                    }
                } else {
                    // otherwise add a final read
                    generatedExpression = virtualMachine.newInvoke(generatedExpression, "read")
                }

                return generatedExpression
            }
        } else {
            return virtualMachine.newBrackets(generateBoolExpression(expr.expression!!))
        }
    }

    /**
     * Generates java code for a D° variable assignment type instantiation and attaches generated code to given method.
     *
     * @param method Java-AST of the block which will contain the generated code
     * @param stmt D°-AST for a variable assignment type instantiation
     */
    private fun generateDataAppVariableAssignmentTypeInstantiation(
        method: net.sourceforge.jenesis4java.Block,
        stmt: VariableAssignmentTypeInstantiation
    ) {
        val variableName = stmt.name
        val variableType = retrieveTypeFromReference(stmt.value.type)
        // check if the type is known
        if (!typeTaxonomy.contains(variableType)) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Referenced unknown type '$variableType'.",
                    stmt.file,
                    stmt.position
                )
            )
            return
        }
        // the uuid of this variable
        val uuid: UUID? = retrieveUuidForVariable(variableName, variableType, stmt, method)
        // create the instance for this variable
        val tempVarName = "var_" + uuidToJavaIdentifier(uuid!!)
        // register the used type in the export type system
        compiler.addTypeToExportSystem(Identifier.of("${variableType.parseNamespace()}.${variableType.parseIdentifier()}"))
        if (checkVariableIsDefinedInCurrentScope(variableName)) {
            method.newStmt(
                virtualMachine.newInvoke("$tempVarName = $typeTaxonomyVar", "create")
                    .addVariableArg("Identifier.of(\"${variableType.parseNamespace()}.${variableType.parseIdentifier()}\")")
            )
        } else {
            scopeAwareVariableInstances.last().add(variableName)
            method.newStmt(
                virtualMachine.newInvoke("Instance $tempVarName = $typeTaxonomyVar", "create")
                    .addVariableArg("Identifier.of(\"${variableType.parseNamespace()}.${variableType.parseIdentifier()}\")")
            )
        }

        // evaluate the variable assignment
        stmt.value.functions.map { method.newStmt(generateDefinitionFunction(it, tempVarName)) }

        var setVarFunction = "initializeVariable"
        if (checkVariableExistsByUuid(variableName)) {
            setVarFunction = "updateVariable"
        }

        // initialize the variable in the data app
        method.newStmt(
            virtualMachine.newInvoke("variableManager", setVarFunction)
                .addVariableArg("UUID.fromString(\"$uuid\")")
                .addVariableArg(tempVarName)
        )

        scopeAwareVariableToTypeMapList.last()[variableName] = variableType
        scopeAwareVariableToUuidMapList.last()[variableName] = uuid
    }

    private fun generateVariableAttributeAssignment(
        method: net.sourceforge.jenesis4java.Block,
        stmt: VariableAttributeAssignment
    ) {
        // ensure variable & attribute is valid
        validateVariableExistence(stmt.name, "variable")

        // resolve variable
        var baseVariableType = resolveVariableTypeByName(stmt.name)
        var currentInstance = typeTaxonomy.create(baseVariableType)

        if (stmt.index != -1) {
            if (currentInstance.cardinality.value() != 0 &&
                currentInstance.cardinality.value() <= stmt.index
            ) {
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.WARNING,
                        "Possible problem detected: " +
                                "Variable '${stmt.name}' has cardinality ${currentInstance.cardinality.value()}. " +
                                "Cannot access index ${stmt.index}.",
                        stmt.file,
                        stmt.position
                    )
                )
            }
            currentInstance.get(stmt.index)
        }

        for (i in stmt.attributes.indices) {
            if (stmt.attributes.isNotEmpty() && currentInstance.isPrimitive) {
                throw IllegalStateException("Primitive type '${currentInstance.type.identifier}' does not allow attribute access.")
            }

            val identifier = Identifier.of(stmt.attributes[i].first).purge()
            val index = stmt.attributes[i].second
            if ((currentInstance as CompositeInstance).type.attributes.containsKey(identifier)) {
                currentInstance = try {
                    currentInstance.get(identifier)
                } catch (e: IllegalArgumentException) {
                    currentInstance.type.newInstance(Cardinality.SINGULAR).get(identifier)
                }
                if (index != -1) {
                    if (currentInstance.cardinality.value() != 0 &&
                        currentInstance.cardinality.value() <= index
                    ) {
                        compilerMessages.add(
                            CompilerMessage(
                                CompilerMessage.Kind.WARNING,
                                "Possible problem detected: " +
                                        "Attribute '${stmt.attributes[i].first}' has cardinality ${currentInstance.cardinality.value()}. " +
                                        "Cannot access index ${index}.",
                                stmt.file,
                                stmt.position
                            )
                        )
                    }
                    // TODO this breaks on lists
                    //currentInstance.get(index)
                }
            } else {
                throw IllegalStateException(
                    "Type '${currentInstance.type.identifier}' does not have an attribute " +
                            "of type '${stmt.attributes[i].first}'."
                )
            }
        }

        // ensure the assignment is valid
        // FIXME the validation does not use the update methods and therefore ignores validation of parent objects
        when (stmt.value) {
            is StringLiteral -> {
                try {
                    when (stmt.operator) {
                        AssignmentOperator.ASSIGN -> currentInstance.write((stmt.value as StringLiteral).value)
                        AssignmentOperator.ADD -> currentInstance.add((stmt.value as StringLiteral).value)
                    }
                } catch (e: Exception) {
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.WARNING,
                            "There may be a problem assigning value '${(stmt.value as StringLiteral).value}' " +
                                    "to variable of type '${currentInstance.type.identifier}'.",
                            stmt.file,
                            stmt.position
                        )
                    )
                }
            }
            is VariableReference -> {
                // TODO: this validation does not take into account custom validation of parent type
                val varName = (stmt.value as VariableReference).name
                if (currentInstance.type.findSubtype(retrieveVariableTypeByName(varName)) == null) {
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.WARNING,
                            "There may be a problem assigning variable '${varName}' " +
                                    "of type '${retrieveVariableTypeByName(varName)}' to variable of type " +
                                    "'${currentInstance.type.identifier}'.",
                            stmt.file,
                            stmt.position
                        )
                    )
                }
            }
            else -> throw NotImplementedError(
                "Variable assignment via '${stmt.value.javaClass.canonicalName}' " +
                        "is currently not supported."
            )
        }

        // generate code for assignment
        val operation = when (stmt.operator) {
            AssignmentOperator.ASSIGN -> "write"
            AssignmentOperator.ADD -> "add"
        }

        val value: String = when (stmt.value) {
            is StringLiteral -> {
                "\"${(stmt.value as StringLiteral).value}\""
            }
            is VariableReference -> {
                if ((stmt.value as VariableReference).index < 0)
                    "variableManager.readVariable(" +
                            "UUID.fromString(" +
                            "\"${retrieveVariableUuidByName((stmt.value as VariableReference).name)}\"" +
                            ")" +
                            ").read()"
                else
                    "variableManager.readVariable(" +
                            "UUID.fromString(" +
                            "\"${retrieveVariableUuidByName((stmt.value as VariableReference).name)}\"" +
                            ")" +
                            ").get(${(stmt.value as VariableReference).index}).read()"
            }
            else -> throw NotImplementedError(
                "Variable assignment via '${stmt.value.javaClass.canonicalName}' " +
                        "is currently not supported."
            )
        }

        var statement =
            "variableManager.readVariable(" +
                    "UUID.fromString(" +
                    "\"${retrieveUuidForVariable(stmt.name, baseVariableType, stmt, method)!!}\"" +
                    ")" +
                    ")"
        // add imports
        imports.add(PrimitiveInstance::class.java.canonicalName)
        imports.add(CompositeInstance::class.java.canonicalName)

        if (stmt.attributes.isEmpty()) {
            // update of a primitive instance
            statement = if (stmt.index == -1) {
                // singular type
                "$statement.$operation($value)"
            } else {
                // non singular type
                if (operation == "add") {
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.ERROR,
                            "+= operator not allowed in combination with index acces.",
                            stmt.file,
                            stmt.position
                        )
                    )
                    return
                }
                // the uuid of this variable
                val uuid: UUID = createFreeUuid()
                // create the instance for this variable
                val tempVarName = "var_assignment_" + uuidToJavaIdentifier(uuid)
                "Instance $tempVarName = $statement;\n" +
                        "if ($tempVarName.getType().isPrimitive) {\n" +
                        "\t((PrimitiveInstance) $tempVarName).$operation(${stmt.index}, $value);\n" +
                        "} else {\n" +
                        "\t((CompositeInstance) $tempVarName).update(${stmt.index}, e -> e.$operation($value))\n" +
                        "}"
            }
        } else {
            // add imports
            imports.add(PrimitiveInstance::class.java.canonicalName)
            imports.add(CompositeInstance::class.java.canonicalName)

            val placeholder = "%%%"
            // we have to traverse the attributes in a reverse order since of the structure of the commands
            for (index in stmt.attributes.indices) {
                if (index == 0) {
                    if (index == stmt.attributes.size - 1) {
                        statement = if (stmt.attributes[index].second != -1) {
                            "\t((CompositeInstance) $statement).update(${stmt.attributes[index].second}, e${index}_index -> e${stmt.attributes.size - 1}_index.$operation($value));\n"
                        } else {
                            "\t((CompositeInstance) $statement).update(Identifier.of(\"${stmt.attributes[index].first}\"), e${index}_index -> e${stmt.attributes.size - 1}_index.$operation($value));\n"
                        }
                    } else {
                        statement =
                            "((CompositeInstance) $statement).update(Identifier.of(\"${stmt.attributes[index].first}\"), e$index -> {\n$placeholder\n})\n"
                        statement = if (stmt.attributes[index].second != -1) {
                            statement.replace(
                                placeholder,
                                "e$index.update(${stmt.attributes[index].second}, e${index}_index -> { \n$placeholder\n});\n"
                            )
                        } else {
                            statement.replace(
                                placeholder,
                                "Instance e${index}_index = e$index;\n$placeholder"
                            )
                        }
                    }
                } else if (index == stmt.attributes.indices.last) {
                    statement = if (stmt.attributes[index].second != -1) {
                        statement.replace(
                            placeholder,
                            "if (e instanceof CompositeInstance) {\n" +
                                    "\t((CompositeInstance) e${index - 1}_index).update(${stmt.attributes[index].second}, e${index}_index -> e${stmt.attributes.size - 1}_index.$operation($value););\n" +
                                    "} else {\n" +
                                    "\t((PrimitiveInstance) e${index - 1}_index).$operation(${stmt.attributes[index].second}, $value);\n" +
                                    "}\n"
                        )
                    } else {
                        statement.replace(
                            placeholder,
                            //"if (e${index-1}_index instanceof CompositeInstance) {\n" +
                            "\t((CompositeInstance) e${index - 1}_index).update(Identifier.of(\"${stmt.attributes[index].first}\"), e_final -> e_final.$operation($value));\n"
                        )
                    }
                } else {
                    statement = statement.replace(
                        placeholder,
                        "((CompositeInstance) e${index - 1}).update(Identifier.of(\"${stmt.attributes[index].first}\"), e$index -> {\n$placeholder\n});\n"
                    )
                    if (stmt.attributes[index].second != -1) {
                        statement = statement.replace(
                            placeholder,
                            "((CompositeInstance) e$index).update(${stmt.attributes[index].second}, e${index}_index -> { \n$placeholder\n});\n"
                        )
                    } else {
                        statement = statement.replace(
                            placeholder,
                            "Instance e${index}_index = e$index;\n$placeholder"
                        )
                    }
                }
            }
        }
        statement = statement.replace("^;(\\s)*;$", ";")
        statement = if (statement.dropLast(1).last() == ';') statement.dropLast(2) + "\n" else statement
        method.newStmt(virtualMachine.newFree(statement))
    }


    /**
     * Generates a (Java) return statement from a D°-Return-Statement-AST.
     *
     * @param method the java method which will contain all generated code
     * @param stmt the D°-AST for a return statement
     */
    private fun generateDataAppReturnStatement(method: net.sourceforge.jenesis4java.Block, stmt: ReturnStatement) {
        stmt.values.map {
            val result = generateReturnVariable(method, it)
            if (result != null) {
                method.newStmt(
                    virtualMachine.newInvoke(returnOutputScopeVar, "add")
                        .addArg(result.first)
                        .addVariableArg(result.second)
                )
            }
        }
        if (method is Method && method.type == virtualMachine.newType(Type.BOOLEAN)) {
            method.newReturn().expression = virtualMachine.newBoolean(true)
        } else {
            method.newReturn().expression = virtualMachine.newVar("returnOutputScope")
        }
    }

    private fun resolveVariableTypeByName(name: String): Identifier {
        scopeAwareVariableToTypeMapList.asReversed().forEach { map ->
            if (map.containsKey(name)) {
                return map[name]!!

            }
        }

        throw IllegalStateException("Could not resolve type of variable '$name'.")
    }

    /**
     * This function needs to be called for every statement and generates the corresponding java-code.
     *
     * @param pckClass the class containing all java code
     * @param block Java-AST of the block which will contain the generated code
     * @param stmt D°-AST of the statement
     * @param hasParent only relevant for statements of type block, indicator if there is a parent block. This information is required for variable scoping. Default value is "true"
     * @param rootLevel true only for the entry point of the application logic. is required for proper return statement handling
     */
    private fun generateDataAppStatement(
        pckClass: PackageClass,
        block: net.sourceforge.jenesis4java.Block,
        stmt: Statement,
        hasParent: Boolean = true,
        rootLevel: Boolean = false
    ) {
        when (stmt) {
            is Block -> {
                // new hierarchy level
                addScopeAwareLevel()
                if (!rootLevel) {
                    val returnIf = block.newIf(generateDataAppBlock(pckClass, block, stmt, hasParent))
                    if (hasParent) {
                        returnIf.newReturn().expression = virtualMachine.newBoolean(true)
                    } else {
                        returnIf.newReturn().expression = virtualMachine.newInvoke("returnOutputScope", "toJson")
                    }
                } else {
                    block.newStmt(generateDataAppBlock(pckClass, block, stmt, hasParent))
                }
                // move one level up on the variable scope hierarchy
                removeScopeAwareLevel()
            }
            is ActivityCall -> {
                generateDataAppActivityCall(pckClass, block, stmt)
            }
            is IfStatement -> {
                generateDataAppIfStatement(pckClass, block, stmt)
            }
            is VariableAssignmentTypeInstantiation -> {
                generateDataAppVariableAssignmentTypeInstantiation(block, stmt)
            }
            is VariableAttributeAssignment -> {
                generateVariableAttributeAssignment(block, stmt)
            }
            is ReturnStatement -> {
                generateDataAppReturnStatement(block, stmt)
            }
            else -> {
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Unknown type of statement '${stmt.javaClass.simpleName}' found. Going to ignore it.",
                        stmt.file,
                        stmt.position
                    )
                )
            }
        }
    }

    /**
     * TODO Not yet implemented
     */
    private fun validateInputMethodCallExpression(input: MethodCallExpression) {
        throw NotImplementedError(input.toString())
    }

    /**
     * If a variable is referenced the existence is checked and the type is determined.
     *
     * @param input the D°-AST for a variable reference
     * @return the qualified identifier of the inputs type if the validation was successful, null otherwise
     */
    private fun validateInputVariableReference(input: VariableReference): Identifier? {
        return if (!validateVariableExistence(input.name)) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Variable '${input.name}' should be used as input parameter but is not known.",
                    input.file,
                    input.position
                )
            )
            null
        } else {
            retrieveVariableTypeByName(input.name)
        }
    }

    /**
     * TODO Not yet implemented
     * FIXME not part of grammar right now
     */
    private fun validateInputBooleanLiteral(input: BooleanLiteral) {
        throw NotImplementedError(input.toString())
    }

    /**
     * TODO Not yet implemented
     * FIXME not part of grammar right now
     */
    private fun validateInputIntegerLiteral(input: IntegerLiteral) {
        throw NotImplementedError(input.toString())
    }

    /**
     * TODO Not yet implemented
     * FIXME not part of grammar right now
     */
    private fun validateInputFloatingPointLiteral(input: FloatingPointLiteral) {
        throw NotImplementedError(input.toString())
    }

    /**
     * TODO Not yet implemented
     */
    private fun validateInputStringLiteral(input: StringLiteral) {
        throw NotImplementedError(input.toString())
    }

    /**
     * Existence of base variable is checked.
     * Call chain (x.y.z) is resolved and checked.
     * Type is determined
     *
     * @param input D°-AST for a field access
     * @return the qualified identifier of the inputs type if the validation was successful, null otherwise
     */
    private fun validateInputFieldAccess(input: FieldAccess): Identifier? {
        var result: Identifier? = null
        if (!validateVariableExistence(input.reference.name)) {
            return result
        } else {
            val current = retrieveVariableTypeByName(input.reference.name)
            // check all field accesses
            input.accessedFields.forEachIndexed { idx, elem ->
                val field = typeTaxonomy.lookup(current)
                // only the last field is allowed to be a primitive type
                if (field is PrimitiveType) {
                    val fieldName = if (idx == 0) {
                        input.reference.name
                    } else {
                        input.accessedFields[idx - 1]
                    }
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.ERROR,
                            "Field $fieldName in field access ${input.reference.name}.${
                                input.accessedFields.joinToString(
                                    separator = "."
                                )
                            } is a primitive type.",
                            input.file,
                            input.position
                        )
                    )
                    return result
                }
                try {
                    val attr = (field as CompositeType).get(Identifier(elem))
                    result = attr.type.identifier
                } catch (e: IllegalArgumentException) {
                    val type = if (idx > 0) {
                        result
                    } else {
                        retrieveVariableTypeByName(input.reference.name)
                    }
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.ERROR,
                            "Field $elem does not exist in type $type in field access ${input.reference.name}.${
                                input.accessedFields.joinToString(
                                    separator = "."
                                )
                            }.",
                            input.file,
                            input.position
                        )
                    )
                    return result
                }
            }
        }
        return result
    }

    /**
     * This function performs type specific checks for a given expression which will be used as input parameter.
     *
     * @param input the input parameter which will be validated
     * @return the qualified identifier of the inputs type if the validation was successful, null otherwise
     */
    private fun validateInputParameterSpecific(input: Expression): Identifier? {
        var result: Identifier? = null
        // make checks which depend on the expression type
        when (input) {
            is MethodCallExpression -> {
                validateInputMethodCallExpression(input)
            }
            is VariableReference -> {
                result = validateInputVariableReference(input)
            }
            is BooleanLiteral -> {
                validateInputBooleanLiteral(input)
            }
            is IntegerLiteral -> {
                validateInputIntegerLiteral(input)
            }
            is FloatingPointLiteral -> {
                validateInputFloatingPointLiteral(input)
            }
            is StringLiteral -> {
                validateInputStringLiteral(input)
            }
            is FieldAccess -> {
                result = validateInputFieldAccess(input)
            }
            else -> {
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Found unknown construct ${input::class.simpleName} as input parameter.",
                        input.file,
                        input.position
                    )
                )
                return result
            }
        }
        return result
    }

    /**
     * Performs generic checks for a single input parameter which are not affected by the actual type of the parameter.
     *
     * @param input the D°-AST for an input parameter
     * @param definition the D° definition of the corresponding parameter
     * @param instanceType type of input
     * @return true if the validation was successful, false otherwise
     */
    private fun validateInputParameterGeneric(
        input: Expression,
        definition: TaggedParameter,
        instanceType: Identifier
    ): Boolean {
        var result = true
        /**
         * Check type compatibility.
         */
        result = validateParameterType(definition, instanceType, input) && result

        return result
    }

    /**
     * Validates a single input parameter.
     * Type specific validation and generic validation
     *
     * @param input the D°-AST for an input parameter
     * @param definition the D° definition of the corresponding parameter
     */
    private fun validateInputParameter(input: Expression, definition: TaggedParameter): Boolean {
        var result = true
        // type specific checks
        val instanceType: Identifier? = validateInputParameterSpecific(input)
        // check for errors
        if (instanceType == null) {
            result = false
            return result
        }
        // generic checks
        result = validateInputParameterGeneric(input, definition, instanceType) && result
        return result
    }

    /**
     * Validates all input parameters for a single activity call. Uses activity definition parameters for some checks.
     *
     * @param callParameters collection of input parameters
     * @param definedParameters collection of input parameters from the matching D°-definition
     * @return true if all parameters have been validated successfully, false otherwise
     */
    private fun validateInputParameters(
        callParameters: Collection<Expression>,
        definedParameters: List<TaggedParameter>
    ): Boolean {
        var result = true
        callParameters.forEachIndexed { idx, it ->
            run {
                result = validateInputParameter(it, definedParameters[idx])
            }
        }

        return result
    }

    /**
     * Validates a method call expression which should be used as output parameter for an activity call.
     * Since this is not a valid construct the validation always fails.
     *
     * @param output the D°-AST for a method call expression
     * @return false
     */
    private fun validateOutputMethodCallExpression(output: MethodCallExpression): Boolean {
        compilerMessages.add(
            CompilerMessage(
                CompilerMessage.Kind.ERROR,
                "It is not allowed to use activity calls as return parameter.",
                output.file,
                output.position
            )
        )
        return false
    }

    /**
     * Validates a variable reference which should be used as output parameter for an activity call.
     *
     * @param output D°-AST for a variable reference
     * @param definition the definition of the output parameter which should match the variable reference
     * @return true if the validation was successful, false otherwise
     */
    private fun validateOutputVariableReference(output: VariableReference, definition: Parameter): Boolean {
        var result = true
        /**
         * If the variable already exists a type check is required
         */
        if (checkVariableExistsByType(output.name) &&
            !checkVariableTypeCompatibility(
                output.name,
                Identifier.of(definition.type_.read()),
                output,
                "output parameter"
            )
        ) {
            result = false
        }

        return result
    }

    /**
     * Validates a string literal which should be used as output parameter for an activity call.
     * Since this is not a valid construct the validation always fails.
     *
     * @param output the D°-AST for a string literal
     * @return false
     */
    private fun validateOutputStringLiteral(output: StringLiteral): Boolean {
        compilerMessages.add(
            CompilerMessage(
                CompilerMessage.Kind.ERROR,
                "It is not allowed to use string literals as return parameter.",
                output.file,
                output.position
            )
        )

        return false
    }

    /**
     * Validates that a call chain (x.y.z) used in a field access is valid.
     *
     * @param fieldAccess the D° field access which needs to be validated
     * @return null if an error occurs, otherwise the qualified identifier of the type of the last element in the call chain
     */
    private fun validateFieldAccessCallChain(fieldAccess: FieldAccess): Identifier? {
        var result: Identifier? = null
        val current = retrieveVariableTypeByName(fieldAccess.reference.name)
        result = current
        fieldAccess.accessedFields.forEachIndexed { idx, elem ->
            val field = typeTaxonomy.lookup(current)
            // only the last field is allowed to be a primitive type
            if (field is PrimitiveType) {
                val fieldName = if (idx == 0) {
                    fieldAccess.reference.name
                } else {
                    fieldAccess.accessedFields[idx - 1]
                }
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Field $fieldName in field access ${fieldAccess.reference.name}." +
                                "${fieldAccess.accessedFields.joinToString(separator = ".")} is a primitive type.",
                        fieldAccess.file,
                        fieldAccess.position
                    )
                )
                return null
            }
            try {
                val attr = (field as CompositeType).get(Identifier(elem).purge())
                result = attr.type.identifier

            } catch (e: IllegalArgumentException) {
                val type = if (idx > 0) {
                    result
                } else {
                    retrieveVariableTypeByName(fieldAccess.reference.name)
                }
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Field $elem does not exist in type $type in field access " +
                                "${fieldAccess.reference.name}.${fieldAccess.accessedFields.joinToString(separator = ".")}.",
                        fieldAccess.file,
                        fieldAccess.position
                    )
                )
                return null
            }
        }
        return result
    }

    /**
     * Validates an field access which should be used as output parameter for an activity call.
     * Includes resolving the call chain and type checks.
     *
     * @param output D°-AST of a field access
     * @param definition Parameter definition which has to match the field access
     * @return true if the validation was successful, false otherwise
     */
    private fun validateOutputFieldAccess(output: FieldAccess, definition: Parameter): Boolean {
        var result = true
        if (!validateVariableExistence(output.reference.name, "output")) {
            result = false
            return result
        } else {
            // check all field accesses
            val instanceType: Identifier? = validateFieldAccessCallChain(output)
            // final type check
            if (definition.type.identifier != instanceType) {
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Type mismatch on output parameter ${definition.name}: " +
                                "Found $instanceType but expected ${definition.type_.read()}.",
                        output.file,
                        output.position
                    )
                )
                result = false
            }
        }

        return result
    }

    /**
     * Validates all output parameters for a single activity call. Uses activity definition parameters for some checks.
     *
     * @param callParameters collection of output parameters
     * @param definedParameters collection of output parameters from the matching D°-definition
     * @return true if all parameters have been validated successfully, false otherwise
     */
    private fun validateOutputParameters(
        callParameters: Collection<Expression>,
        definedParameters: List<Parameter>
    ): Boolean {
        var result = true
        callParameters.forEachIndexed { idx, it ->
            run {
                when (it) {
                    is MethodCallExpression -> {
                        result = validateOutputMethodCallExpression(it) && result
                    }
                    is VariableReference -> {
                        result = validateOutputVariableReference(it, definedParameters[idx]) && result
                    }
                    is StringLiteral -> {
                        result = validateOutputStringLiteral(it) && result
                    }
                    is FieldAccess -> {
                        result = validateOutputFieldAccess(it, definedParameters[idx])
                    }
                    else -> {
                        compilerMessages.add(
                            CompilerMessage(
                                CompilerMessage.Kind.ERROR,
                                "Found unknown construct ${it::class.simpleName} as output parameter.",
                                it.file,
                                it.position
                            )
                        )
                        result = false
                        return@run
                    }
                }
            }
        }

        return result
    }

    /**
     * Applies a given D° definition function to a variable and returns the generated java code.
     *
     * @param defFun D°-AST of definition function
     * @param varName name of the variable to which the definition function is applied
     * @return Java-AST for given D° definition function
     */
    private fun generateDefinitionFunction(
        defFun: DefinitionFunction,
        varName: String
    ): net.sourceforge.jenesis4java.Expression {
        val exp: net.sourceforge.jenesis4java.Expression = if (defFun.name == "cardinality") {
            imports.add(Cardinality::class.java.canonicalName)
            if (defFun.arguments.size != 1) {
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Definition function 'cardinality' expects exactly one argument.",
                        defFun.file,
                        defFun.position
                    )
                )
                throw IllegalArgumentException("Definition function 'cardinality' expects exactly one argument.")
            }
            virtualMachine.newFree(
                "$varName = $varName.getType().newInstance(new Cardinality(${(defFun.arguments[0] as StringLiteral).value}))"
            )
        } else {
            val tmp = virtualMachine.newInvoke(varName, defFun.name)
            defFun.arguments.map {
                var param = ""
                when (it) {
                    is TypeReference -> {
                        throw NotImplementedError()
                    }
                    is ReferenceByQualifiedName -> {
                        throw NotImplementedError()
                    }
                    is Expression -> {
                        param = generateExpressionString(it)
                    }
                }
                tmp.addVariableArg(param)
            }
            tmp
        }

        return exp
    }

    /**
     * Resolves a D° type reference and returns the qualified identifier.
     *
     * @param ref D° type reference
     * @return qualified identifier which contains the type of given type reference
     */
    private fun retrieveTypeFromReference(ref: TypeReference): Identifier {
        return ref.name.let { Identifier.of(it.qualifier + "." + it.name) }
    }

    /**
     * Generates java code for a given D°-Expression.
     *
     * @param expr the D°-Expression which should be converted to java
     * @return string representing java-code for an expression
     */
    private fun generateExpressionString(expr: Expression): String {
        when (expr) {
            is MethodCallExpression -> {
                throw NotImplementedError(expr.toString())
            }
            is VariableReference -> {
                throw NotImplementedError(expr.toString())
            }
            is StringLiteral -> {
                return "\"" + expr.value + "\""
            }
            is FieldAccess -> {
                throw NotImplementedError(expr.toString())
            }
        }
        return ""
    }

    /**
     * Generates java code for a given D°-return value.
     *
     * @param expr the D°-Expression which should be converted to java
     * @return string representing java-code for an expression
     */
    private fun generateReturnVariable(
        method: net.sourceforge.jenesis4java.Block,
        expr: Expression
    ): Pair<String, String>? {
        when (expr) {
            is MethodCallExpression -> {
                throw NotImplementedError(expr.toString())
            }
            is VariableReference -> {
                if (expr.index < 0)
                    return Pair(
                        expr.name,
                        "variableManager.readVariable(UUID.fromString(\"${retrieveVariableUuidByName(expr.name)}\"))"
                    )
                else
                    return Pair(
                        expr.name,
                        "variableManager.readVariable(UUID.fromString(\"${retrieveVariableUuidByName(expr.name)}\")).get(${expr.index})"
                    )
            }
            is StringLiteral -> {
                val literalUuid = createFreeUuid()
                val literalIdentifier = uuidToJavaIdentifier(literalUuid)
                val anonymousName = "anonymousText${anonymousCounter++}"
                scopeAwareVariableToUuidMapList.last()[anonymousName] = literalUuid
                val varName = "var_$literalIdentifier"

                scopeAwareVariableInstances.last().add(literalIdentifier)
                method.newStmt(
                    virtualMachine.newInvoke("Instance $varName = $typeTaxonomyVar", "create")
                        .addVariableArg("new Identifier(\"core.Text\")")
                )
                method.newStmt(virtualMachine.newInvoke(varName, "write").addArg(expr.value))
                return Pair(anonymousName, varName)
            }
            is FieldAccess -> {
                var callChain =
                    "variableManager.readVariable(UUID.fromString(\"${retrieveVariableUuidByName(expr.reference.name)}\"))"
                if (expr.reference.index > -1)
                    callChain += ".get(${expr.reference.index})"

                if (expr.accessedFields.isNotEmpty())
                    callChain = "((CompositeInstance) $callChain)"
                expr.accessedFields.forEachIndexed { index, it ->
                    callChain += ".get(new Identifier($it).purge())"
                    if (index < expr.accessedFields.size - 1)
                        callChain = "((CompositeInstance) $callChain)"
                }

                return Pair(
                    "anonymousField${anonymousCounter++}",
                    callChain
                )
            }
        }
        return null
    }

    /**
     * Generates java method with given name, inputs, output and visibility and attaches it to given class.
     * Will also generate code for scoped variable managers.
     *
     * @param pckClass java class which contains all generated code
     * @param funName name of the generated function
     * @param inTypes input types of the generated function
     * @param outType return type of the generated function
     * @param accessType java visibility of the function. Default value is public
     * @param hasParent indicator if this method is called by another block function or by the root. Relevant for scoping with variable managers
     * @return java method for a D°-Block
     */
    private fun generateDataAppBlockFun(
        pckClass: PackageClass,
        funName: String,
        inTypes: List<Pair<Type, String>>,
        outType: Type,
        accessType: Access.AccessType = Access.AccessType.PUBLIC,
        hasParent: Boolean = true,
        thrownExceptions: List<String> = ArrayList()
    ): ClassMethod {
        val method = pckClass.newMethod(outType, funName)
        method.access = accessType
        inTypes.map { method.addParameter(it.first, it.second) }

        // determine if a variableManager should be passed to this function
        if (hasParent) {
            method.addParameter(
                virtualMachine.newType(VariableManager::class.java.simpleName)!!,
                "parentVarManager"
            )
            method.newStmt(virtualMachine.newVar("VariableManager variableManager = new VariableManager(parentVarManager)"))
            method.addParameter(virtualMachine.newType("OutputScope"), "returnOutputScope")
        } else {
            method.newStmt(virtualMachine.newVar("VariableManager variableManager = new VariableManager(null)"))
            method.newStmt(virtualMachine.newFree("OutputScope returnOutputScope = new OutputScope()"))
        }
        thrownExceptions.map { method.addThrows(it) }

        return method
    }

    /**
     * Generates a method as entry point for execution a data app and attaches the code to a given class.
     * The generated method is bound to REST-API and accepts POST-Request on URL /process.
     * Expected input is an InputScope and return Value is OutputScope.
     *
     * @param pckClass java class to which the generated code is attached
     */
    private fun generateDataAppLogicFun(pckClass: PackageClass, dataApp: DataApp): Boolean {
        var processMethod = when (dataAppType) {
            DataAppTypes.CLI -> {
                generateDataAppBlockFun(
                    pckClass,
                    "process",
                    listOf(
                        Pair(virtualMachine.newType("${String::class.java.simpleName}"), "inputString"),
                        Pair(virtualMachine.newType("${UUID::class.java.simpleName}"), "sessionId")
                    ),
                    virtualMachine.newType(String::class.java.simpleName)!!,
                    Access.PUBLIC,
                    false
                )
            }
            DataAppTypes.HTTP, DataAppTypes.IDS_HTTP -> {
                // add imports
                imports.add(RequestMapping::class.java.canonicalName)
                imports.add(ResponseBody::class.java.canonicalName)
                imports.add(RequestMethod::class.java.canonicalName)
                imports.add(RequestBody::class.java.canonicalName)
                imports.add(Operation::class.java.canonicalName)
                imports.add(ApiResponse::class.java.canonicalName)
                imports.add(Content::class.java.canonicalName)
                imports.add(Schema::class.java.canonicalName)
                imports.add(ExampleObject::class.java.canonicalName)
                imports.add(CrossOrigin::class.java.canonicalName)

                // this function is the rest function which starts executors
                val method = generateDataAppBlockFun(
                    pckClass,
                    "start",
                    listOf(
                        Pair(
                            virtualMachine.newType("@RequestBody ${String::class.java.simpleName}"),
                            "inputString"
                        )// FIXME dirty hack for annotation
                    ),
                    virtualMachine.newType(String::class.java.simpleName)!!,
                    Access.PUBLIC,
                    false
                )
                // add RequestMapping and ResponseBody annotations
                var corsAnnotation = method.addAnnotation("CrossOrigin")
                corsAnnotation.addAnnotationAttribute("origins", virtualMachine.newString("*"))
                var mappingAnnotation = method.addAnnotation("RequestMapping")
                mappingAnnotation.addAnnotationAttribute("path", virtualMachine.newString(retrieveUrl(dataApp)))
                mappingAnnotation.addAnnotationAttribute("method", virtualMachine.newVar("RequestMethod.POST"))
                mappingAnnotation.addAnnotationAttribute("produces", virtualMachine.newString("application/json"))
                method.addAnnotation("ResponseBody")

                // add swagger annotations
                var swaggerAnnotation = method.addAnnotation("Operation")
                var summary = when (executionType) {
                    ExecutionTypes.SINGLE -> "Start a single execution of the application logic."
                    ExecutionTypes.PERIODIC -> "Start a periodic execution of the application logic."
                }
                swaggerAnnotation.addAnnotationAttribute("summary", virtualMachine.newString(summary))
                var description = when (executionType) {
                    ExecutionTypes.SINGLE -> "A single execution of the data app's application logic is started."
                    ExecutionTypes.PERIODIC -> "A periodic execution of the data app's application logic is started."

                } + " A UUID which is used to identify the execution will be returned."
                swaggerAnnotation.addAnnotationAttribute("description", virtualMachine.newString(description))
                swaggerAnnotation.addAnnotationAttribute("tags", virtualMachine.newString("Data App control"))
                var responseArray = virtualMachine.newFree(
                    """
                    {
                        @ApiResponse(
                            responseCode = "200",
                            description = "Returns a JSON-object which contains a nukleus UUID-instance with key 'identifier'.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject("{\n" +
                                        "\t\"identifier\": \"{\\\"UUID\\\":\\\"3bee2e4d-8a97-496f-8cba-2d383306f55c\\\"}\"\n" +
                                    "}")
                            )
                        ),
                        @ApiResponse(
                                responseCode = "500",
                                description = "Returns an error if accessing the endpoint failed because of a not data app specific error.",
                                content = @Content(
                                            mediaType = "application/text",
                                            examples = @ExampleObject(
                                                    "<html>\n" +
                                                    "\t<body>\n" +
                                                    "\t\t<h1>Whitelabel Error Page</h1>\n" + 
                                                    "\t\t<p>This application has no explicit mapping for /error, so you are seeing this as a fallback.</p>\n" + 
                                                    "\t\t<div id='created'>Mon Feb 01 16:52:17 CET 2021</div>\n" + 
                                                    "\t\t<div>There was an unexpected error (type=Server Error, status=500).</div>\n" +
                                                    "\t\t<div></div>\n" + 
                                                    "\t</body>\n" + 
                                                    "</html>"
                                             )
                                         )
                                )
                    }
                """.trimIndent()
                )
                swaggerAnnotation.addAnnotationAttribute("responses", responseArray)

                var requestBody = virtualMachine.newFree(
                    """
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "A JSON-object which contains serialized nukleus-instances which will be used as input for the Data App.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(
                                    "${StringEscapeUtils.escapeJava(buildExampleInputScopeString())}"
                                )
                        )
                    )
                """.trimIndent()
                )
                swaggerAnnotation.addAnnotationAttribute("requestBody", requestBody)

                /* BEGIN
                This is somewhat of a code duplicate from generateSpringMainFunction for cli apps... */
                // setup and start executor
                method.newStmt(virtualMachine.newFree("Executor executor = new Executor()"))
                method.newStmt(
                    virtualMachine.newInvoke("executor", "trySetInputs")
                        .addVariableArg("inputString")
                )
                method.newStmt(
                    virtualMachine.newInvoke("executor", "setDataApp")
                        .addVariableArg("($dataAppName) ctx.getBean(\"$dataAppName\")")
                )
                method.newStmt(virtualMachine.newFree("Thread executorThread = new Thread(executor)"))
                method.newStmt(virtualMachine.newInvoke("executorThread", "start"))

                method.newStmt(virtualMachine.newFree("UUID uuid = UUID.randomUUID()"))
                method.newStmt(virtualMachine.newInvoke("logInfo")
                    .addVariableArg("\"Started executor with uuid '\" + uuid.toString() + \"'.\""))
                method.newStmt(virtualMachine.newFree("currentExecutions.put(uuid, executor)"))

                method.newStmt(virtualMachine.newFree("OutputScope uuidOutputScope = createUuidOutputScope(uuid.toString())"))
                method.newReturn().expression = virtualMachine.newInvoke("uuidOutputScope.toJson")

                /* END */

                // this function is used to retrieve execution results
                val retrieveMethod = generateDataAppBlockFun(
                    pckClass,
                    "result",
                    listOf(
                        Pair(
                            virtualMachine.newType("@RequestBody ${String::class.java.simpleName}"),
                            "inputString"
                        )// FIXME dirty hack for annotation
                    ),
                    virtualMachine.newType(String::class.java.simpleName)!!,
                    Access.PUBLIC,
                    false
                )
                // add RequestMapping and ResponseBody annotations
                corsAnnotation = retrieveMethod.addAnnotation("CrossOrigin")
                corsAnnotation.addAnnotationAttribute("origins", virtualMachine.newString("*"))
                mappingAnnotation = retrieveMethod.addAnnotation("RequestMapping")
                mappingAnnotation.addAnnotationAttribute(
                    "path",
                    virtualMachine.newString("${retrieveUrl(dataApp)}Result")
                )
                mappingAnnotation.addAnnotationAttribute("method", virtualMachine.newVar("RequestMethod.POST"))
                retrieveMethod.addAnnotation("ResponseBody")

                // add swagger annotations
                swaggerAnnotation = retrieveMethod.addAnnotation("Operation")
                summary = when (executionType) {
                    ExecutionTypes.SINGLE -> "Request the result of a single execution of the application logic."
                    ExecutionTypes.PERIODIC -> "Request the result of a periodic execution of the application logic."
                }
                swaggerAnnotation.addAnnotationAttribute("summary", virtualMachine.newString(summary))
                description = when (executionType) {
                    ExecutionTypes.SINGLE -> "After a single execution of the data app's application logic finished, the result can be retrieved with this endpoint."
                    ExecutionTypes.PERIODIC -> "After a periodic execution of the data app's application logic finished, the result can be retrieved with this endpoint."

                } + " It is mandatory to provide the UUID which identifies the execution. The endpoint will wait for 2 seconds before a time out and an error is returned."
                swaggerAnnotation.addAnnotationAttribute("description", virtualMachine.newString(description))
                swaggerAnnotation.addAnnotationAttribute("tags", virtualMachine.newString("Data App control"))
                // TODO: As soon as there are fix information about the return values of the data app, we can build a more meaningful example
                responseArray = virtualMachine.newFree(
                    """
                    {
                        @ApiResponse(
                            responseCode = "200",
                            description = "Returns a JSON-object which contains serialized nukleus-instances representing the execution result or a JSON-object which contains a single serialized nukleus error-instance in case there was an error during the execution.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                        @ExampleObject(
                                            name = "success",
                                            description = "If the execution was successful, the resulting serialized nukleus-instances are stored in the returned JSON-object.",
                                            value = "{\n\t\"result\": \"{\\\"UnsignedInt\\\":\\\"42\\\"}\"\n}"
                                        ),
                                        @ExampleObject(
                                            name = "error",
                                            description = "In case a data app related error occurred during the execution, the returned JSON-object contains a single serialized nukleus error-instance, describing the error.",
                                            value = "{\n\t\"error\": \"{\\\"Error\\\":\\\"Execution aborted because of missing input data.\\\"}\"\n}"
                                        )
                                    }
                            )
                        ),
                        @ApiResponse(
                                responseCode = "500",
                                description = "Returns an error if accessing the endpoint failed because of a not data app specific error.",
                                content = @Content(
                                            mediaType = "application/text",
                                            examples = @ExampleObject(
                                                    "<html>\n" +
                                                    "\t<body>\n" +
                                                    "\t\t<h1>Whitelabel Error Page</h1>\n" + 
                                                    "\t\t<p>This application has no explicit mapping for /error, so you are seeing this as a fallback.</p>\n" + 
                                                    "\t\t<div id='created'>Mon Feb 01 16:52:17 CET 2021</div>\n" + 
                                                    "\t\t<div>There was an unexpected error (type=Server Error, status=500).</div>\n" +
                                                    "\t\t<div></div>\n" + 
                                                    "\t</body>\n" + 
                                                    "</html>"
                                             )
                                         )
                                )
                    }
                """.trimIndent()
                )
                swaggerAnnotation.addAnnotationAttribute("responses", responseArray)

                requestBody = virtualMachine.newFree(
                    """
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "A JSON-object which contains serialized nukleus-instances which will be used as input for the Data App.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(
                                    "{\n\t\"identifier\": \"{\\\"UUID\\\":\\\"3bee2e4d-8a97-496f-8cba-2d383306f55c\\\"}\"\n}"
                                )
                        )
                    )
                """.trimIndent()
                )
                swaggerAnnotation.addAnnotationAttribute("requestBody", requestBody)

                retrieveMethod.newStmt(
                    virtualMachine.newFree(
                        "InputScope input = new InputScope();\n" +
                                "input.fromJson(inputString);\n" +
                                "try {\n" +
                                "    if (!input.getValues().containsKey(\"identifier\")) {\n" +
                                "        logError(\"Missing required input parameter 'identifier' of type 'core.UUID'.\");\n" +
                                "        throw new DegreeMissingInputException(\"Missing required input parameter 'identifier' of type 'core.UUID'.\");\n" +
                                "    }\n" +
                                "} catch (Exception e) {\n" +
                                "    OutputScope errorOutputScope = createErrorOutputScope(\"Because of missing input parameters the execution was aborted. Message: \" + e.getMessage());\n" +
                                "    logError(\"Because of missing input parameters the execution was aborted.\", e);\n" +
                                "    return errorOutputScope.toJson();\n" +
                                "}\n" +
                                "UUID identifier = null;\n" +
                                "try {\n" +
                                "    identifier = UUID.fromString(input.getValues().get(\"identifier\").read());\n" +
                                "} catch (IllegalArgumentException e) {\n" +
                                "    return createErrorOutputScope(\"No valid execution identifier '\" + identifier.toString() + \"'.\").toJson();\n" +
                                "}\n" +
                                "logInfo(\"Try to get result for executor with id '\" + identifier.toString() + \"'.\");" +
                                "if (!currentExecutions.containsKey(identifier)) {\n" +
                                "    OutputScope errorOutputScope = createErrorOutputScope(\"No execution with identifier '\" + identifier.toString() + \"' known.\");\n" +
                                "    logError(\"No execution with identifier '\" + identifier.toString() + \"' known.\");\n" +
                                "    return errorOutputScope.toJson();\n" +
                                "}\n" +
                                "long queryStart = System.currentTimeMillis();\n" +
                                "while (System.currentTimeMillis() - queryStart <= QUERY_TIMEOUT) {\n" +
                                "    if (!currentExecutions.get(identifier).isOutputReady()) {\n" +
                                "        try {\n" +
                                "            Thread.sleep(SLEEP_INTERVAL);\n" +
                                "        } catch (InterruptedException e) {\n" +
                                "            OutputScope errorOutputScope = createErrorOutputScope(\"Error while waiting for execution results. Message: \" + e.getMessage());\n" +
                                "            logError(\"Error while waiting for execution results.\", e);\n" +
                                "            System.out.println(errorOutputScope.toJson());\n" +
                                "        }\n" +
                                "    } else {\n" +
                                "        String result = currentExecutions.get(identifier).tryRetrieveOutputs(currentExecutions.get(identifier).getSessionId());\n" +
                                "        currentExecutions.remove(identifier);\n" +
                                "        return result;\n" +
                                "    }\n" +
                                "}\n" +
                                "logInfo(\"Execution for given identifier '\" + identifier.toString() + \"' has not yet finished. Try again later.\");" +
                                "return createErrorOutputScope(\"Execution for given identifier '\" + identifier.toString() + \"' has not yet finished. Try again later.\").toJson()"
                    )
                )

                // this is the actual processing function
                generateDataAppBlockFun(
                    pckClass,
                    "process",
                    listOf(
                        Pair(virtualMachine.newType(String::class.java.simpleName), "inputString"),
                        Pair(virtualMachine.newType(UUID::class.java.simpleName), "sessionId")
                    ),
                    virtualMachine.newType(String::class.java.simpleName)!!,
                    Access.PUBLIC,
                    false
                )
            }
        } as net.sourceforge.jenesis4java.Block
        // update container
        processMethodContainer = processMethod
        // parse the input parameters
        processMethod.newStmt(virtualMachine.newFree("InputScope input = new InputScope()"))
        // try to deserialize the input params
        processMethod.newStmt(virtualMachine.newFree("input.fromJson(inputString)"))

        // we need to generate different code for different execution behaviours
        when (executionType) {
            ExecutionTypes.SINGLE -> {
                // do nothing since single is the default style
            }
            ExecutionTypes.PERIODIC -> {
                // we may need to generate a stop function
                generateStopPeriodicFun(pckClass, dataApp)

                // synchronize the loop access in order to prevent race conditions
                // NOTE: The lock origin is based on the execution type
                processMethod.newStmt(virtualMachine.newFree("locks.put(sessionId, new Object())"))
                processMethod.newStmt(virtualMachine.newFree("loops.put(sessionId, true)"))

                // we use a counter to indicate the number of iterations
                processMethod.newStmt(virtualMachine.newFree("int iterations = 0"))
                // we need a copy of the original process method in order to generate proper return statements
                val procMtd = processMethod
                // loop around the boolean flag
                processMethod =
                    processMethod.newWhile(virtualMachine.newFree("loops.get(sessionId)")) as net.sourceforge.jenesis4java.Block
                processMethodContainer = processMethod

                // generate return
                procMtd.newStmt(virtualMachine.newFree("OutputScope outputScope = new OutputScope()"))
                procMtd.newStmt(
                    virtualMachine.newInvoke("Instance returnInstance = $typeTaxonomyVar", "create")
                        .addVariableArg("new Identifier(\"core.Text\")")
                )
                procMtd.newStmt(
                    virtualMachine.newFree(
                        "returnInstance.write(" +
                                "\"Execution was stopped after \" + iterations + \" iterations.\")"
                    )
                )
                procMtd.newStmt(virtualMachine.newFree("outputScope.getValues().put(\"result\", returnInstance)"))
                procMtd.newReturn().expression = virtualMachine.newInvoke("outputScope.toJson")
            }
        }

        // TODO this try-catch is redundant to the one in process method but could not be solved otherwise
        // wrap all in try catch block to catch specific degree exceptions
        val processMethodTry = processMethod.newTry()
        // catch clauses
        val processMethodCatch =
            generateCatch(processMethodTry, "Because of missing input parameters the execution was aborted.")

        // we have to create returns for single execution and log messages for periodic
        when (executionType) {
            ExecutionTypes.SINGLE -> {
                processMethodCatch.newReturn().expression = virtualMachine.newInvoke("errorOutputScope.toJson")
            }
            ExecutionTypes.PERIODIC -> {
                processMethodCatch.newStmt(
                    virtualMachine.newInvoke("this", "logError").addVariableArg("errorOutputScope.toJson()")
                )
            }
        }

        dataApp.inputs.map { input ->
            if (!embedDataAppInputVariable(input.key, input.value, processMethodTry, dataApp)) {
                return false
            }
        }

        // we need to generate different code for different execution behaviours
        when (executionType) {
            ExecutionTypes.SINGLE -> {
                // do nothing since single is the default style
            }
            ExecutionTypes.PERIODIC -> {
                // increment iteration counter
                processMethod.newStmt(virtualMachine.newFree("logInfo(\"Starting iteration \" + ++iterations)"))
                // sleep for periodic time
                val sleepTry = processMethod.newTry()
                sleepTry.newStmt(
                    virtualMachine.newInvoke("Thread", "sleep").addVariableArg("Long.parseLong(this.getPeriodicTime())")
                )
                val sleepCatch = sleepTry.newCatch(virtualMachine.newType("InterruptedException"), "e")
                sleepCatch.newStmt(logError("Could not sleep between executions.", "e"))
            }
        }

        return true
    }

    private fun buildExampleInputScopeString(): String {
        val inputScope = InputScope()

        dataApp.inputs.map { input ->
            inputScope.add(
                input.key,
                TypeTaxonomy.getInstance().newInstance(Identifier.of(input.value.first.toString()))
            )
        }

        return inputScope.toPrettyJson()
    }

    private fun retrieveUrl(dataApp: DataApp): String {
        if (dataApp.configurationItems.containsKey(HttpDataApp.URL_KEY) && !dataApp.configurationItems[HttpDataApp.URL_KEY].isNullOrEmpty()) {
            return dataApp.configurationItems[HttpDataApp.URL_KEY]!!
        }
        return "process"
    }

    private fun generateStopPeriodicFun(pckClass: PackageClass, dataApp: DataApp) {
        when (dataAppType) {
            DataAppTypes.CLI -> {
                // do nothing since SIGTERM is the proper way to stop periodic CLI Data Apps
            }
            DataAppTypes.HTTP -> {
                // generate stop method with http endpoint
                val method = generateDataAppBlockFun(
                    pckClass,
                    "stop",
                    listOf(
                        Pair(virtualMachine.newType("@RequestBody " + String::class.java.simpleName), "inputString")
                    ),
                    virtualMachine.newType(String::class.java.simpleName),
                    Access.PUBLIC,
                    false
                )
                // add RequestMapping and ResponseBody annotations
                val corsAnnotation = method.addAnnotation("CrossOrigin")
                corsAnnotation.addAnnotationAttribute("origins", virtualMachine.newString("*"))
                val mappingAnnotation = method.addAnnotation("RequestMapping")
                mappingAnnotation.addAnnotationAttribute(
                    "path",
                    virtualMachine.newString("${retrieveUrl(dataApp)}Stop")
                )
                mappingAnnotation.addAnnotationAttribute("method", virtualMachine.newVar("RequestMethod.POST"))

                // add swagger annotations
                val swaggerAnnotation = method.addAnnotation("Operation")
                val summary = when (executionType) {
                    ExecutionTypes.SINGLE -> "Stop a single execution of the application logic"
                    ExecutionTypes.PERIODIC -> "Stop a periodic execution of the application logic"
                } + ", identified by given UUID."
                swaggerAnnotation.addAnnotationAttribute("summary", virtualMachine.newString(summary))
                val description = when (executionType) {
                    ExecutionTypes.SINGLE -> "Stop a currently running single execution of the data app's application logic"
                    ExecutionTypes.PERIODIC -> "Stop a currently running periodic execution of the data app's application logic"

                } + ", which is identified by given UUID."
                swaggerAnnotation.addAnnotationAttribute("description", virtualMachine.newString(description))
                swaggerAnnotation.addAnnotationAttribute("tags", virtualMachine.newString("Data App control"))
                val responseArray = virtualMachine.newFree(
                    """
                    {
                        @ApiResponse(
                            responseCode = "200",
                            description = "Returns a JSON-object which contains serialized nukleus-instances representing the execution status or an error message.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                        @ExampleObject(
                                            name = "success",
                                            description = "If the stop was issued successfully, the response contains information about the point when the execution will be stopped.",
                                            value = "{\n\t\"status\": \"{\\\"Text\\\":\\\"Execution with identifier '3bee2e4d-8a97-496f-8cba-2d383306f55c' will be stopped after the current itereation.\\\"}\"\n}"
                                        ),
                                        @ExampleObject(
                                            name = "error",
                                            description = "In case a data app related error occurred during the execution, the returned JSON-object contains a single serialized nukleus error-instance, describing the error.",
                                            value = "{\n\t\"error\": \"{\\\"Error\\\":\\\"No valid execution identifier '3bee2e4d-8a97-496f-8cba-2d383306f55c'.\\\"}\"\n}"
                                        )
                                    }
                            )
                        ),
                        @ApiResponse(
                                responseCode = "500",
                                description = "Returns an error if accessing the endpoint failed because of a not data app specific error.",
                                content = @Content(
                                            mediaType = "application/text",
                                            examples = @ExampleObject(
                                                    "<html>\n" +
                                                    "\t<body>\n" +
                                                    "\t\t<h1>Whitelabel Error Page</h1>\n" + 
                                                    "\t\t<p>This application has no explicit mapping for /error, so you are seeing this as a fallback.</p>\n" + 
                                                    "\t\t<div id='created'>Mon Feb 01 16:52:17 CET 2021</div>\n" + 
                                                    "\t\t<div>There was an unexpected error (type=Server Error, status=500).</div>\n" +
                                                    "\t\t<div></div>\n" + 
                                                    "\t</body>\n" + 
                                                    "</html>"
                                             )
                                         )
                                )
                    }
                """.trimIndent()
                )
                swaggerAnnotation.addAnnotationAttribute("responses", responseArray)

                method.newStmt(
                    virtualMachine.newFree(
                        "InputScope input = new InputScope();\n" +
                                "input.fromJson(inputString);\n" +
                                "try {\n" +
                                "    if (!input.getValues().containsKey(\"identifier\")) {\n" +
                                "        logError(\"Missing required input parameter 'identifier' of type 'core.UUID'.\");\n" +
                                "        throw new DegreeMissingInputException(\"Missing required input parameter 'identifier' of type 'core.UUID'.\");\n" +
                                "    }\n" +
                                "} catch (Exception e) {\n" +
                                "    OutputScope errorOutputScope = createErrorOutputScope(\"Because of missing input parameters the execution was aborted. Message: \" + e.getMessage());\n" +
                                "    logError(\"Because of missing input parameters the execution was aborted.\", e);\n" +
                                "    return errorOutputScope.toJson();\n" +
                                "}\n" +
                                "UUID identifier = null;\n" +
                                "try {\n" +
                                "    identifier = UUID.fromString(input.getValues().get(\"identifier\").read());\n" +
                                "} catch (IllegalArgumentException e) {\n" +
                                "    return createErrorOutputScope(\"No valid execution identifier '\" + identifier.toString() + \"'.\").toJson();\n" +
                                "}\n" +
                                "if (!currentExecutions.containsKey(identifier)) {\n" +
                                "    OutputScope errorOutputScope = createErrorOutputScope(\"No execution with identifier '\" + identifier.toString() + \"' known.\");\n" +
                                "    logError(\"No execution with identifier '\" + identifier.toString() + \"' known.\");\n" +
                                "    return errorOutputScope.toJson();\n" +
                                "}\n" +
                                "synchronized (locks.get(currentExecutions.get(identifier).getSessionId())) {\n" +
                                "   loops.put(currentExecutions.get(identifier).getSessionId(), false);\n" +
                                "}\n" +
                                "return createStatusOutputScope(\"Execution with identifier '\" + identifier.toString() + \"' will be stopped after the current itereation.\").toJson()"
                    )
                )
            }
        }
    }

    private fun generateCatch(
        processMethodTry: Try,
        errorReason: String,
        generateReturn: Boolean = false,
        generateSysOut: Boolean = false,
        exceptionType: String = "Exception"
    ): Catch {
        val processMethodCatch = processMethodTry.newCatch(virtualMachine.newType(exceptionType), "e")
        processMethodCatch.newStmt(virtualMachine.newFree("OutputScope errorOutputScope = createErrorOutputScope(\"$errorReason Message: \" + e.getMessage())"))
        processMethodCatch.newStmt(logError("$errorReason", "e"))
        if (generateReturn) {
            processMethodCatch.newReturn().expression = virtualMachine.newFree("errorOutputScope.toJson()")
        }
        if (generateSysOut) {
            processMethodCatch.newStmt(virtualMachine.newFree("System.out.println(errorOutputScope.toJson())"))
        }
        return processMethodCatch
    }

    /**
     * This function is used to create some boilerplate code to map data app input parameters to D° variables with
     * UUIDs.
     *
     * @param name human readable name of the parameter
     * @param type the fully qualified type of the parameter as string (e.g. core.Text)
     * @param processMethod the method to which the code will be added
     * @param node the currently processed note within the AST. Used for error messages
     * @return false if an error occurs, true otherwise
     */
    private fun embedDataAppInputVariable(
        name: String,
        type: Pair<QualifiedName, List<DefinitionFunction>>,
        processMethod: net.sourceforge.jenesis4java.Block,
        node: Node
    ): Boolean {
        // add required imports
        imports.add(List::class.java.canonicalName)
        imports.add(ArrayList::class.java.canonicalName)
        imports.add(UsageControlObject::class.java.canonicalName)
        imports.add(ConstraintInstance::class.java.canonicalName)
        imports.add(PolicyInstance::class.java.canonicalName)
        if (type.second.isNotEmpty()) {
            imports.add(CompositeInstance::class.java.canonicalName)
            imports.add(RuntimeDefinitionRegistry::class.java.canonicalName)
        }

        val varType = Identifier.of(type.first.toString())
        // check if the type is known
        if (!typeTaxonomy.contains(varType)) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Unknown type '$varType' referenced as input parameter.",
                    node.file,
                    node.position
                )
            )
            return false
        }
        // check if the actual input parameters contain the required value
        val ifStmt = processMethod.newIf(virtualMachine.newFree("!input.getValues().containsKey(\"$name\")"))
        val errorMsg = "Missing required input parameter '$name' of type '$varType'."
        ifStmt.newStmt(logError(errorMsg))
        ifStmt.newThrow(virtualMachine.newInvoke("new DegreeMissingInputException").addArg(errorMsg))

        // register the variable
        val uuid: UUID? = retrieveUuidForVariable(name, varType, dataApp, processMethod)
        // store the input parameter value in variable manager
        val setVarFunction = "initializeVariable"
        // initialize the variable in the data app
        processMethod.newStmt(
            virtualMachine.newInvoke("variableManager", setVarFunction)
                .addVariableArg("UUID.fromString(\"$uuid\")")
                .addVariableArg("input.getValues().get(\"$name\")") // FIXME there are better solutions for this
        )
        // store uuid and type for input parameter
        scopeAwareVariableToTypeMapList.last()[name] = varType!!
        scopeAwareVariableToUuidMapList.last()[name] = uuid!!

        // validate and process definition functions
        for (defFun in type.second) {
            // validation
            if (defFun.name != "policy") {
                compilerMessages.add(
                    CompilerMessage(
                        CompilerMessage.Kind.ERROR,
                        "Function '${defFun.name}' is not supported for data app inputs. Only the type " +
                                "'policy' is allowed.",
                        node.file,
                        node.position
                    )
                )
                return false
            }

            // processing
            val varUuid = uuid.toString().replace("-", "_")
            processMethod.newStmt(virtualMachine.newFree("List<Instance> dataPol_$varUuid = new ArrayList()"))

            for (polArg in defFun.arguments) {
                if (polArg !is StringLiteral) {
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.ERROR,
                            "Unsupported argument type '${polArg.javaClass.simpleName}' used for data " +
                                    "app input function. Only 'StringLiteral' is allowed.",
                            node.file,
                            node.position
                        )
                    )
                    return false
                }

                // ensure that the references policy is existing
                if (!runtimeDefinitionRegistry.contains(Identifier.of(polArg.value))) {
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.ERROR,
                            "No element with name '${polArg.value}' found, but used as policy for a data " +
                                    "app input.",
                            node.file,
                            node.position
                        )
                    )
                    return false
                }
                val instance = runtimeDefinitionRegistry.lookup(Identifier.of(polArg.value))
                if (instance.type.identifier.toString() != "degree.PolicyInstance" &&
                    instance.type.identifier.toString() != "degree.ConstraintInstance"
                ) {
                    compilerMessages.add(
                        CompilerMessage(
                            CompilerMessage.Kind.ERROR,
                            "No policy/constraint instance '${polArg.value}' found, but used as policy for a data " +
                                    "app input.",
                            node.file,
                            node.position
                        )
                    )
                    return false
                }

                // actual code generation for injecting the referenced policies
                if (instance is PolicyInstance)
                    compiler.addPolicyInstanceToExport(instance)
                else
                    compiler.addConstraintInstanceToExport(instance as ConstraintInstance)

                processMethod.newStmt(
                    virtualMachine.newFree(
                        "dataPol_$varUuid.add(RuntimeDefinitionRegistry.getInstance().lookup(Identifier.of(\"${polArg.value}\")))"
                    )
                )
            }
            // finally add the collected policies to usage control object for later usage
            processMethod.newStmt(
                virtualMachine.newFree(
                    "String identifier;\n" +
                            "Instance ins_$varUuid = variableManager.readVariable(UUID.fromString(\"$uuid\"));\n" +
                            "if (ins_$varUuid instanceof CompositeInstance)\n" +
                            "   identifier = ((CompositeInstance) ins_$varUuid).getIdentity().linkValue();\n" +
                            "else\n" +
                            "   identifier = System.identityHashCode(ins_$varUuid) + \"\";\n" +
                            "UsageControlObject.Companion.getInstance().getDataPolicies().put(identifier, dataPol_$varUuid);\n" +
                            "List<String> customIds_$varUuid;\n" +
                            "for (Instance dataPolIns : dataPol_$varUuid) { \n" +
                            "   if (((CompositeInstance) dataPolIns).getType().getIdentifier().toString().equals(\"degree.PolicyInstance\"))\n" +
                            "      customIds_$varUuid = SANDBOX.collectIds((PolicyInstance) dataPolIns);\n" +
                            "   else\n" +
                            "       customIds_$varUuid = SANDBOX.collectIds((ConstraintInstance) dataPolIns);\n" +
                            "   customIds_$varUuid.removeIf(String::isEmpty);\n" +
                            "   for (String customId : customIds_$varUuid) {\n" +
                            "       UsageControlObject.Companion.getInstance().addExternalIdentifierMappingEntry(customId, identifier);\n" +
                            "   }\n" +
                            "}"
                )
            )// an "unnecessary" ; will be generated here
        }

        return true
    }

    /**
     * This function is used to create some boilerplate code to map input parameters (e.g. for activities but not for
     * whole data apps) to D° variables with UUIDs.
     *
     * @param name human readable name of the parameter
     * @param type the fully qualified type of the parameter as string (e.g. core.Text)
     * @param processMethod the method to which the code will be added
     * @param node the currently processed note within the AST. Used for error messages
     * @return false if an error occurs, true otherwise
     */
    private fun embedInputVariable(
        name: String,
        type: String,
        processMethod: net.sourceforge.jenesis4java.Block,
        node: Node
    ): Boolean {
        val varType = Identifier.of(type)
        // check if the type is known
        if (!typeTaxonomy.contains(varType)) {
            compilerMessages.add(
                CompilerMessage(
                    CompilerMessage.Kind.ERROR,
                    "Unknown type '$varType' referenced as input parameter.",
                    node.file,
                    node.position
                )
            )
            return false
        }
        // check if the actual input parameters contain the required value
        val ifStmt = processMethod.newIf(virtualMachine.newFree("!input.getValues().containsKey(\"$name\")"))
        val errorMsg = "Missing required input parameter '$name' of type '$varType'."
        ifStmt.newStmt(logError(errorMsg))
        ifStmt.newThrow(virtualMachine.newInvoke("new DegreeMissingInputException").addArg(errorMsg))

        // register the variable
        val uuid: UUID? = retrieveUuidForVariable(name, varType, dataApp, processMethod)
        // store the input parameter value in variable manager
        val setVarFunction = "initializeVariable"
        // initialize the variable in the data app
        processMethod.newStmt(
            virtualMachine.newInvoke("variableManager", setVarFunction)
                .addVariableArg("UUID.fromString(\"$uuid\")")
                .addVariableArg("input.getValues().get(\"$name\")") // FIXME there are better solutions for this
        )
        // store uuid and type for input parameter
        scopeAwareVariableToTypeMapList.last()[name] = varType!!
        scopeAwareVariableToUuidMapList.last()[name] = uuid!!

        return true
    }

    /**
     * Generates a java class for a data app inside a given compilation unit.
     * The generated class will extend one of the classes in
     * de.fhg.isst.oe270.degree.runtime.java.data.app
     *
     * @param unit compilation unit which will contain the new class
     * @return newly generated java class to which all generated code of a data app will be attached
     */
    private fun createDataAppClass(unit: CompilationUnit): PackageClass {
        // register types that are always used
        compiler.addTypeToExportSystem(Identifier.of("core.Text"))
        compiler.addTypeToExportSystem(Identifier.of("core.Error"))
        val dataAppClass = unit.newPublicClass(dataAppName)

        val propertySource = dataAppClass.addAnnotation("PropertySource")
        propertySource.addAnnotationAttribute("value", virtualMachine.newString("classpath:application.properties"))
        val dependsOn = dataAppClass.addAnnotation("DependsOn")
        dependsOn.addAnnotationAttribute("value", virtualMachine.newString("springContext"))

        dataAppClass.extends = when (dataAppType) {
            DataAppTypes.CLI ->
                // find correct subclass
                when (executionType) {
                    ExecutionTypes.SINGLE -> {
                        imports.add(CliDataApp::class.java.canonicalName)
                        "CliDataApp"
                    }
                    ExecutionTypes.PERIODIC -> {
                        imports.add(CliDataAppPeriodic::class.java.canonicalName)
                        "CliDataAppPeriodic"
                    }
                }
            DataAppTypes.HTTP -> {
                generateOpenAPIBean(dataAppClass)
                // register the used type in the export type system
                compiler.addTypeToExportSystem(Identifier.of("core.UUID"))
                // find correct subclass
                when (executionType) {
                    ExecutionTypes.SINGLE -> {
                        imports.add(HttpDataApp::class.java.canonicalName)
                        "HttpDataApp"
                    }
                    ExecutionTypes.PERIODIC -> {
                        imports.add(HttpDataAppPeriodic::class.java.canonicalName)
                        "HttpDataAppPeriodic"
                    }
                }
            }
            DataAppTypes.IDS_HTTP -> {
                generateOpenAPIBean(dataAppClass)
                compiler.addTypeToExportSystem(Identifier.of("core.UUID"))

                compiler.javaTemplateProcessor!!.addInclude("infomodel.json")

                // find correct subclass
                when (executionType) {
                    ExecutionTypes.SINGLE -> {
                        imports.add(IdsHttpDataApp::class.java.canonicalName)
                        "IdsHttpDataApp"
                    }
                    ExecutionTypes.PERIODIC -> {
                        imports.add(IdsHttpDataAppPeriodic::class.java.canonicalName)
                        "IdsHttpDataAppPeriodic"
                    }
                }
            }
        }

        dataAppClass.setComment(
            Comment.DOCUMENTATION,
            """This Data App is generated by D°.
                    |
                    |D°-Compiler Version: ${compilerConfiguration.compilerVersion}
                    |Generation time: $generationTime
                """.trimMargin()
        )

        return dataAppClass
    }

    private fun generateOpenAPIBean(appClass: PackageClass) {
        imports.add(OpenAPI::class.java.canonicalName)
        imports.add(Bean::class.java.canonicalName)
        imports.add(Components::class.java.canonicalName)
        imports.add(SecurityScheme::class.java.canonicalName)
        imports.add(Info::class.java.canonicalName)

        val method = appClass.newMethod(virtualMachine.newType("OpenAPI"), "openAPI")
        method.addAnnotation("Bean")
        method.newStmt(
            virtualMachine.newFree(
                """
            return new OpenAPI()
				.components(new Components().addSecuritySchemes("basicScheme",
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
				.info(new Info().title("$dataAppName").version("$dataAppVersion").description(
						"This Data App is generated by D°. The D°-Compiler Version ${compilerConfiguration.compilerVersion} was used to generate this application at $generationTime.")
						)
        """.trimIndent()
            )
        )
    }

    /**
     * Adds an arbitrary list of imports to a java class.
     *
     * @param pckClass java class which will contain the import statements
     * @param imports list of imports which should be added to the class
     */
    private fun addImports(pckClass: PackageClass, imports: List<String>) {
        imports.map { pckClass.addImport(it) }
    }

    /**
     * Generates a java default-constructor and attaches it to a given class.
     * Constructor will be empty except for a call to super().
     *
     * @param pckClass java class which will contain the generated constructor
     */
    private fun generateDataAppInitMethod(pckClass: PackageClass) {
        // create configuration map as class member
        pckClass.newField(virtualMachine.newType("HashMap<String, String>"), "configuration")

        // create constructor and call superclass ones
        val initMethod = pckClass.newMethod("init")
        initMethod.access = Access.AccessType.PROTECTED
        initMethod.addAnnotation("PostConstruct")

        initMethod.newStmt(virtualMachine.newInvoke("super.init"))

        // add all items from data app configuration to configuration map
        dataApp.configurationItems.map { entry ->
            initMethod.newStmt(
                virtualMachine.newInvoke("CONFIGURATION_MAP", "put").addArg(entry.key).addArg(entry.value)
            )
        }

        // add all tags
        if ((dataApp.configurationItems[CliDataApp.TAGS_KEY] ?: error("")).isNotEmpty()) {
            (dataApp.configurationItems[CliDataApp.TAGS_KEY] ?: error("")).split(",").map { it.trim() }.map {
                initMethod.newStmt(virtualMachine.newInvoke("TAGS", "add").addArg(it))
            }
        }

        // build and set usage control object
        initMethod.newStmt(
            virtualMachine.newInvoke("this", "createUsageControlObject")
                .addArg(dataApp.configurationItems[CliDataApp.USAGE_CONTROL_OBJECT_TYPE_KEY] ?: error(""))
        )

        // after (!!) the initialization we can load the data app tags into the execution context
        val tagLoop = initMethod.newFor()
        tagLoop.addInit(virtualMachine.newFree("int i = 0"))
        tagLoop.predicate = virtualMachine.newFree("i < TAGS.size()")
        tagLoop.addUpdate(virtualMachine.newFree("i++"))
        tagLoop.newStmt(
            virtualMachine.newFree(
                "ExecutionContext.getInstance().getModule(\"TagsContextModule\")" +
                        ".addContextEntity(TAGS.get(i), new ReadOnlyEntity(TAGS.get(i), TAGS.get(i)))"
            )
        )

        // initialize the sandbox
        val initializationIf = initMethod.newIf(
            virtualMachine.newBinary(
                Binary.EQUAL_TO,
                virtualMachine.newInvoke(sandboxVar, "initialize").addVariableArg("CONFIGURATION_MAP"),
                virtualMachine.newFree("false")
            )
        )
        initializationIf.newStmt(logError("Error during the initialization of the Data App. Going to exit now."))
        initializationIf.newStmt(virtualMachine.newInvoke("System", "exit").addArg(-1))
    }

    /**
     * Generates java main-method as entry point of the data app.
     * The generated method will be attached to a given class.
     * The main-method will start the spring-context.
     *
     * @param pckClass the java class which will contain the main-method
     */
    private fun generateSpringMainFunction(pckClass: PackageClass) {
        // create main method
        val mainMethod = pckClass.newMethod(virtualMachine.newType(Type.VOID)!!, "main")
        mainMethod.access = Access.PUBLIC
        mainMethod.isStatic(true)
        mainMethod.addParameter(virtualMachine.newArray("String", 1), "args")

        // data app type dependent (starting) code
        when (dataAppType) {
            // CliDataApp
            DataAppTypes.CLI -> {
                // validate inputs
                mainMethod.newStmt(virtualMachine.newInvoke("validateInputs").addVariableArg("args"))
            }
        }
        // start spring application and store application context
        pckClass.newField(virtualMachine.newType("static ApplicationContext"), "ctx")
        val runSpring = virtualMachine.newInvoke("ctx = SpringApplication", "run")
        runSpring.addVariableArg("$dataAppName.class")
        runSpring.addVariableArg("args")
        mainMethod.newStmt(runSpring)

        // create data app instance
        //pckClass.newField(virtualMachine.newType("static " + pckClass.name), "dataApp")
        //mainMethod.newStmt(virtualMachine.newVar("dataApp = new ${pckClass.name}()"))

        /**
         * The code generated for CliDataApps takes place in the main method instead of the logic function.
         * This could be streamlined in the future.
         */
        // data app type dependent code
        when (dataAppType) {
            // CliDataApp
            DataAppTypes.CLI -> {
                // setup and start executor
                pckClass.newField(virtualMachine.newType("static Executor"), "executor")
                pckClass.newField(virtualMachine.newType("static Thread"), "executorThread")
                mainMethod.newStmt(virtualMachine.newFree("executor = new Executor()"))
                mainMethod.newStmt(
                    virtualMachine.newInvoke("executor", "trySetInputs")
                        .addVariableArg("args[0]")
                )
                mainMethod.newStmt(
                    virtualMachine.newInvoke("executor", "setDataApp")
                        .addVariableArg("($dataAppName) ctx.getBean(\"$dataAppName\")")
                )
                mainMethod.newStmt(virtualMachine.newFree("executorThread = new Thread(executor)"))
                mainMethod.newStmt(virtualMachine.newInvoke("executorThread", "start"))

                // build try/catch for Thread.sleep(...)
                val waitTry = mainMethod.newTry()
                generateCatch(
                    waitTry,
                    "Error while waiting for execution results.",
                    generateReturn = false,
                    generateSysOut = true,
                    exceptionType = "InterruptedException"
                )

                // wait for result
                val execWhile = waitTry.newWhile(virtualMachine.newFree("!executor.isOutputReady()"))
                execWhile.newStmt(virtualMachine.newInvoke("Thread", "sleep").addVariableArg("SLEEP_INTERVAL"))

                // print result and exit
                mainMethod.newStmt(logInfo("Execution finished. Result:"))
                mainMethod.newStmt(virtualMachine.newFree("System.out.println(executor.tryRetrieveOutputs(executor.getSessionId()))"))
                // exit spring application
                mainMethod.newStmt(virtualMachine.newInvoke("SpringApplication", "exit").addVariableArg("ctx"))
            }
        }
    }

    @Suppress("unused")
    private fun logInfo(msg: String): Invoke? {
        return virtualMachine.newInvoke("logInfo").addArg(msg)
    }

    @Suppress("unused")
    private fun logInfo(msg: String, t: String): Invoke? {
        return virtualMachine.newInvoke("logInfo").addArg(msg).addVariableArg(t)
    }

    @Suppress("unused")
    private fun logError(msg: String): Invoke? {
        return virtualMachine.newInvoke("logError").addArg(msg)
    }

    @Suppress("SameParameterValue", "unused")
    private fun logError(msg: String, t: String): Invoke? {
        return virtualMachine.newInvoke("logError").addArg(msg).addVariableArg(t)
    }

    /**
     * Transforms a UUID into a valid java identifier by replacing dashes with underscores.
     *
     * @param uuid uuid which will be converted into a valid java identifier
     * @return string which contains a valid java identifier
     */
    private fun uuidToJavaIdentifier(uuid: UUID): String {
        return uuid.toString().replace('-', '_')
    }

    /**
     * Generate a unique UUID.
     *
     * @return a UUID which is unique for the whole compiler
     */
    private fun createFreeUuid(): UUID {
        var result = UUID.randomUUID()

        while (usedUuids.contains(result)) {
            result = UUID.randomUUID()
        }
        usedUuids.add(result)

        return result
    }

}

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
package de.fhg.isst.oe270.degree.remote.processing.controller

import de.fhg.isst.oe270.degree.remote.processing.DataAppState
import de.fhg.isst.oe270.degree.remote.processing.communication.*
import de.fhg.isst.oe270.degree.remote.processing.communication.requests.*
import de.fhg.isst.oe270.degree.remote.processing.communication.response.*
import io.swagger.annotations.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import springfox.documentation.builders.*
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*
import javax.annotation.PostConstruct
import org.springframework.web.bind.annotation.RequestMapping



@RestController
@RequestMapping(value = ["/remoteProcessing"])
@EnableSwagger2
class RemoteProcessingController {

    private val logger = LoggerFactory.getLogger("RemoteProcessingController")!!

    @Autowired
    lateinit var ioController: IoController

    @Autowired
    lateinit var gitController: GitController

    @Autowired
    lateinit var compileController: CompileController

    @Autowired
    lateinit var dockerController: DockerController

    @Autowired
    lateinit var dispatchController: DispatchController

    @Bean
    fun remoteProcessingApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                    .apis(RequestHandlerSelectors.any())
                    .paths(PathSelectors.regex("/remoteProcessing/.*"))
                    .build()
                .pathMapping("/")
                .apiInfo(
                        ApiInfoBuilder()
                                .title("D° Remote Processing")
                                .description("This Api provides remote processing functionality for D°.")
                                .version("v1.0.0")
                                .build()
                )
    }

    @PostConstruct
    fun init() {
        ioController.writeStartupLogEntry()
    }

    @ApiOperation(
            value = "Registers a new Data App which is stored in a git repository",
            notes = "Will assign a UUID to this Data App and perform an initial clone on the repository."
    )
    @PostMapping(
            value = ["/register/git"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun registerGitRepository(
            @ApiParam(
                    value = "JSON representation of a RegisterRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"gitRepositoryUrl\" : \"https://github.com/my/dataapp/app.git\"," +
                            "\"username\" : \"...\"," +
                            "\"password\" : \"...\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received register/git request.")
        // create the response
        val response = RegisterResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<RegisterGitRequest>(json, statuscode)

        // register the data app in file system
        try {
            val uuid = ioController.registerDataApp(request!!.toJson())
            response.setUUID(uuid)
        } catch (e: Exception) {
            statuscode.setBit(statuscode.ioError)
            if (e.message != null) {
                statuscode.appendError(e.message!!)
            }
        }

        // clone the repository
        statuscodeAwareGitClone(request, response, statuscode, request!!.getRepositoryURL())
        // remove git metadata
        statuscodeAwareRemoveRepositoryMetadata(response.getUUID(), statuscode)
        // update data app status
        ioController.setDataAppStateByUuid(response.getUUID(), DataAppState.REGISTERED)
        // push changes to remoteProcessing repository
        statuscodeAwareCommitPushGit("Cloned the newly registered Data App and assigned UUID " +
                "'${response.getUUID()}'.", statuscode)

        if (statuscode.getStatuscode() != 0L) {
            response.setUUID(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        }

        logger.info("""
            |Finished register/git request.
            |UUID of newly registered Data App: ${response.getUUID()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    @ApiOperation(
            value = "Updates a Data App which is stored in a git repository",
            notes = "Affected Data App is identified by given UUID. Will update the stored git commit to the most recent one."
    )
    @PostMapping(
            value = ["/update/git"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun updateDataApp(
            @ApiParam(
                    value = "JSON representation of an UpdateRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"UUID\" : \"8bc8ef59-33c1-463d-b8a8-0e914a2b3afe\"," +
                            "\"username\" : \"...\"," +
                            "\"password\" : \"...\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received update/git request.")
        // create the response
        val response = RegisterResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<UpdateRequest>(json, statuscode)
        response.setUUID(request!!.getUUID())

        // state check
        if (setOf(DataAppState.REGISTERED, DataAppState.UPDATED, DataAppState.COMPILED, DataAppState.COMPILATION_ERROR,
                        DataAppState.DEPLOYED, DataAppState.DEPLOYMENT_ERROR, DataAppState.TERMINATED, DataAppState.UNKNOWN_STATE)
                        .contains(ioController.getDataAppStateByUuid(response.getUUID()))) {
            // update data app status
            ioController.setDataAppStateByUuid(response.getUUID(), DataAppState.UPDATED)
            // remove old repository
            statuscodeAwareRemoveGit(request, statuscode)
            // remove docker artifacts
            removeDockerElementsByUuid(response.getUUID())

            // load original registration message
            val originRequest = try {
                ioController.loadGitRegisterRequest(request.getUUID())
            } catch (e: Exception) {
                statuscode.setBit(statuscode.ioError)
                if (e.message != null) {
                    statuscode.appendError(e.message!!)
                }
                null
            }

            // clone the repository
            statuscodeAwareGitClone(request, response, statuscode, originRequest!!.getRepositoryURL())
            // remove git metadata
            statuscodeAwareRemoveRepositoryMetadata(response.getUUID(), statuscode)
            // create a corresponding log entry
            if(statuscode.isSuccess()) {
                ioController.writeDataAppUpdatedSuccessLogEntry(response.getUUID(), false)
            } else {
                ioController.writeDataAppUpdatedFailureLogEntry(response.getUUID(), statuscode.createMessage(), false)
            }
            // push changes to remoteProcessing repository
            statuscodeAwareCommitPushGit("Updated Data App with UUID ${request.getUUID()}.", statuscode)
        } else {
            statuscode.setBit(statuscode.updateStateError)
        }

        logger.info("""
            |Finished update/git request.
            |UUID of updated Data App: ${response.getUUID()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    @ApiOperation(
            value = "Gets current status of a Data App",
            notes = "Affected Data App is identified by given UUID. Load and return the current status of a Data App."
    )
    @PostMapping(
            value = ["/status"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun retrieveDataAppStatus(
            @ApiParam(
                    value = "JSON representation of a StatusRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"UUID\" : \"8bc8ef59-33c1-463d-b8a8-0e914a2b3afe\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received status request.")
        // create the response
        val response = StatusResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<StatusRequest>(json, statuscode)
        response.setUUID(request!!.getUUID())
        // get status of data app
        response.setDataAppState(ioController.getDataAppStateByUuid(response.getUUID()))

        logger.info("""
            |Finished status request.
            |UUID of Data App: ${response.getUUID()}.
            |Status of Data App: ${response.getDataAppState()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    @ApiOperation(
            value = "Deletes a Data App",
            notes = "Affected Data App is identified by given UUID. Delete Data App and all assigned resources. " +
                    "This will mark the UUID as unusable for the future."
    )
    @PostMapping(
            value = ["/delete"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun deleteDataApp(
            @ApiParam(
                    value = "JSON representation of a DeleteRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"UUID\" : \"c7a8537e-26e7-47b7-8a43-2b5905c9637f\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received delete request.")
        // create the response
        val response = DeleteResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<DeleteRequest>(json, statuscode)
        response.setUUID(request!!.getUUID())

        // state check
        if (setOf(DataAppState.REGISTERED, DataAppState.UPDATED, DataAppState.COMPILED, DataAppState.COMPILATION_ERROR,
                        DataAppState.DEPLOYED, DataAppState.DEPLOYMENT_ERROR, DataAppState.TERMINATED, DataAppState.UNKNOWN_STATE)
                        .contains(ioController.getDataAppStateByUuid(response.getUUID()))) {
            // delete all data
            // remove the cloned git
            statuscodeAwareRemoveGit(response, statuscode)
            // remove management data
            statuscodeAwareRemoveManagementDir(response, statuscode)
            // remove docker artifacts
            removeDockerElementsByUuid(response.getUUID())
            // update data app status
            ioController.setDataAppStateByUuid(response.getUUID(), DataAppState.DELETED)
            // release the port
            ioController.releasePort(response.getUUID())
            // update git
            statuscodeAwareCommitPushGit("Deleted Data App with UUID ${request.getUUID()}", statuscode, true)

            ioController.freeUuid(request.getUUID())

            // create a corresponding log entry
            if(statuscode.isSuccess()) {
                ioController.writeDataAppDeletedSuccessLogEntry(response.getUUID(), false)
            } else {
                ioController.writeDataAppDeletedFailureLogEntry(response.getUUID(), statuscode.createMessage(), false)
            }
        } else {
            statuscode.setBit(statuscode.deleteStateError)
        }

        logger.info("""
            |Finished delete request.
            |UUID of deleted Data App: ${response.getUUID()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    @ApiOperation(
            value = "Deploys a Data App",
            notes = "Affected Data App is identified by given UUID. Creates a docker image for the Data App. The container " +
                    "will be created on the docker machine used by the Remote Processing."
    )
    @PostMapping(
            value = ["/deploy"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun deployDataApp(
            @ApiParam(
                    value = "JSON representation of a DeployRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"UUID\" : \"8bc8ef59-33c1-463d-b8a8-0e914a2b3afe\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received deploy request.")
        // create the response
        val response = DeployResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<DeployRequest>(json, statuscode)
        response.setUUID(request!!.getUUID())

        if (setOf(DataAppState.COMPILED, DataAppState.TERMINATED).contains(ioController.getDataAppStateByUuid(response.getUUID()))) {
            // build the docker container
            dockerController.deployDataApp(response.getUUID())
            statuscodeAwareCommitPushGit("Deployed Data App ${response.getUUID()}.", statuscode)
        } else {
            statuscode.setBit(statuscode.deployStateError)
        }

        // create a corresponding log entry
        if(statuscode.isSuccess()) {
            ioController.writeDataAppDeployedSuccessLogEntry(response.getUUID(), false)
        } else {
            ioController.writeDataAppDeployedFailureLogEntry(response.getUUID(), statuscode.createMessage(), false)
        }

        logger.info("""
            |Finished deploy request.
            |UUID of deployed Data App: ${response.getUUID()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    @ApiOperation(
            value = "Compiles a Data App",
            notes = "Affected Data App is identified by given UUID. Start the compilation of the program code and affected" +
                    "resources with the D° compiler."
    )
    @ApiResponse(
            code = 200,
            message = "JSON representation of a CompileResponse used by the RemoteProcessingController."
    )
    @PostMapping(
            value = ["/compile"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun compileDataApp(
            @ApiParam(
                    value = "JSON representation of a CompileRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"UUID\" : \"8bc8ef59-33c1-463d-b8a8-0e914a2b3afe\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received compile request.")
        // create the response
        val response = CompileResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<CompileRequest>(json, statuscode)
        response.setUUID(request!!.getUUID())

        if (setOf(DataAppState.REGISTERED, DataAppState.UPDATED, DataAppState.COMPILED,
                        DataAppState.COMPILATION_ERROR, DataAppState.DEPLOYED, DataAppState.DEPLOYMENT_ERROR,
                        DataAppState.TERMINATED)
                        .contains(ioController.getDataAppStateByUuid(response.getUUID()))) {
            // remove docker artifacts
            removeDockerElementsByUuid(response.getUUID())
            // compile
            compileController.compileByUuid(response.getUUID())
        } else {
            statuscode.setBit(statuscode.compileStateError)
        }

        // create a corresponding log entry
        if(statuscode.isSuccess()) {
            ioController.writeDataAppCompileSuccessLogEntry(response.getUUID(), false)
        } else {
            ioController.writeDataAppCompileFailureLogEntry(response.getUUID(), statuscode.createMessage(), false)
        }

        logger.info("""
            |Finished compile request.
            |UUID of compiled Data App: ${response.getUUID()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    @ApiOperation(
            value = "Starts a Data App",
            notes = "Affected Data App is identified by given UUID. Creates a docker container from an already built image" +
                    "and starts it."
    )
    @PostMapping(
            value = ["/start"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun startDataApp(
            @ApiParam(
                    value = "JSON representation of a StartRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"UUID\" : \"8bc8ef59-33c1-463d-b8a8-0e914a2b3afe\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received start request.")
        // create the response
        val response = StartResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<StartRequest>(json, statuscode)
        response.setUUID(request!!.getUUID())

        if (setOf(DataAppState.DEPLOYED, DataAppState.TERMINATED).contains(ioController.getDataAppStateByUuid(response.getUUID()))) {
            // start
            dockerController.startDataApp(response.getUUID())
        } else {
            statuscode.setBit(statuscode.startStateError)
        }

        // create a corresponding log entry
        if(statuscode.isSuccess()) {
            ioController.writeDataAppStartSuccessLogEntry(response.getUUID(), false)
        } else {
            ioController.writeDataAppStartFailureLogEntry(response.getUUID(), statuscode.createMessage(), false)
        }

        logger.info("""
            |Finished start request.
            |UUID of started Data App: ${response.getUUID()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    @ApiOperation(
            value = "Dispatches message to Data App",
            notes = "Affected Data App is identified by given UUID. Dispatch the given message to the running Data App."
    )
    @PostMapping(
            value = ["/dispatch"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun dispatchToDataApp(
            @ApiParam(
                    value = "JSON representation of a DispatchRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"UUID\" : \"8bc8ef59-33c1-463d-b8a8-0e914a2b3afe\"," +
                            "\"payload\" : \"{\\\"payload\\\": \\\"{\\\\\\\"Text\\\\\\\":\\\\\\\"Hello World.\\\\\\\"}\\\"}\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received dispatch request.")
        // create the response
        val response = DispatchResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<DispatchRequest>(json, statuscode)
        response.setUUID(request!!.getUUID())

        if (ioController.getDataAppStateByUuid(response.getUUID()).equals(DataAppState.RUNNING)) {
            val dataAppResponse = dispatchController.dispatchToHttpDataApp(response.getUUID(), request.getPayload())
            response.setPayload(dataAppResponse)
        } else {
            statuscode.setBit(statuscode.dispatchStateError)
        }

        // create a corresponding log entry
        if(statuscode.isSuccess()) {
            ioController.writeDataAppStopSuccessLogEntry(response.getUUID(), false)
        } else {
            ioController.writeDataAppStopFailureLogEntry(response.getUUID(), statuscode.createMessage(), false)
        }

        logger.info("""
            |Finished dispatch request.
            |UUID of target Data App: ${response.getUUID()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    @ApiOperation(
            value = "Stops a Data App",
            notes = "Affected Data App is identified by given UUID. Dispatch the given message to the running Data App."
    )
    @PostMapping(
            value = ["/stop"],
            consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    fun stopDataApp(
            @ApiParam(
                    value = "JSON representation of a StopRequest used by the RemoteProcessingController.",
                    required = true,
                    readOnly = true,
                    examples =  Example(ExampleProperty(mediaType="application/json", value = "{" +
                            "\"timestamp\" : \"2019-10-18T07:37:34.437Z\"," +
                            "\"UUID\" : \"8bc8ef59-33c1-463d-b8a8-0e914a2b3afe\"" +
                            "}"))
            )
            @RequestBody json : String) : String {
        logger.info("Received stop request.")
        // create the response
        val response = StopResponse()
        val statuscode = Statuscode()

        // parse the request
        val request = statuscodeAwareRequestParsing<StopRequest>(json, statuscode)
        response.setUUID(request!!.getUUID())

        if (ioController.getDataAppStateByUuid(response.getUUID()) == DataAppState.RUNNING) {
            // stop
            dockerController.stopDataApp(response.getUUID())
        } else {
            statuscode.setBit(statuscode.stopStateError)
        }

        // create a corresponding log entry
        if(statuscode.isSuccess()) {
            ioController.writeDataAppStopSuccessLogEntry(response.getUUID(), false)
        } else {
            ioController.writeDataAppStopFailureLogEntry(response.getUUID(), statuscode.createMessage(), false)
        }

        logger.info("""
            |Finished stop request.
            |UUID of stopped Data App: ${response.getUUID()}.
            |Statuscode: ${statuscode.getStatuscode()}
            |Return message: ${statuscode.createMessage()}
        """.trimMargin())

        return returnValueMessageToString(response, statuscode)
    }

    private fun removeDockerElementsByUuid(uuid: UUID) {
        // if there is a docker container for the data app remove it
        // IMPORTANT: Container must be removed before the image
        if (ioController.getContainerIdByUuid(uuid) != ioController.getDefaultContainerId()) {
            dockerController.removeContainerByUuid(uuid)
            ioController.setContainerIdByUuid(uuid, ioController.getDefaultContainerId())
        }
        // if there is a docker image remove for the data app remove it
        if (ioController.getImageIdByUuid(uuid) != ioController.getDefaultImageId()) {
            dockerController.removeImageByUuid(uuid)
            ioController.setImageIdByUuid(uuid, ioController.getDefaultImageId())
        }
    }

    private fun statuscodeAwareRemoveGit(request: IUuidMessage, statuscode: Statuscode) {
        try {
            ioController.removeRepositoryByUuid(request.getUUID())
        } catch (e: Exception) {
            statuscode.setBit(statuscode.ioError)
            if (e.message != null) {
                statuscode.appendError(e.message!!)
            }
        }
    }

    private fun statuscodeAwareRemoveManagementDir(request: IUuidMessage, statuscode: Statuscode) {
        try {
            ioController.removeManagementDirByUuid(request.getUUID())
        } catch (e: Exception) {
            statuscode.setBit(statuscode.ioError)
            if (e.message != null) {
                statuscode.appendError(e.message!!)
            }
        }
    }


    private fun returnValueMessageToString(messageI: IReturnvalueMessage, statuscode: Statuscode): String {
        messageI.setStatuscode(statuscode.getStatuscode())
        messageI.setMessage(statuscode.createMessage())
        messageI.updateTimestamp()

        return try {
            messageI.toJson()
        } catch (e: Exception) {
            statuscode.appendError(e.message!!)
            statuscode.setBit(statuscode.outputFormatError)
            messageI.setStatuscode(statuscode.getStatuscode())
            messageI.setMessage(statuscode.createMessage())
            messageI.toJson()
        }
    }

    private inline fun <reified T: Message>statuscodeAwareRequestParsing(json: String, statuscode: Statuscode): T? {
        val request = try {
            @Suppress("UNCHECKED_CAST")
            val request = MessageFactory.createMessage(T::class.java as Class<Message>)
            request!!.fromJson(json)

            request
        } catch (e: Exception) {
            statuscode.setBit(statuscode.inputFormatError)
            if (e.message != null) {
                statuscode.appendError(e.message!!)
            }

            null
        }
        return request as T
    }

    private fun statuscodeAwareGitClone(
            request: IUsernamePasswordMessage?,
            response: RegisterResponse,
            statuscode: Statuscode,
            repositoryUrl: String) {
        try {
            gitController.cloneRepository(
                    ioController.getRepoDirByUuid(response.getUUID()),
                    repositoryUrl,
                    request!!.getUsername(),
                    request.getPassword()
            )
        } catch (e: Exception) {
            statuscode.setBit(statuscode.dataAppGitError)
            if (e.message != null) {
                statuscode.appendError(e.message!!)
            }
        }
    }

    private fun statuscodeAwareRemoveRepositoryMetadata(uuid: UUID, statuscode: Statuscode) {
        try {
            ioController.removeGitMetadataByUuid(uuid)
        } catch (e: Exception) {
            statuscode.setBit(statuscode.ioError)
            if (e.message != null) {
                statuscode.appendError(e.message!!)
            }
        }
    }

    private fun statuscodeAwareCommitPushGit(message: String, statuscode: Statuscode, update: Boolean = false) {
        try {
            gitController.commitPushGit(message, update)
        } catch (e: Exception) {
            statuscode.setBit(statuscode.dataAppGitError)
            if (e.message != null) {
                statuscode.appendError(e.message!!)
            }
        }
    }

}
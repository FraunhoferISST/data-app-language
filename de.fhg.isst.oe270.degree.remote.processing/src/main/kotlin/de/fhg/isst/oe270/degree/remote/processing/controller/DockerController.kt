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

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.RemoteApiVersion
import de.fhg.isst.oe270.degree.remote.processing.configuration.DockerConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory
import com.github.dockerjava.core.command.BuildImageResultCallback
import de.fhg.isst.oe270.degree.remote.processing.DataAppState
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Component("dockerController")
class DockerController {

    private val logger = LoggerFactory.getLogger("DockerController")!!

    @Autowired
    lateinit var dockerConfiguration: DockerConfiguration

    @Autowired
    lateinit var ioController: IoController

    private val dockerClient : DockerClient by lazy { buildDockerClient() }

    private val threadPool: ExecutorService by lazy { Executors.newFixedThreadPool(dockerConfiguration.threadPoolSize) }

    @PostConstruct
    private fun initDockerSystem() {
        logger.info("Initializing docker system.")
        val info = dockerClient.infoCmd().exec()
        logger.info("Docker info: ${System.lineSeparator()}${dockerInfoToString(info)}")
        logger.info("Initialization of docker system finished.")
    }

    private fun buildDockerClient() : DockerClient {
        logger.info("Building docker client")

        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withApiVersion(RemoteApiVersion.parseConfig(dockerConfiguration.apiVersion))
                .withDockerHost(dockerConfiguration.host)
        if (dockerConfiguration.tlsVerify) {
            config.withDockerTlsVerify(true)
                    .withDockerCertPath(dockerConfiguration.certPath)
        } else {
            config.withDockerTlsVerify(false)
        }
        if (dockerConfiguration.registryUrl.isNotBlank()) {
            config.withRegistryUrl(dockerConfiguration.registryUrl)
            if (dockerConfiguration.registryUsername.isNotBlank()) {
                config.withRegistryUsername(dockerConfiguration.registryUsername)
                config.withRegistryPassword(dockerConfiguration.registryPassword)
                config.withRegistryEmail(dockerConfiguration.registryEmail)
            }
        }

        val dockerCmdExecFactory = JerseyDockerCmdExecFactory()
                .withReadTimeout(dockerConfiguration.readTimeout)
                .withConnectTimeout(dockerConfiguration.connectionTimeout)
                .withMaxTotalConnections(dockerConfiguration.connectionTotalMax)
                .withMaxPerRouteConnections(dockerConfiguration.connectionRouteMax)

        return DockerClientBuilder.getInstance(config.build())
                .withDockerCmdExecFactory(dockerCmdExecFactory).build()
    }

    fun deployDataApp(uuid: UUID) {
        logger.info("Queueing deployment of Data App $uuid.")
        ioController.setDataAppStateByUuid(uuid, DataAppState.DEPLOYING)
        threadPool.execute { dockerBuildWorker(uuid) }
    }

    fun removeContainerByUuid(uuid: UUID) {
        dockerClient
                .removeContainerCmd(ioController.getContainerIdByUuid(uuid))
                .exec()
    }

    fun removeImageByUuid(uuid: UUID) {
        dockerClient
                .removeImageCmd(ioController.getImageIdByUuid(uuid))
                .exec()
    }

    fun startDataApp(uuid: UUID) {
        logger.info("Queueing start of Data App $uuid.")
        ioController.setDataAppStateByUuid(uuid, DataAppState.STARTING)
        threadPool.execute { dockerStartWorker(uuid) }
    }

    fun stopDataApp(uuid: UUID) {
        logger.info("Queueing stop of Data App $uuid.")
        ioController.setDataAppStateByUuid(uuid, DataAppState.TERMINATING)
        threadPool.execute { dockerStopWorker(uuid) }
    }

    private fun dockerInfoToString(info: Info): String {
        return """
            Docker version: ${info.serverVersion} architecture: ${info.architecture}
            Images: ${info.images}
            Container: ${info.containers} (${info.containersRunning} Running / ${info.containersPaused} Paused / ${info.containersStopped} Stopped)
        """.trimIndent()
    }

    private fun dockerStopWorker(uuid: UUID) {
        logger.info("Stopping Data App $uuid.")
        val containerId = ioController.getContainerIdByUuid(uuid)
        dockerClient.stopContainerCmd(containerId).exec()
        logger.info("Container with id $containerId stopped.")
        // set state if there was no external change in the meantime
        if (ioController.getDataAppStateByUuid(uuid) == DataAppState.TERMINATING)
            ioController.setDataAppStateByUuid(uuid, DataAppState.TERMINATED)
    }

    private fun dockerStartWorker(uuid: UUID) {
        logger.info("Starting Data App $uuid.")
        val imageId = ioController.getImageIdByUuid(uuid)
        // if there is an old container we need to remove it
        val oldContainerId = ioController.getContainerIdByUuid(uuid)
        if (!oldContainerId.isNullOrEmpty() && oldContainerId != ioController.getDefaultContainerId()) {
            logger.info("Removing old container with id '$oldContainerId' for Data App $uuid.")
            dockerClient.removeContainerCmd(oldContainerId).exec()
        }
        // create container
        val usedPort = ioController.getUsedPortByUuid(uuid).toInt()
        val portBinding = Ports()
        portBinding.bind(ExposedPort.tcp(usedPort), Ports.Binding.bindPort(usedPort))
        val containerId = dockerClient.createContainerCmd(imageId)
                .withExposedPorts(ExposedPort(usedPort))
                .withHostConfig(HostConfig.newHostConfig().withPortBindings(portBinding))
                .exec().id
        logger.info("Created container with id $containerId out of image with id $imageId.")
        ioController.setContainerIdByUuid(uuid, containerId)
        logger.info("Starting container with id $containerId.")
        dockerClient.startContainerCmd(containerId).exec()
        // sleep for 15 seconds to allow the data app to start
        Thread.sleep(15000)
        logger.info("Container with id $containerId started.")
        // set state if there was no external change in the meantime
        if (ioController.getDataAppStateByUuid(uuid) == DataAppState.STARTING)
        ioController.setDataAppStateByUuid(uuid, DataAppState.RUNNING)
    }

    private fun dockerBuildWorker(uuid: UUID) {
        logger.info("Deploying Data App with id $uuid.")
        logger.info("Preparing temporarily build folder.")
        ioController.createTmpDirByUuid(uuid)
        val dockerfileFile = File(ioController.getTmpDirByUuid(uuid) + "${File.separator}Dockerfile")
        val runFile = File(ioController.getTmpDirByUuid(uuid) + "${File.separator}run.sh")
        FileUtils.copyFile(
                File(ioController.getRepoDirByUuid(uuid) + "${File.separator}generated${File.separator}target${File.separator}dataApp.jar"),
                File(ioController.getTmpDirByUuid(uuid) + "${File.separator}dataApp.jar")
                )
        FileUtils.copyInputStreamToFile(
                DockerController::class.java.getResourceAsStream("/containerTemplate/Dockerfile"),
                dockerfileFile
        )
        FileUtils.copyInputStreamToFile(
                DockerController::class.java.getResourceAsStream("/containerTemplate/run.sh"),
                runFile
        )
        // ensure linux line endings in files
        for (file in setOf(dockerfileFile, runFile)) {
            val content = FileUtils.readFileToString(file, Charset.forName("utf-8"))
            FileUtils.writeStringToFile(file, content.replace("\r\n", "\n"),Charset.forName("utf-8"), false)
        }

        logger.info("Executing docker build.")
        val success = try {
            var port = ioController.getUsedPortByUuid(uuid)
            if (port == ioController.getDefaultPort()) {
                // this is the first time the image is build so we need to get a new port
                port = ioController.getFreePort(uuid)

            }
            val imageId = dockerClient
                    .buildImageCmd(File(ioController.getTmpDirByUuid(uuid) + File.separator + "Dockerfile"))
                    .withBuildArg("http_proxy", dockerConfiguration.imageHttpProxyHost)
                    .withBuildArg("https_proxy", dockerConfiguration.imageHttpsProxyHost)
                    .withBuildArg("app_port", "" + port)
                    .exec(
                            object : BuildImageResultCallback() {
                                override fun onNext(item: BuildResponseItem) {
                                    if (!item.stream.isNullOrBlank()) {
                                        logger.info("Docker build message: ${item.stream!!.trim()}")
                                    }
                                    super.onNext(item)
                                }
                            }
            ).awaitImageId()
            logger.info("Docker build finished. ImageID is '$imageId'.")

            val oldImageId = ioController.getImageIdByUuid(uuid)
            if (oldImageId != ioController.getDefaultImageId()) {
                logger.info("There is already an image with ID '$oldImageId' for Data App '$uuid'. Going to delete it.")
                // before we can delete the old image we need to check if there is a container referencing the image
                val oldContainerId = ioController.getContainerIdByUuid(uuid)
                if (oldContainerId != ioController.getDefaultContainerId()) {
                    logger.info("There is a container with id $oldContainerId which references the image which will be deleted. Deleting the container, too.")
                    dockerClient.removeContainerCmd(oldContainerId).exec()
                }
                // there is already an image for this data app --> remove it and reuse port
                dockerClient
                        .removeImageCmd(oldImageId)
                        .exec()
            } else {
                ioController.setPortByUuid(uuid, port)
            }
            // update the used image id
            ioController.setImageIdByUuid(uuid, imageId)

            ioController.setDataAppStateByUuid(uuid, DataAppState.DEPLOYED)
            true
        } catch (e: Exception) {
            logger.error("error", e)
            logger.info("Docker build failed with error '${e.message}'.")
            ioController.setDataAppStateByUuid(uuid, DataAppState.DEPLOYMENT_ERROR)
            false
        }
        logger.info("Removing build folder.")
        ioController.removeTmpDirByUuid(uuid)
    }

}
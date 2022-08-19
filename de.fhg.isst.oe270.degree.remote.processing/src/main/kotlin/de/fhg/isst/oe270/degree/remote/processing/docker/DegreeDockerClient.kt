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
package de.fhg.isst.oe270.degree.remote.processing.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory
import de.fhg.isst.oe270.degree.remote.processing.configuration.DockerConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class DegreeDockerClient {

    @Autowired
    private lateinit var dockerConfig : DockerConfiguration

    // the docker client is used for all docker communication
    private var dockerClient : DockerClient? = null

    fun getDockerClient() : DockerClient {
        setupDockerClient()

        return dockerClient!!
    }

    /**
     * Create a docker client for communication with the docker machine specified in the configuration.
     */
    @PostConstruct
    private fun setupDockerClient() {
        val defaultDockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withApiVersion(dockerConfig.apiVersion)
                .withDockerHost(dockerConfig.host)
                .withDockerTlsVerify(dockerConfig.tlsVerify)
                .withDockerCertPath(dockerConfig.certPath)
                .withRegistryUsername(dockerConfig.registryUsername)
                .withRegistryPassword(dockerConfig.registryPassword)
                .withRegistryEmail(dockerConfig.registryEmail)
                .withRegistryUrl(dockerConfig.registryUrl)
                .build()

        val dockerExecCmdFactory = JerseyDockerCmdExecFactory()
                .withConnectTimeout(dockerConfig.connectionTimeout)
                .withMaxTotalConnections(dockerConfig.connectionTotalMax)
                .withMaxPerRouteConnections(dockerConfig.connectionRouteMax)
                .withReadTimeout(dockerConfig.readTimeout)

        val dockerClientBuilder = DockerClientBuilder.getInstance(defaultDockerConfig)
                .withDockerCmdExecFactory(dockerExecCmdFactory)

        dockerClient = dockerClientBuilder.build()
    }

}
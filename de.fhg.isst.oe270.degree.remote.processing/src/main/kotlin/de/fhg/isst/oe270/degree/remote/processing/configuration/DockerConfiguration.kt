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
package de.fhg.isst.oe270.degree.remote.processing.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DockerConfiguration {

    @Value("\${docker.api.version:1.38}")
    var apiVersion : String = "1.38"

    @Value("\${docker.host:tcp://127.0.0.1:2375}")
    var host : String = "tcp://127.0.0.1:2376"

    @Value("\${docker.tls.verify:false}")
    var tlsVerify : Boolean = false

    @Value("\${docker.cert.path:/home/user/.docker}")
    var certPath : String = "/home/user/.docker"

    @Value("\${docker.registry.username:}")
    var registryUsername : String = ""

    @Value("\${docker.registry.password:}")
    var registryPassword : String = ""

    @Value("\${docker.registry.email:}")
    var registryEmail : String = ""

    @Value("\${docker.registry.url:}")
    var registryUrl : String = ""

    @Value("\${docker.connection.timeout:1000}")
    var connectionTimeout : Int = 1000

    @Value("\${docker.connection.total.max:100}")
    var connectionTotalMax : Int = 100

    @Value("\${docker.connection.route.max:10}")
    var connectionRouteMax : Int = 10

    @Value("\${docker.read.timeout:1000}")
    var readTimeout : Int = 1000

    @Value("\${docker.threadpool.size:1}")
    var threadPoolSize : Int = 1

    @Value("\${docker.image.proxy.http:}")
    var imageHttpProxyHost : String = ""

    @Value("\${docker.image.proxy.https:}")
    var imageHttpsProxyHost : String = ""

}
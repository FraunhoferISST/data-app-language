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

import de.fhg.isst.oe270.degree.remote.processing.configuration.DispatchConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*

@Component("dispatchController")
class DispatchController {

    private val logger = LoggerFactory.getLogger("DispatchController")!!

    @Autowired
    lateinit var dispatchConfiguration: DispatchConfiguration

    @Autowired
    lateinit var ioController: IoController

    /**
     * TODO: the function is currently relying on well-formed input which is in fact validated nowhere
     */
    fun dispatchToHttpDataApp(uuid: UUID, json: String): String {
        val requestFactory = SimpleClientHttpRequestFactory()
        if (!dispatchConfiguration.httpProxyHost.isNullOrEmpty()) {
            val proxy = Proxy(Proxy.Type.HTTP,
                    InetSocketAddress(dispatchConfiguration.httpProxyHost, dispatchConfiguration.httpProxyPort.toInt()))
            requestFactory.setProxy(proxy)
        }
        val restTemplate = RestTemplate(requestFactory)
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_PLAIN
        headers.accept = listOf(MediaType.TEXT_PLAIN)

        val response : ResponseEntity<String> = restTemplate.exchange(
                getUrlForHttpDataAppByUuid(uuid),
                HttpMethod.POST,
                HttpEntity(json, headers),
                String::class.java)

        return if (response.hasBody()) {
            response.body
        } else {
            ""
        }
    }

    private fun getUrlForHttpDataAppByUuid(uuid: UUID): String {
        return dispatchConfiguration.machineUrl + ":" + ioController.getUsedPortByUuid(uuid) + "/process"
    }

}
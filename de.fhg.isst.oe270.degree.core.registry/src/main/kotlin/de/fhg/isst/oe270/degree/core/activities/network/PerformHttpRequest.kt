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
package de.fhg.isst.oe270.degree.core.activities.network

import de.fhg.isst.oe270.degree.activities.BaseActivity
import de.fhg.isst.oe270.degree.activities.annotations.ActivityAnnotation
import de.fhg.isst.oe270.degree.activities.execution.InputScope
import de.fhg.isst.oe270.degree.activities.execution.OutputScope
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import nukleus.core.Identifier
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.net.InetSocketAddress
import java.net.Proxy


@ActivityAnnotation("core.PerformHttpRequest")
class PerformHttpRequest : BaseActivity(){

    private val logger = LoggerFactory.getLogger("core.PerformHttpRequest")

    override fun run(input: InputScope): OutputScope {
        // check if all required inputs are available
        val url : String
        val httpMethod : String
        val body : String
        val contentType : String
        val acceptedTypes : String
        val proxyHost : String
        val proxyPort : Int?
        try {
            url = input.values["url"]!!.read()
            httpMethod = input.values["httpMethod"]!!.read()
            contentType = input.values["contentType"]!!.read()
            acceptedTypes = input.values["acceptedTypes"]!!.read()
            body = input.values["body"]!!.read()
            proxyHost = input.values["proxyHost"]!!.read()
            proxyPort = input.values["proxyPort"]!!.read().toIntOrNull()
        } catch (e : Exception) {
            logger.error("Missing input data.")
            val errorInstance = TypeTaxonomy.getInstance().create(Identifier.of("core.Error"))
            errorInstance.write("Execution aborted because of missing input data.")
            val outputScope = OutputScope()
            outputScope.add("error", errorInstance)

            return outputScope
        }
        // check that input types are correct
        if (input.values["proxyHost"]!!.type.identifier.toString() != "core.Hostname" ||
                input.values["proxyPort"]!!.type.identifier.toString() != "core.Port" ||
                input.values["url"]!!.type.identifier.toString() != "core.URL" ||
                input.values["httpMethod"]!!.type.identifier.toString() != "core.HttpMethod" ||
                input.values["contentType"]!!.type.identifier.toString() != "core.HttpContentType" ||
                input.values["acceptedTypes"]!!.type.identifier.toString() != "core.HttpAcceptedTypes" ||
                input.values["body"]!!.type.identifier.toString() != "core.Text") {
            logger.error("Wrong input types.")
            val errorInstance = TypeTaxonomy.getInstance().create(Identifier.of("core.Error"))
            errorInstance.write("Execution aborted because of wrong input types.")
            val outputScope = OutputScope()
            outputScope.add("error", errorInstance)
        }

        val requestFactory = SimpleClientHttpRequestFactory()

        if (proxyHost.isNotBlank() && proxyHost.isNotEmpty()) {
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort!!))
            requestFactory.setProxy(proxy)
        }

        // perform rest call
        val restTemplate = RestTemplate(requestFactory)
        val headers = HttpHeaders()
        val httpMethodVal = when(httpMethod.toUpperCase()) {
            "GET" -> HttpMethod.GET
            "POST" -> HttpMethod.POST
            "PUT" -> HttpMethod.PUT
            "DELETE" -> HttpMethod.DELETE
            "PATCH" -> HttpMethod.PATCH
            "HEAD" -> HttpMethod.HEAD
            "TRACE" -> HttpMethod.TRACE
            "OPTIONS" -> HttpMethod.OPTIONS
            else -> null
        }

        if (contentType.isNotBlank() && contentType.isNotEmpty()) {
            headers.contentType = MediaType.parseMediaType(contentType)
        }

        if (acceptedTypes.isNotBlank() && acceptedTypes.isNotEmpty()) {
            headers.accept = acceptedTypes.split(",").map { MediaType.parseMediaType(it) }
        }

        val response : ResponseEntity<String> = restTemplate.exchange(url, httpMethodVal!!, HttpEntity(body, headers), String::class.java)
        // build output scope
        val result = TypeTaxonomy.getInstance().create(Identifier.of("core.Text"))
        if (response.hasBody()) {
            result.write(response.body!!)
        } else {
            result.write("")
        }
        val outputScope = OutputScope()
        outputScope.add("response", result)

        return outputScope
    }

}
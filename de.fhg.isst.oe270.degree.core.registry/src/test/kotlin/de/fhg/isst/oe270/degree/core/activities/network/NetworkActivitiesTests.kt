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

import de.fhg.isst.oe270.degree.activities.execution.InputScope
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import de.fhg.isst.oe270.degree.util.SubSystemUtils
import nukleus.core.Identifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NetworkActivitiesTests {

    private val typeTaxonomy : TypeTaxonomy = TypeTaxonomy.getInstance()

    init {
        if (typeTaxonomy.size() == 0) {
            SubSystemUtils.updateSubSystems()
            typeTaxonomy.load(Paths.get(Configuration.CORE_TYPES_FILE_PATH))
        }
    }

    @Test
    fun `Test that Activity core_PerformHttpRequest can perform HTTP-GET requests`() {
        val url = typeTaxonomy.create(Identifier.of("core.URL"))
        url.write("http://google.de")
        val httpMethod = typeTaxonomy.create(Identifier.of("core.HttpMethod"))
        httpMethod.write("GET")
        val contentType = typeTaxonomy.create(Identifier.of("core.HttpContentType"))
        contentType.write("")
        val acceptedTypes = typeTaxonomy.create(Identifier.of("core.HttpContentType"))
        acceptedTypes.write("*/*")
        val body = typeTaxonomy.create(Identifier.of("core.Text"))
        body.write("")
        val proxyHost = typeTaxonomy.create(Identifier.of("core.Hostname"))
        proxyHost.write(System.getenv("http.proxyHost") ?: System.getProperty("http.proxyHost", ""))
        val proxyPort = typeTaxonomy.create(Identifier.of("core.Port"))
        proxyPort.write(System.getenv("http.proxyPort") ?: System.getProperty("http.proxyPort", "80"))

        val inputScope = InputScope()
        inputScope.add("url", url)
        inputScope.add("httpMethod", httpMethod)
        inputScope.add("contentType", contentType)
        inputScope.add("acceptedTypes", acceptedTypes)
        inputScope.add("body", body)
        inputScope.add("proxyHost", proxyHost)
        inputScope.add("proxyPort", proxyPort)

        val activity = PerformHttpRequest()
        val outputScope = activity.run(inputScope)

        assertNotNull(outputScope.get("response"))
        assertTrue(outputScope.get("response")!!.read().isNotEmpty())
    }

}
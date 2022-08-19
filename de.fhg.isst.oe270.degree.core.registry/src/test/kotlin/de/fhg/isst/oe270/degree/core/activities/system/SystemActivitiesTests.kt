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
package de.fhg.isst.oe270.degree.core.activities.system

import de.fhg.isst.oe270.degree.activities.execution.InputScope
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import de.fhg.isst.oe270.degree.util.SubSystemUtils
import nukleus.core.Identifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
 class SystemActivitiesTests {

    private val typeTaxonomy : TypeTaxonomy = TypeTaxonomy.getInstance()

    init {
        if (typeTaxonomy.size() == 0) {
            SubSystemUtils.updateSubSystems()
            typeTaxonomy.load(Paths.get(Configuration.CORE_TYPES_FILE_PATH))
        }
    }

    @Test
    fun `Test that Activity core_ExecCmd successfully executes the given command`() {
        // create required parameters and inputscope
        val cmd = typeTaxonomy.create(Identifier.of("core.Text"))
        // the actual command depends on the used operating system
        if (System.getProperty("os.name").startsWith("Windows")) {
            cmd.write("cmd.exe /c echo")
        } else {
            cmd.write("echo")
        }
        val args = typeTaxonomy.create(Identifier.of("core.Text"))
        args.write("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
        val inputScope = InputScope()
        inputScope.add("cmd", cmd)
        inputScope.add("args", args)

        // exec activity
        val execCmd = ExecCmd()
        val outputScope = execCmd.run(inputScope)

        // check for correct result
        assertEquals(args.read().trim(),
                outputScope.get("returnValue")!!.read().trim(),
                "Return value of executed command differs from the expected.")
    }

}
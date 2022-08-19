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
package de.fhg.isst.oe270.degree.core.activities.io

import de.fhg.isst.oe270.degree.activities.execution.InputScope
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import de.fhg.isst.oe270.degree.util.SubSystemUtils
import nukleus.core.Identifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IoActivitiesTests {

    private val typeTaxonomy : TypeTaxonomy = TypeTaxonomy.getInstance()

    init {
        if (typeTaxonomy.size() == 0) {
            SubSystemUtils.updateSubSystems()
            typeTaxonomy.load(Paths.get(Configuration.CORE_TYPES_FILE_PATH))
        }
    }

    @Test
    fun `Test that Activity core_PrintToConsole prints the correct content to the console`() {
        // save original standard output
        val originalOutput = System.out
        // redirect stdout
        val byteArrayOutputStream = ByteArrayOutputStream()
        val tempOutput = PrintStream(byteArrayOutputStream, true, "UTF-8")
        System.setOut(tempOutput)

        // prepare and call print to console activity
        // val text = typeTaxonomy.create(Identifier.of("core.Text"))
        val text = typeTaxonomy.newInstance(Identifier.of("core.Text"));
        text.write("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
        val inputScope = InputScope()
        inputScope.add("text", text)
        val printToConsole = PrintToConsole()
        printToConsole.run(inputScope)

        // restore original standard output
        System.setOut(originalOutput)

        // check for correct results
        assertEquals(text.read() + System.lineSeparator(),
                String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8),
                "The text which was written to console differs from the expected text.")
    }

    @Test
    fun `Test that Activity core_ReadFile returns content of a given file`() {
        // create required parameters
        // val filePath = typeTaxonomy.create(Identifier.of("core.Text"))
        val filePath = typeTaxonomy.newInstance(Identifier.of("core.Text"));
        // this is some kind of hack for test execution from intelliJ vs mvn test
        if (this.javaClass.getResource("../testFile.txt") != null) {
            filePath.write(this.javaClass.getResource("../testFile.txt").path.toString())
        } else {
            filePath.write(this.javaClass.getResource("/testFile.txt").path.toString())
        }
        // build correct input scope for activity
        val inputScope = InputScope()
        inputScope.add("filePath", filePath)
        // create the activity
        val activity = ReadFile()
        // call the activity
        val outputScope = activity.run(inputScope)
        // check for correct result
        assertEquals("This is a multiline${System.lineSeparator()}test file.",
                outputScope.get("content")!!.read(),
                "Read file content does not match the actual file content.")
    }

    @Test
    fun `Test that Activity core_WriteFile stores the correct content to a given file`() {
        // create required parameters
        // val filePath = typeTaxonomy.create(Identifier.of("core.Text"))
        val filePath = typeTaxonomy.newInstance(Identifier.of("core.Text"));
        filePath.write(this.javaClass.getResource("..").path.plus("anotherTestFile.txt"))
        // val content = typeTaxonomy.create(Identifier.of("core.Text"))
        val content = typeTaxonomy.newInstance(Identifier.of("core.Text"));
        content.write("""
            |Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer placerat neque eget quam tristique, sed mollis eros vehicula. In consequat congue libero, nec congue augue rutrum nec. Donec sit amet metus ipsum. Duis tempus, nunc eu tristique pulvinar, lacus urna dictum arcu, ut finibus nibh libero vel lectus. Integer lobortis eu urna sed vehicula. Sed rutrum porta nisl nec auctor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer pellentesque in ipsum ac aliquam. In ultrices justo augue, at congue massa dictum quis. Fusce porttitor blandit tincidunt. Phasellus nisl tortor, venenatis ac sollicitudin in, fringilla sed augue. Suspendisse potenti. Pellentesque dui ex, hendrerit eu semper pretium, rutrum a lectus.
            |
            |Maecenas in nisi laoreet, pellentesque eros eget, mattis tortor. Quisque vitae fringilla risus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Vivamus porttitor elit magna, at mollis ex rutrum sed. Donec aliquet est sit amet enim orci aliquam.
        """.trimMargin())
        // build correct input scope for activities
        val readInputScope = InputScope()
        readInputScope.add("filePath", filePath)
        val writeInputScope = InputScope()
        writeInputScope.add("filePath", filePath)
        writeInputScope.add("content", content)
        // create activities
        val writeActivity = WriteFile()
        val readActivity = ReadFile()
        // call activities
        writeActivity.run(writeInputScope)
        val outputScope = readActivity.run(readInputScope)
        // check for correct result
        assertEquals(content.read(),
                outputScope.get("content")!!.read(),
                "The content which was written to file differs from the one loaded.")
    }

}
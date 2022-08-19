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

import de.fhg.isst.oe270.degree.activities.BaseActivity
import de.fhg.isst.oe270.degree.activities.annotations.ActivityAnnotation
import de.fhg.isst.oe270.degree.activities.execution.InputScope
import de.fhg.isst.oe270.degree.activities.execution.OutputScope
import de.fhg.isst.oe270.degree.runtime.java.security.functionality.modules.DegreeFileOperations
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import nukleus.core.Identifier

@ActivityAnnotation("core.ExecCmd")
class ExecCmd : BaseActivity() {

    override fun run(input: InputScope): OutputScope {
        // retrieve desired command
        val command = input.values["cmd"]!!.read()
        // and its arguments
        val arguments = input.values["args"]!!.read()
        // build final cmd with args
        var execCmd = command
        if (arguments.isNotEmpty()) {
            execCmd += " $arguments"
        }
        // execute the cmd
        val returnVal = DegreeFileOperations.executeFile(execCmd)
        // build the outputscope
        val result = OutputScope()

        val execOutput = TypeTaxonomy.getInstance().create(Identifier.of("core.Text"))
        execOutput.write(returnVal)
        result.values["returnValue"] = execOutput

        return result
    }

}
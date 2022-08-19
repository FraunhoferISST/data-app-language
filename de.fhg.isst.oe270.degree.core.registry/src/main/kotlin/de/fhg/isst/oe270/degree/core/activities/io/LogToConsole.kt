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

import de.fhg.isst.oe270.degree.activities.BaseActivity
import de.fhg.isst.oe270.degree.activities.annotations.ActivityAnnotation
import de.fhg.isst.oe270.degree.activities.execution.InputScope
import de.fhg.isst.oe270.degree.activities.execution.OutputScope
import org.slf4j.LoggerFactory

@ActivityAnnotation("core.LogToConsole")
class LogToConsole : BaseActivity() {

    private var logger = LoggerFactory.getLogger("LogToConsole")

    override fun run(input: InputScope): OutputScope {
        logger.info(input.values["text"]!!.read())

        return OutputScope()
    }

}

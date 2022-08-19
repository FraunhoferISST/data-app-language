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
import java.io.File

@Component
class IoConfiguration {

    @Value("\${remote.processing.work.dir:./remoteProcessing}")
    var workDir : String = "${System.getProperty("user.home")}${File.separator}.degree${File.separator}remoteProcessing"

    val repoDir = workDir + File.separator + "repository"

    val managementDir = workDir + File.separator + "management"

    val tempDir = workDir + File.separator + "temp"

    val logFile = workDir + File.separator + "remoteProcessing.log"

    val gitManagementDir = workDir + File.separator + ".git"

    val originFileName = "origin.json"

    val usedUuidFile = workDir + File.separator + "uuidState.json"

    val minimumPort = 10000u

    val imageDataFileName = "imageData.json"

    val imageDataPortKey = "port"

    val imageDataPortDefaultValue = 0u

    val imageDataImageIdKey = "imageId"

    val imageDataImageIdDefaultValue = "" + 0u

    val imageDataContainerIdKey = "containerId"

    val imageDataContainerIdDefaultValue = "" + 0u

}
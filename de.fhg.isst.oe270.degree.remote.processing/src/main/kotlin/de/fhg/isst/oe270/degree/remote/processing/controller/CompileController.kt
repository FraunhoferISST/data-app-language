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

import de.fhg.isst.oe270.degree.compiler.Entrypoint.startCompile
import de.fhg.isst.oe270.degree.remote.processing.DataAppState
import de.fhg.isst.oe270.degree.remote.processing.configuration.CompileConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Component("compileController")
class CompileController {

    private val logger = LoggerFactory.getLogger("CompileController")!!

    @Autowired
    lateinit var compileConfiguration: CompileConfiguration

    @Autowired
    lateinit var ioController: IoController

    private val threadPool: ExecutorService by lazy { Executors.newFixedThreadPool(compileConfiguration.threadPoolSize) }

    @PostConstruct
    fun initCompileSystem() {
        logger.info("Initializing compile system.")
        logger.info("Creating a thread pool of ${compileConfiguration.threadPoolSize} threads.")
        threadPool // useless reference of thread pool to trigger lazy initialization
        logger.info("Initialization of compile system finished.")
    }

    fun compileByUuid(uuid: UUID) {
        logger.info("Queueing compilation of Data App $uuid.")
        ioController.setDataAppStateByUuid(uuid, DataAppState.COMPILING)
        threadPool.execute { compileWorker(uuid) }
    }

    private fun compileWorker(uuid: UUID) {
        logger.info("Starting compilation of Data App $uuid.")
        val success = startCompile(arrayOf(ioController.getRepoDirByUuid(uuid)))
        if (success) {
            ioController.setDataAppStateByUuid(uuid, DataAppState.COMPILED)
        } else {
            ioController.setDataAppStateByUuid(uuid, DataAppState.COMPILATION_ERROR)
        }
        logger.info("Finished compilation of Data App $uuid")
    }

}
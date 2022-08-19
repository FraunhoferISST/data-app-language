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

import com.beust.klaxon.Klaxon
import de.fhg.isst.oe270.degree.remote.processing.DataAppState
import de.fhg.isst.oe270.degree.remote.processing.communication.requests.RegisterGitRequest
import de.fhg.isst.oe270.degree.remote.processing.configuration.IoConfiguration
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileFilter
import java.nio.charset.Charset
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.annotation.PostConstruct
import java.util.HashMap

@Component("ioController")
@DependsOn("gitController")
class IoController {

    private val logger = LoggerFactory.getLogger("IoController")!!

    @Autowired
    lateinit var ioConfiguration: IoConfiguration

    @Autowired
    lateinit var gitController: GitController

    // Contains all UUIDs which are already used currently
    private val currentlyUsedUuids = HashSet<UUID>()

    // Contains all UUIDs and their state which have been used at some time
    private val usedUuids = HashMap<UUID, DataAppState>()

    // Contains all ports which are used by data app images
    private val usedPorts = HashMap<UUID, UShort>()

    // Contains image data (if available) for data apps
    private val imageData = HashMap<UUID, HashMap<String, String>>()

    @PostConstruct
    private fun initIoSystem() {
        logger.info("Initializing IO system.")

        // ensure the required folders are available
        val workDir = File(ioConfiguration.workDir)
        val repoDir = File(ioConfiguration.repoDir)
        val managementDir = File(ioConfiguration.managementDir)
        val tempDir = File(ioConfiguration.tempDir)

        createWorkingDirectory(workDir)
        createWorkingDirectory(repoDir)
        createWorkingDirectory(managementDir)
        createWorkingDirectory(tempDir)

        FileUtils.writeStringToFile(
                File(ioConfiguration.workDir + File.separator + ".gitignore"),
                retrieveGitignore(),
                Charset.forName("utf-8"),
                false)

        // create log file if it doesn't exist yet
        File(ioConfiguration.logFile).createNewFile()

        // load the list of currently used UUIDs
        managementDir.listFiles(DirectoryFileFilter.DIRECTORY as FileFilter)!!.forEach { dir ->
            currentlyUsedUuids.add(UUID.fromString(dir.name))
        }
        logger.info("Loaded ${currentlyUsedUuids.size} currently used UUIDs.")

        // load list of all used UUIDs
        val uuidFile = File(ioConfiguration.usedUuidFile)
        if (uuidFile.exists()) {
            usedUuids.putAll(Klaxon().parse<Map<String, String>>(uuidFile)!!
                    .mapKeys { UUID.fromString(it.key) }
                    .mapValues { DataAppState.valueOf(it.value) })
        } else {
            FileUtils.writeStringToFile(uuidFile, Klaxon().toJsonString(usedUuids), Charset.forName("utf-8"))
        }
        logger.info("In total ${usedUuids.size} have been used by the remote processing engine.")

        var resolutionRequired = false
        // check for inconsistencies
        for (uuid in currentlyUsedUuids) {
            if (!usedUuids.containsKey(uuid)) {
                logger.info("Found inconsistencies in data about used UUIDs. Going to fix them now.")
                resolutionRequired = true
                break
            }
        }

        if (resolutionRequired) {
            usedUuids.putAll(
                    currentlyUsedUuids.map { it to DataAppState.UNKNOWN_STATE }.toMap()
            )
            FileUtils.writeStringToFile(uuidFile, Klaxon().toJsonString(usedUuids), Charset.forName("utf-8"))
            logger.info("Registered unknown but used UUIDs with UNKNOWN_STATE state.")
        }

        // load image data
        for (uuid in currentlyUsedUuids) {
            loadImageDataByUuid(uuid)
            usedPorts[uuid] = getUsedPortByUuid(uuid)
        }

        logger.info("Initialization of IO system finished.")
        // ensure the folder structure is stored in the git
        gitController.commitPushGit("Created directory structure.")
    }

    /**
     * Ensures that a given directory is existing.
     *
     * @throws RuntimeException in case the directory could not be created
     * @param workDir the directory which should be created
     */
    private fun createWorkingDirectory(workDir: File) {
        if (!workDir.isDirectory) {
            if (workDir.exists()) {
                throw RuntimeException("Could not initialize io system since an already existing file prevents the " +
                        "creation of necessary working directory folder '${workDir.absolutePath}'.")
            }
            if (!workDir.mkdirs()) {
                throw RuntimeException("An error occurred during the creation of the working directory '${workDir.absolutePath}'.")
            }
            logger.info("Created working directory at location '${workDir.absolutePath}'.")
        }
    }

    fun registerDataApp(registrationData: String): UUID {
        val uuid = createFreeUUID()
        // create necessary directories
        try {
            createWorkingDirectory(File(getMgmtDirByUuid(uuid)))
        } catch (e: Exception) {
            writeDataAppRegisteredFailureLogEntry(uuid, e)
            throw e
        }

        try {
            createWorkingDirectory(File(getRepoDirByUuid(uuid)))
        } catch (e: Exception) {
            // in case something went wrong we need to delete the previously generated folder
            FileUtils.forceDelete(File(getMgmtDirByUuid(uuid)))
            writeDataAppRegisteredFailureLogEntry(uuid, e)
            throw e
        }

        try {
            // write the message which was used to register the data app to a file
            FileUtils.writeStringToFile(
                    File(getMgmtDirByUuid(uuid) + File.separator + ioConfiguration.originFileName),
                    registrationData,
                    Charset.forName("utf-8"),
                    false
            )
        } catch (e: Exception) {
            // in case something went wrong we need to delete the previously generated folders
            FileUtils.forceDelete(File(getRepoDirByUuid(uuid)))
            FileUtils.forceDelete(File(getMgmtDirByUuid(uuid)))
            writeDataAppRegisteredFailureLogEntry(uuid, e)
        }

        // mark the new uuid as used
        currentlyUsedUuids.add(uuid)
        writeDataAppRegisteredSuccessLogEntry(uuid, false)

        // store the new information in git
        gitController.commitPushGit("Registered new Data App with UUID $uuid.")

        return uuid
    }

    fun removeGitMetadataByUuid(uuid: UUID) {
        FileUtils.forceDelete(File(getRepoDirByUuid(uuid) + File.separator + ".git"))
    }

    fun removeRepositoryByUuid(uuid: UUID) {
        FileUtils.forceDelete(File(getRepoDirByUuid(uuid)))
    }

    fun removeManagementDirByUuid(uuid: UUID) {
        FileUtils.forceDelete(File(getMgmtDirByUuid(uuid)))
    }

    fun getRepoDirByUuid(uuid: UUID): String {
        return ioConfiguration.repoDir + File.separator + uuid.toString()
    }

    private fun getMgmtDirByUuid(uuid: UUID): String {
        return ioConfiguration.managementDir + File.separator + uuid.toString()
    }

    fun createTmpDirByUuid(uuid: UUID) {
        createWorkingDirectory(File(ioConfiguration.tempDir + File.separator + uuid.toString()))
    }

    fun getTmpDirByUuid(uuid: UUID): String {
        return ioConfiguration.tempDir + File.separator + uuid.toString()
    }

    fun removeTmpDirByUuid(uuid: UUID) {
        FileUtils.forceDelete(File(ioConfiguration.tempDir + File.separator + uuid.toString()))
    }

    /**
     * Creates UUIDs until one is found which is not already in use.
     *
     * @return A UUID which is not yet used
     */
    private fun createFreeUUID(): UUID {
        var uuid = UUID.randomUUID()
        while (currentlyUsedUuids.contains(uuid)) {
            uuid = UUID.randomUUID()
        }

        return uuid
    }

    fun writeDataAppStopSuccessLogEntry(dataApp: UUID, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Successfully stopped Data App.", persistToGit)
    }

    fun writeDataAppStopFailureLogEntry(dataApp: UUID, error: String, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Failed to stop Data App with error '$error'.", persistToGit)
    }

    fun writeDataAppStartSuccessLogEntry(dataApp: UUID, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Successfully started Data App.", persistToGit)
    }

    fun writeDataAppStartFailureLogEntry(dataApp: UUID, error: String, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Failed to start Data App with error '$error'.", persistToGit)
    }

    fun writeDataAppDeployedSuccessLogEntry(dataApp: UUID, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Successfully deployed Data App.", persistToGit)
    }

    fun writeDataAppDeployedFailureLogEntry(dataApp: UUID, error: String, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Failed to deploy Data App with error '$error'.", persistToGit)
    }

    fun writeDataAppCompileSuccessLogEntry(dataApp: UUID, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Successfully started compilation of Data App.", persistToGit)
    }

    fun writeDataAppCompileFailureLogEntry(dataApp: UUID, error: String, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Failed to start compilation of Data App with error '$error'.", persistToGit)
    }

    fun writeDataAppDeletedSuccessLogEntry(dataApp: UUID, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Successfully deleted Data App.", persistToGit)
    }

    fun writeDataAppDeletedFailureLogEntry(dataApp: UUID, error: String, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Failed to delete Data App with error '$error'.", persistToGit)
    }

    fun writeDataAppUpdatedSuccessLogEntry(dataApp: UUID, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Successfully updated Data App.", persistToGit)
    }

    fun writeDataAppUpdatedFailureLogEntry(dataApp: UUID, error: String, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Failed to update Data App with error '$error'.", persistToGit)
    }

    private fun writeDataAppRegisteredFailureLogEntry(dataApp: UUID, e: Exception, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Failed to register Data App with error '${e.message}'.", persistToGit)
    }

    private fun writeDataAppRegisteredSuccessLogEntry(dataApp: UUID, persistToGit: Boolean = true) {
        writeDataAppLogEntry(dataApp, "Successfully registered Data App.", persistToGit)
    }

    private fun writeDataAppLogEntry(dataApp: UUID, message: String, persistToGit: Boolean = true) {
        writeLogEntry("$dataApp - $message", persistToGit)
    }

    fun writeStartupLogEntry(persistToGit: Boolean = true) {
        writeLogEntry("Started the remote processing service. ${currentlyUsedUuids.size} Data Apps are already registered."
                , persistToGit)
    }

    private fun writeLogEntry(message: String, persistToGit: Boolean = true) {
        val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        FileUtils.writeStringToFile(
                File(ioConfiguration.logFile),
                "$timestamp - $message\n",
                Charset.forName("utf-8"),
                true
        )

        if (persistToGit)
        // ensure the new log entry is stored in git
            gitController.commitPushGit("Added new log entry.")
    }

    fun loadGitRegisterRequest(uuid: UUID): RegisterGitRequest {
        val result = RegisterGitRequest()

        val originFile = FileUtils.readFileToString(
                File(getMgmtDirByUuid(uuid) + File.separator + ioConfiguration.originFileName),
                Charset.forName("utf-8"))
        result.fromJson(originFile)

        return result
    }

    fun freeUuid(uuid: UUID) {
        currentlyUsedUuids.remove(uuid)
    }

    fun setDataAppStateByUuid(uuid: UUID, state: DataAppState, commitPush: Boolean = false) {
        usedUuids[uuid] = state
        val uuidFile = File(ioConfiguration.usedUuidFile)
        FileUtils.writeStringToFile(uuidFile, Klaxon().toJsonString(usedUuids), Charset.forName("utf-8"))

        if (commitPush) {
            gitController.commitPushGit("Changed state of Data App $uuid to $state")
        }
    }

    fun getDataAppStateByUuid(uuid: UUID): DataAppState {
        return usedUuids[uuid] ?: DataAppState.UNKNOWN_DATA_APP
    }

    private fun retrieveGitignore(): String {
        return """
            **/generated
            temp/
            """.trimIndent()
    }

    fun setImageIdByUuid(uuid: UUID, value: String) {
        setImageDataByUuid(ioConfiguration.imageDataImageIdKey, uuid, value)
    }

    fun setContainerIdByUuid(uuid: UUID, value: String) {
        setImageDataByUuid(ioConfiguration.imageDataContainerIdKey, uuid, value)
    }

    fun setPortByUuid(uuid: UUID, value: UShort) {
        setImageDataByUuid(ioConfiguration.imageDataPortKey, uuid, "" + value)
    }

    fun getImageIdByUuid(uuid: UUID): String {
        return getImageDataByUuid(ioConfiguration.imageDataImageIdKey, uuid, ioConfiguration.imageDataImageIdDefaultValue)
    }

    fun getContainerIdByUuid(uuid: UUID): String {
        return getImageDataByUuid(ioConfiguration.imageDataContainerIdKey, uuid, ioConfiguration.imageDataContainerIdDefaultValue)
    }

    fun getUsedPortByUuid(uuid: UUID): UShort {
        val stringValue = getImageDataByUuid(ioConfiguration.imageDataPortKey, uuid, ioConfiguration.imageDataPortDefaultValue.toString()) as String
        return stringValue.toUShort()
    }

    fun getDefaultImageId(): String {
        return ioConfiguration.imageDataImageIdDefaultValue
    }

    fun getDefaultContainerId(): String {
        return ioConfiguration.imageDataContainerIdDefaultValue
    }

    fun getDefaultPort(): UShort {
        return ioConfiguration.imageDataPortDefaultValue.toUShort()
    }

    private fun createImageData(): HashMap<String, String> {
        val result = HashMap<String, String>()
        result[ioConfiguration.imageDataImageIdKey] = ioConfiguration.imageDataImageIdDefaultValue
        result[ioConfiguration.imageDataContainerIdKey] = ioConfiguration.imageDataContainerIdDefaultValue
        result[ioConfiguration.imageDataPortKey] = ioConfiguration.imageDataPortDefaultValue.toString()

        return result
    }

    private fun setImageDataByUuid(key: String, uuid: UUID, value: String) {
        ensureImageDataAvailableByUuid(uuid)
        (imageData[uuid]!!)[key] = value
        storeImageDataByUuid(uuid)
    }

    private fun <T> getImageDataByUuid(key: String, uuid: UUID, defaultValue: String): T {
        // check if data is available
        if (!ensureImageDataAvailableByUuid(uuid)) {
            return defaultValue as T
        }
        // now return the data
        return imageData[uuid]?.get(key) as T ?: defaultValue as T
    }

    private fun storeImageDataByUuid(uuid: UUID) {
        FileUtils.writeStringToFile(
                File(getMgmtDirByUuid(uuid) + File.separator + ioConfiguration.imageDataFileName),
                Klaxon().toJsonString(imageData[uuid]),
                Charset.forName("utf-8"),
                false
        )
    }

    private fun loadImageDataByUuid(uuid: UUID) {
        if (!doesImageDataExistByUuid(uuid)) {
            return
        }
        val loadedData = HashMap<String, String>()
        loadedData.putAll(Klaxon().parse<Map<String, String>>(
                File(getMgmtDirByUuid(uuid) + File.separator + ioConfiguration.imageDataFileName)
        )!!)
        imageData[uuid] = loadedData
    }

    private fun ensureImageDataAvailableByUuid(uuid: UUID): Boolean {
        // check if data is available
        if (!imageData.containsKey(uuid)) {
            loadImageDataByUuid(uuid)
        }
        // if data is still not available there is no image data for this uuid
        if (!imageData.containsKey(uuid)) {
            imageData[uuid] = createImageData()
            return false
        }
        return true
    }

    private fun doesImageDataExistByUuid(uuid: UUID): Boolean {
        return File(getMgmtDirByUuid(uuid) + File.separator + ioConfiguration.imageDataFileName).exists()
    }

    fun getFreePort(uuid: UUID): UShort {
        var port: UShort = 0u
        do {
            port = ThreadLocalRandom.current().nextInt(ioConfiguration.minimumPort.toInt(), UShort.MAX_VALUE.toInt() + 1).toUShort()
        } while (usedPorts.containsValue(port))
        usedPorts[uuid] = port
        return port
    }

    fun releasePort(uuid: UUID) {
        usedPorts.remove(uuid)
    }

}
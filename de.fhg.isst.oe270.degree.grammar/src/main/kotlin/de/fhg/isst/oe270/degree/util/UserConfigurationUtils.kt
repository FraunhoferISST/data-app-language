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
package de.fhg.isst.oe270.degree.util

import de.fhg.isst.oe270.degree.parsing.configuration.Configuration
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * This object provides various utility functions which simplify and unify the acces
 * to the D° user folder.
 */
object UserConfigurationUtils {

    /**
     * The used logger.
     */
    private val logger = LoggerFactory.getLogger(UserConfigurationUtils::class.java.simpleName)

    /**
     * Ensure that the D° configuration folder is present in user's home directory.
     *
     * @return true if the folder is present/was created, false otherwise
     */
    @JvmStatic
    fun initializeUserPropertiesFolder(): Boolean {
        checkAndCreateFolder(Configuration.USER_PROPERTIES_FOLDER)
        return true
    }

    /**
     * Ensure that the subsystem folder is present in D° user properties directory.
     *
     * @return true if the folder is present/was created, false otherwise
     */
    @JvmStatic
    fun initializeSubsystemFolder(): Boolean {
        checkAndCreateFolder(Configuration.SUBSYSTEM_FOLDER)
        checkAndCreateFolder(Configuration.SUBSYSTEM_EXTENSIONS_FOLDER)
        return true
    }

    /**
     * Update all core elements which are used within D°.
     *
     * Includes types and "the remaining".
     * (activities, policies, constraints, instances of the previous mentioned)
     */
    @JvmStatic
    fun updateSubSystems() {
        if (!updateSystem(Configuration.CORE_TYPES_FILE_PATH, retrieveCoreTypes())) {
            logger.error("Could not update core types.")
        }
        if (!updateSystem(Configuration.CORE_SUBSYSTEM_FILE_PATH, retrieveCoreElementDefinitions())) {
            logger.error("Could not update core element definitions.")
        }

        if (!updateSystem(
                Configuration.CORE_SUBSYSTEM_IMPLEMENTATION_FILE_PATH,
                retrieveCoreElementImplementations()
            )
        ) {
            logger.error("Could not update core element implementations.")
        }
    }

    /**
     * Update a given file with given content.
     * This function will create a backup, store the new value and delete the backup,
     * or restore the backup in case of any errors.
     *
     * @param filepath The file which will be updated
     * @param value The updated content of the file
     * @return true if everything went right, false otherwise
     */
    @JvmStatic
    fun updateSystem(filepath: String, value: ByteArray): Boolean {
        // create file if it is not already existing
        try {
            Files.createFile(Paths.get(filepath))
        } catch (e: java.nio.file.FileAlreadyExistsException) {
            logger.debug("Found previous version of core types.")
        }
        // create a backup of core types
        val result = backupFile(filepath)
        if (!result) {
            logger.debug("Could not backup core elements ($filepath). Going to continue anyways.")
        }
        if (value.isEmpty()) {
            logger.debug("Error retrieving current core elements."
                    + " Going to restore backup for $filepath.")
            restoreBackup(filepath)
            return false
        }
        if (!writeBytesToFile(value, filepath)) {
            logger.debug("Error writing retrieved elements to disk ($filepath). Going to restore backup.")
            restoreBackup(filepath)
            return false
        }
        // retrieved and stored current core types, remove the backup now
        removeBackup(filepath)

        logger.info("Successfully updated $filepath.")
        return true
    }

    /**
     * Write given bytes to a specific file.
     * File will be created if necessary or truncated if already existing.
     *
     * @param bytes The content which will be written to the file
     * @param filepath The file which will be written
     * @return true if writing is successful, false otherwise
     */
    private fun writeBytesToFile(bytes: ByteArray, filepath: String): Boolean {
        try {
            if (!File(filepath).exists()) {
                File(filepath).createNewFile()
            }
            Files.write(Paths.get(filepath), bytes)
        } catch (e: IOException) {
            logger.error("Error writing to file \"$filepath\".", e)
            return false
        }
        return true
    }

    /**
     * Retrieve the current definitions for core types.
     *
     * @return File which contains the most recent core type definitions as [ByteArray]
     */
    private fun retrieveCoreTypes(): ByteArray {
        return try {
            javaClass.classLoader.getResource("core.types.yaml")!!.readBytes()
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    /**
     * Retrieve the current definitions for core elements.
     * Contains Activities, Policies, Constraints,
     * ActivityInstances, PolicyInstances, and ConstraintInstances
     *
     * @return File which contains the most recent core element definitions as [ByteArray]
     */
    private fun retrieveCoreElementDefinitions(): ByteArray {
        return try {
            javaClass.classLoader.getResource("core.registry.yaml")!!.readBytes()
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    /**
     * Retrieve the current implementations for core elements.
     * Contains Activities, Policies, Constraints,
     * ActivityInstances, PolicyInstances, and ConstraintInstances
     *
     * @return File which contains the most recent core element definitions as [ByteArray]
     */
    private fun retrieveCoreElementImplementations(): ByteArray {
        return try {
            logger.warn("Retrieval of core implementations is not yet supported.")
            ByteArray(0)
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    /**
     * Backup a given file prior (e.g.) updating.
     *
     * @param filepath File for which a backup will be created
     * @return true if the bacup creation was successful, false otherwise
     */
    private fun backupFile(filepath: String): Boolean {
        val original = File(filepath)
        val backup = File(filepath + Configuration.BACKUP_SUFFIX)
        if (original.exists() && !original.isDirectory) {
            if (backup.exists() && !backup.isDirectory) {
                logger.debug("Found an old backup for file \"$filepath\". Going to delete it.")
                if (!backup.delete()) {
                    logger.debug("Could not delete old backup for file \"$filepath\".")
                    return false
                }
            }
            if (!original.renameTo(backup)) {
                logger.debug("Could not create backup file for file \"$filepath\"")
                return false
            }
        } else {
            logger.debug("File \"$filepath\" should be backed up but is either not existing or a directory.")
            return false
        }
        return true
    }

    /**
     * In case an update fails this function allows to restore a backup.
     * Requires a call to [backupFile] prior use.
     *
     * @param filepath The file which will be restored
     * @return true if the restoration was successful, false otherwise
     */
    private fun restoreBackup(filepath: String): Boolean {
        val original = File(filepath)
        val backup = File(filepath + Configuration.BACKUP_SUFFIX)
        if (original.exists() && !original.isDirectory) {
            logger.debug("Found a file with filename \"$filepath\""
                    + " which should be restored from backup. Going to delete it.")
            if (!original.delete()) {
                logger.debug("Could not delete file which should be restored from backup.")
                return false
            }
        }
        if (backup.exists() && !backup.isDirectory) {
            if (!backup.renameTo(original)) {
                logger.debug("Could not restore backup file for file \"$filepath\"")
                return false
            }
        } else {
            logger.debug("Tried to restore non existing backup for file \"$filepath\"")
            return false
        }
        return true
    }

    /**
     * Removes a file which was used for backup purposes.
     *
     * @param filepath The file which will be removed.
     * @return true if the removal was successful, false otherwise
     */
    private fun removeBackup(filepath: String): Boolean {
        val backup = File(filepath + Configuration.BACKUP_SUFFIX)
        if (backup.exists() && !backup.isDirectory) {
            if (!backup.delete()) {
                logger.debug("Could not delete backup for file \"$filepath\".")
                return false
            }
        } else {
            logger.debug("Tried to remove backup for file \"$filepath\" which is either not existing or a directory.")
            return false
        }
        return true
    }

    /**
     * Ensures that a given directory exists.
     * This function will create the directory (including parent directories) if it does not exist.
     *
     * @param dirpath The directory which needs to be created.
     */
    @JvmStatic
    private fun checkAndCreateFolder(dirpath: String) {
        val file = File(dirpath)
        if (!file.exists()) {
            logger.debug("Folder not found. Going to create it: \"$dirpath\"")
            file.mkdirs()
        }
    }

}

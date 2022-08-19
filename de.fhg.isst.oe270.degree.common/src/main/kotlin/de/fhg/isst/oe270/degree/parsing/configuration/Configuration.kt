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
package de.fhg.isst.oe270.degree.parsing.configuration

import java.io.File

/**
 * Collection of various globally used configuration keys.
 */
object Configuration {

    /**
     * Folder where runtime definition modules are stored.
     */
    private const val SUBSYSTEM_FOLDER_NAME = "runtimeDefinitions"

    /**
     * Folder where extensions are stored.
     */
    private const val EXTENSIONS_FOLDER_NAME = "extensions"

    /**
     * File suffix for backup files.
     */
    const val BACKUP_SUFFIX = "_BAK"

    /**
     * Namespace for default extension module.
     */
    const val CORE_NAME_SPACE = "core"

    /**
     * Filename (without extension) of core extension module.
     */
    const val CORE_FILE_NAME = "core"

    /**
     * File extension for files containing D° source.
     */
    const val DATA_APP_FILE_EXTENSION = "degree"

    /**
     * File extension for yaml files containing language elements.
     */
    const val SUBSYSTEM_FILE_EXTENSION = "yaml"

    /**
     * File extension for java archives that provide implementations for language elements.
     */
    private const val IMPLEMENTATION_FILE_EXTENSION = "jar"

    /**
     * File extension for property files used in D° applications.
     */
    const val PROPERTIES_FILE_EXTENSION = "applicationProperties"

    /**
     * Part of the filename for yaml files which contain type definitions.
     */
    const val TYPES_IDENTIFIER = ".types"

    /**
     * Part of the filename for yaml files which contain activity and policy definitions &
     * implementations.
     */
    const val SUBSYSTEM_IDENTIFIER = ".registry"

    /**
     * D° home folder path.
     */
    @JvmField
    val USER_PROPERTIES_FOLDER = System.getProperty("user.home") + File.separator + ".degree"

    /**
     * Default subsystem path.
     */
    @JvmField
    val SUBSYSTEM_FOLDER = USER_PROPERTIES_FOLDER + File.separator + SUBSYSTEM_FOLDER_NAME

    /**
     * Path to default activity and policy definitions.
     */
    @JvmField
    val CORE_SUBSYSTEM_FILE_PATH =
        SUBSYSTEM_FOLDER + File.separator + CORE_FILE_NAME + SUBSYSTEM_IDENTIFIER + "." + SUBSYSTEM_FILE_EXTENSION

    /**
     * Path to default activity and policy implementations.
     */
    @JvmField
    val CORE_SUBSYSTEM_IMPLEMENTATION_FILE_PATH =
        SUBSYSTEM_FOLDER + File.separator + CORE_FILE_NAME + SUBSYSTEM_IDENTIFIER + "." + IMPLEMENTATION_FILE_EXTENSION

    /**
     * Path to default type definitions.
     */
    @JvmField
    val CORE_TYPES_FILE_PATH =
        SUBSYSTEM_FOLDER + File.separator + CORE_FILE_NAME + TYPES_IDENTIFIER + "." + SUBSYSTEM_FILE_EXTENSION

    /**
     * Default path for extension modules.
     */
    @JvmField
    val SUBSYSTEM_EXTENSIONS_FOLDER = SUBSYSTEM_FOLDER + File.separator + EXTENSIONS_FOLDER_NAME
}

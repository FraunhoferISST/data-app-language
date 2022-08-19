/*
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
package de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * This enum contains various matching strategies which are available for permission evaluation. It
 * determines under which conditions a required permission with given attributes accepts a granting
 * permission whith its respective attributes.
 * <p>
 * In addition it contains various functions which are required at various points to prepare
 * permission parameters for evaluation.
 */
public enum PermissionMatchingStrategy {
    /**
     * Only exact matches.
     */
    EXACT_MATCH,
    /**
     * Only exact matches & <<ALL_FILES>> wildcard. Can be used with relative paths.
     */
    PATH_EXACT_MATCH,
    /**
     * Allow access to subdirectories. Maybe used with relative paths.
     */
    PATH_SUBDIR;

    /**
     * <<ALL_FILES>> wildcard value which can be used in file permissions.
     */
    public static final String ALL_FILES = "<<ALL_FILES>>";

    /**
     * The used logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PermissionMatchingStrategy.class.getSimpleName());

    /**
     * Ensure that a filePermission path is in a form that can be used by the sandbox.
     *
     * @param path Path from required permission
     * @return Absolute path to referred path
     */
    public static String preparePath(final String path) {
        String result = path;
        // special flag from file permission
        if (path.equals(ALL_FILES)) {
            return result;
        }
        // check if this path refers the current working directory
        if (path.equals("-") || path.equals("*")) {
            try {
                result = new File(".").getCanonicalPath() + File.separator + path;
            } catch (IOException e) {
                LOGGER.error(
                        "An error occurred during retrival of the current working directory.");
            }
        } else {
            // required for special characters
            String suffix = "";
            // ensure that the path is absolute
            if (path.endsWith("/-") || path.endsWith("/*")) {
                suffix = path.substring(path.length() - 2);
                result = path.substring(0, path.length() - 2);
            }
            result = Paths.get(result).toAbsolutePath() + suffix;
        }

        return result;
    }
}

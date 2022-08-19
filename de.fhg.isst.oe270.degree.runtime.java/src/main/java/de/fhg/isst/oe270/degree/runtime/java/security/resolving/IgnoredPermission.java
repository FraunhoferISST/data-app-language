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
package de.fhg.isst.oe270.degree.runtime.java.security.resolving;

import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * A permission which is ignored for some reason. For example file reading permissions from class
 * loaders are ignored.
 */
@EqualsAndHashCode
@Getter
public class IgnoredPermission {

    /**
     * The used logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("IgnoredPermission");

    /**
     * Prefix for all ignored permissions.
     */
    private static final String PREFIX = "degree.securitymanager.ignore";

    /**
     * Separator used in config file for ignored permission entries.
     */
    private static final String SEPARATOR = ",";

    /**
     * Map with ignored permission types as keys and values contains lists of classes which are
     * allowed to ignore the permission.
     */
    @Getter
    private static final HashMap<DegreePermissionType, HashMap<String, ArrayList<String>>>
            IGNORED_PERMISSION_MAP = new HashMap<>();

    /**
     * The D° permission type.
     */
    private final DegreePermissionType permission;

    /**
     * The affected class.
     */
    private final String className;

    /**
     * The affected method.
     */
    private final String methodName;

    /**
     * Construct a new ignored permission with given type for given class and method.
     *
     * @param type   the permission type to ignore
     * @param clazz  the class allowed to ignore
     * @param method the method allowed to ignore
     */
    public IgnoredPermission(
            final DegreePermissionType type,
            final String clazz,
            final String method) {
        this.permission = type;
        this.className = clazz;
        this.methodName = method;
    }

    /**
     * Initialize the ignored permissions by loading the corresponding configuration file.
     */
    public static void init() {
        Properties properties = new Properties();
        try {
            properties.load(IgnoredPermission.class.getResourceAsStream(
                    "/ignored_elements.properties"));
        } catch (IOException e) {
            LOGGER.error("An error occurred while loading the ignored permission objects.", e);
            return;
        }

        final int positionsPerEntry = 3;
        properties.forEach((key, value) -> {
            if (((String) key).startsWith(PREFIX)
                    && ((String) value).split(SEPARATOR).length == positionsPerEntry) {
                String[] values = ((String) value).split(SEPARATOR);

                HashMap<String, ArrayList<String>> classMap;
                DegreePermissionType type = DegreePermissionType.valueOf(values[0].trim());
                if (!IGNORED_PERMISSION_MAP.containsKey(type)) {
                    IGNORED_PERMISSION_MAP.put(type, new HashMap<>());
                }
                classMap = IGNORED_PERMISSION_MAP.get(type);
                if (!classMap.containsKey(values[1].trim())) {
                    classMap.put(values[1].trim(), new ArrayList<>());
                }
                classMap.get(values[1].trim()).add(values[2].trim());
                // load the ignored class to prevent later waiting
                try {
                    IgnoredPermission.class.getClassLoader().loadClass(values[1].trim());
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Tried to add an ignored permission for unknown class '"
                            + values[1].trim() + "'.");
                }
            }
        });
    }

}

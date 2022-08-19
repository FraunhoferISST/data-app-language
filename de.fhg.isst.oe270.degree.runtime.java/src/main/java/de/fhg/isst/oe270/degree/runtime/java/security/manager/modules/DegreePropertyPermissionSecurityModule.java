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
package de.fhg.isst.oe270.degree.runtime.java.security.manager.modules;

import de.fhg.isst.oe270.degree.runtime.java.exceptions.security.DegreeUnsupportedSecurityFeatureException;
import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.PropertyPermission;

/**
 * Module for {@link PropertyPermission} of the D° security manager.
 */
public final class DegreePropertyPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreePropertyPermissionSecurityModule() {
    }

    /**
     * A java.util.PropertyPermission is for property permissions.
     * <p>
     * The name is the name of the property ("java.home", "os.name", etc). The naming convention
     * follows the hierarchical property naming convention. Also, an asterisk may appear at the end
     * of the name, following a ".", or by itself, to signify a wildcard match. For example:
     * "java.*" or "*" is valid, "*java" or "a*b" is not valid.
     * <p>
     * The actions to be granted are passed to the constructor in a string containing a list of zero
     * or more comma- separated keywords. The possible keywords are "read" and "write".
     * <p>
     * The actions string is converted to lowercase before processing.
     * <p>
     * Care should be taken before granting code permission to access certain system properties. For
     * example, granting permission to access the "java.home" system property gives potentially
     * malevolent code sensitive information about the system environment (the location of the
     * runtime environment's directory). Also, granting permission to access the "user.name" and
     * "user.home" system properties gives potentially malevolent code sensitive information about
     * the user environment (the user's account name and home directory).
     *
     * @param permission the PropertyPermission which will be checked
     * @return a list of required permissions
     * @see PropertyPermission
     */
    public static List<RequiredPermission> checkPropertyPermission(
            final PropertyPermission permission) {
        String[] targetActions = permission.getActions().split(",");

        List<RequiredPermission> requiredPermissions = new ArrayList<>();

        for (String targetAction : targetActions) {
            switch (targetAction) {
                case "read":
                    requiredPermissions.add(checkRead(permission));
                    break;
                case "write":
                    requiredPermissions.add(checkWrite(permission));
                    break;
                default:
                    throw new DegreeUnsupportedSecurityFeatureException();
            }
        }
        return requiredPermissions;
    }

    /**
     * What the permission allows: Permission to read. Allows System.getProperty to be called.
     *
     * @param permission The PropertyPermission that will be checked
     * @return a required permission
     */
    private static RequiredPermission checkRead(final PropertyPermission permission) {
        return null;
    }

    /**
     * What the permission allows: Permission to write. Allows System.setProperty to be called.
     *
     * @param permission The PropertyPermission that will be checked
     * @return a required permission
     */
    private static RequiredPermission checkWrite(final PropertyPermission permission) {
        return null;
    }
}

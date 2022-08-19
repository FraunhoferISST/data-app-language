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

import java.lang.management.ManagementPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link ManagementPermission} of the D° security manager.
 */
public final class DegreeManagementPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeManagementPermissionSecurityModule() {
    }

    /**
     * The permission which the SecurityManager will check when code that is running with a
     * SecurityManager calls methods defined in the management interface for the Java platform.
     * <p>
     * Programmers do not normally create ManagementPermission objects directly. Instead they are
     * created by the security policy code based on reading the security policy file.
     *
     * @param permission the ManagementPermission which will be checked
     * @return a list of required permissions
     * @see ManagementPermission
     */
    public static List<RequiredPermission> checkManagementPermission(
            final ManagementPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "control":
                return checkControl(permission);
            case "monitor":
                return checkMonitor(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: Ability to control the runtime characteristics of the Java
     * virtual machine, for example, enabling and disabling the verbose output for the class loading
     * or memory system, setting the threshold of a memory pool, and enabling and disabling the
     * thread contention monitoring support. Some actions controlled by this permission can disclose
     * information about the running application, like the -verbose:class flag.
     * <p>
     * Risk of Allowing this permission This allows an attacker to control the runtime
     * characteristics of the Java virtual machine and cause the system to misbehave. An attacker
     * can also access some information related to the running application.
     *
     * @param permission The ManagementPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkControl(final ManagementPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Ability to retrieve runtime information about the Java virtual
     * machine such as thread stack trace, a list of all loaded class names, and input arguments to
     * the Java virtual machine.
     * <p>
     * Risk of Allowing this permission This allows malicious code to monitor runtime information
     * and uncover vulnerabilities.
     *
     * @param permission The ManagementPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkMonitor(final ManagementPermission permission) {
        return new ArrayList<>();
    }

}

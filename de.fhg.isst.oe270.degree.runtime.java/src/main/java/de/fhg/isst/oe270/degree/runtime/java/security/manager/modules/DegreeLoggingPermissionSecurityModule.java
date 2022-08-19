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
import java.util.logging.LoggingPermission;

/**
 * Module for {@link LoggingPermission} of the D° security manager.
 */
public final class DegreeLoggingPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeLoggingPermissionSecurityModule() {
    }

    /**
     * A SecurityManager will check the java.util.logging.LoggingPermission object when code running
     * with a SecurityManager calls one of the logging control methods (such as Logger.setLevel).
     * Currently there is only one named LoggingPermission, "control". control grants the ability to
     * control the logging configuration; for example by adding or removing Handlers, by adding or
     * removing Filters, or by changing logging levels.
     * <p>
     * Normally you do not create LoggingPermission objects directly; instead they are created by
     * the security policy code based on reading the security policy file.
     *
     * @param permission the LoggingPermission which will be checked
     * @return a list of required permissions
     * @see LoggingPermission
     */
    public static List<RequiredPermission> checkLoggingPermission(
            final LoggingPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "control":
                return checkControl(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * @param permission The LoggingPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkControl(final LoggingPermission permission) {
        return new ArrayList<>();
    }

}

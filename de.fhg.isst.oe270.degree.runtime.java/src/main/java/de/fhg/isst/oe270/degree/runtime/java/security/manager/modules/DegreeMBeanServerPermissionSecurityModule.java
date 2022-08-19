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

import javax.management.MBeanServerPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link MBeanServerPermission} of the D° security manager.
 */
public final class DegreeMBeanServerPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeMBeanServerPermissionSecurityModule() {
    }

    /**
     * @param permission the MBeanServerPermission which will be checked
     * @return a list of required permissions
     * @see MBeanServerPermission
     */
    public static List<RequiredPermission> checkMBeanServerPermission(
            final MBeanServerPermission permission) {
        String[] targetNames = permission.getName().split(",");

        List<RequiredPermission> requiredPermissions = new ArrayList<>();

        for (String targetName : targetNames) {
            switch (targetName) {
                case "createMBeanServer":
                    requiredPermissions.add(checkCreateMBeanServer(permission));
                    break;
                case "findMBeanServer":
                    requiredPermissions.add(checkFindMBeanServer(permission));
                    break;
                case "newMBeanServer":
                    requiredPermissions.add(checkNewMBeanServer(permission));
                    break;
                case "releaseMBeanServer":
                    requiredPermissions.add(checkReleaseMBeanServer(permission));
                    break;
                default:
                    throw new DegreeUnsupportedSecurityFeatureException();
            }
        }

        return requiredPermissions;
    }

    /**
     * Create a new MBeanServer object using the method MBeanServerFactory.createMBeanServer() or
     * MBeanServerFactory.createMBeanServer(java.lang.String).
     *
     * @param permission The MBeanServerPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkCreateMBeanServer(
            final MBeanServerPermission permission) {
        return null;
    }

    /**
     * Find an MBeanServer with a given name, or all MBeanServers in this JVM, using the method
     * MBeanServerFactory.findMBeanServer(java.lang.String).
     *
     * @param permission The MBeanServerPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkFindMBeanServer(final MBeanServerPermission permission) {
        return null;
    }

    /**
     * Create a new MBeanServer object without keeping a reference to it, using the method
     * MBeanServerFactory.newMBeanServer() or MBeanServerFactory.newMBeanServer(java.lang.String).
     *
     * @param permission The MBeanServerPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkNewMBeanServer(final MBeanServerPermission permission) {
        return null;
    }

    /**
     * Remove the MBeanServerFactory's reference to an MBeanServer, using the method
     * MBeanServerFactory.releaseMBeanServer(javax.management.MBeanServer).
     *
     * @param permission The MBeanServerPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkReleaseMBeanServer(
            final MBeanServerPermission permission) {
        return null;
    }

}

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
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType;

import java.net.SocketPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link SocketPermission} of the D° security manager.
 */
public final class DegreeSocketPermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeSocketPermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * @param permission the SocketPermission which will be checked
     * @return a list of required permissions
     * @see SocketPermission
     */
    public static List<RequiredPermission> checkSocketPermission(
            final SocketPermission permission) {
        String[] targetActions = permission.getActions().split(",");

        List<RequiredPermission> requiredPermissions = new ArrayList<>();

        for (String targetAction : targetActions) {
            switch (targetAction) {
                case "accept":
                    requiredPermissions.add(checkAccept(permission));
                    break;
                case "connect":
                    requiredPermissions.add(checkConnect(permission));
                    break;
                case "listen":
                    requiredPermissions.add(checkListen(permission));
                    break;
                case "resolve":
                    requiredPermissions.add(checkResolve(permission));
                    break;
                default:
                    throw new DegreeUnsupportedSecurityFeatureException();
            }
        }

        return requiredPermissions;
    }

    /**
     * @param permission The SocketPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkAccept(final SocketPermission permission) {
        return new RequiredPermission(
                DegreePermissionType.ACCEPT_SOCKET,
                permission.getName()
        );
    }

    /**
     * @param permission The SocketPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkConnect(final SocketPermission permission) {
        return new RequiredPermission(
                DegreePermissionType.CONNECT_SOCKET,
                permission.getName()
        );
    }

    /**
     * @param permission The SocketPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkListen(final SocketPermission permission) {
        return new RequiredPermission(
                DegreePermissionType.LISTEN_SOCKET,
                permission.getName()
        );
    }

    /**
     * @param permission The SocketPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkResolve(final SocketPermission permission) {
        return new RequiredPermission(
                DegreePermissionType.RESOLVE_SOCKET,
                permission.getName()
        );
    }

}

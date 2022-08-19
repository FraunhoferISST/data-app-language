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

import java.net.URLPermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Module for {@link URLPermission} of the D° security manager.
 */
public final class DegreeURLPermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeURLPermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * @param permission the URLPermission which will be checked
     * @return a list of required permissions
     * @see URLPermission
     */
    public static List<RequiredPermission> checkURLPermission(final URLPermission permission) {
        String[] targetActions = permission.getActions().split(",");

        String[] allowedActionsArray = {"GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT",
                "OPTIONS", "TRACE", "PATCH"};
        List<String> allowedActions = Arrays.asList(allowedActionsArray);
        ArrayList<String> parsedActions = new ArrayList();
        String currentAction = "";
        for (String targetAction : targetActions) {
            if (allowedActions.contains(targetAction.split(":")[0])) {
                if (!currentAction.isEmpty()) {
                    parsedActions.add(currentAction);
                    currentAction = "";
                }
                currentAction = targetAction;
            } else {
                currentAction += "," + targetAction;
            }
        }
        parsedActions.add(currentAction);

        List<RequiredPermission> requiredPermissions = new ArrayList<>();

        for (String targetAction : parsedActions) {
            if (targetAction.startsWith("GET")) {
                requiredPermissions.add(checkGET(permission));
                break;
            } else if (targetAction.startsWith("HEAD")) {
                requiredPermissions.add(checkHEAD(permission));
                break;
            } else if (targetAction.startsWith("POST")) {
                requiredPermissions.add(checkPOST(permission));
                break;
            } else if (targetAction.startsWith("PUT")) {
                requiredPermissions.add(checkPUT(permission));
                break;
            } else if (targetAction.startsWith("DELETE")) {
                requiredPermissions.add(checkDELETE(permission));
                break;
            } else if (targetAction.startsWith("CONNECT")) {
                requiredPermissions.add(checkCONNECT(permission));
                break;
            } else if (targetAction.startsWith("OPTIONS")) {
                requiredPermissions.add(checkOPTIONS(permission));
                break;
            } else if (targetAction.startsWith("TRACE")) {
                requiredPermissions.add(checkTRACE(permission));
                break;
            } else if (targetAction.startsWith("PATCH")) {
                requiredPermissions.add(checkPATCH(permission));
                break;
            } else {
                throw new DegreeUnsupportedSecurityFeatureException();
            }
        }
        return requiredPermissions;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkGET(final URLPermission permission) {
        return null;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkHEAD(final URLPermission permission) {
        return null;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkPOST(final URLPermission permission) {
        return null;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkPUT(final URLPermission permission) {
        return null;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkDELETE(final URLPermission permission) {
        return null;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkCONNECT(final URLPermission permission) {
        return null;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkOPTIONS(final URLPermission permission) {
        return null;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkTRACE(final URLPermission permission) {
        return null;
    }

    /**
     * @param permission The URLPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkPATCH(final URLPermission permission) {
        return null;
    }

}

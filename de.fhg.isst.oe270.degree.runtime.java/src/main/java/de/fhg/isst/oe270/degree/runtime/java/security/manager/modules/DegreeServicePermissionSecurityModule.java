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

import javax.security.auth.kerberos.ServicePermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link ServicePermission} of the D° security manager.
 */
public final class DegreeServicePermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeServicePermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * The javax.security.auth.kerberos.ServicePermission class is used to protect Kerberos services
     * and the credentials necessary to access those services. There is a one to one mapping of a
     * service principal and the credentials necessary to access the service. Therefore granting
     * access to a service principal implicitly grants access to the credential necessary to
     * establish a security context with the service principal. This applies regardless of whether
     * the credentials are in a cache or acquired via an exchange with the KDC. The credential can
     * be either a ticket granting ticket, a service ticket or a secret key from a key table. A
     * ServicePermission contains a service principal name and a list of actions which specify the
     * context the credential can be used within.
     * <p>
     * The service principal name is the canonical name of the KereberosPrincipal supplying the
     * service, that is the KerberosPrincipal represents a Kerberos service principal. This name is
     * treated in a case sensitive manner.
     * <p>
     * Granting this permission implies that the caller can use a cached credential (Ticket Granting
     * Ticket, service ticket or secret key) within the context designated by the action. In the
     * case of the TGT, granting this permission also implies that the TGT can be obtained by an
     * Authentication Service exchange.
     * <p>
     * For example, to specify the permission to access to the TGT to initiate a security context
     * the permission is constructed as follows: ServicePermission("krbtgt/EXAMPLE.COM@EXAMPLE.COM",
     * "initiate"); To obtain a service ticket to initiate a context with the "host" service the
     * permission is constructed as follows: ServicePermission("host/foo.example.com@EXAMPLE.COM",
     * "initiate"); For a Kerberized server the action is "accept". For example, the permission
     * necessary to access and use the secret key of the Kerberized "host" service (telnet and the
     * likes) would be constructed as follows: ServicePermission("host/foo.example.com@EXAMPLE.COM",
     * "accept");
     *
     * @param permission the ServicePermission which will be checked
     * @return a list of required permissions
     * @see ServicePermission
     */
    public static List<RequiredPermission> checkServicePermission(
            final ServicePermission permission) {
        String[] targetActions = permission.getActions().split(",");

        List<RequiredPermission> requiredPermissions = new ArrayList<>();

        for (String targetAction : targetActions) {
            switch (targetAction) {
                case "initiate":
                    requiredPermissions.add(checkInitiate(permission));
                    break;
                case "accept":
                    requiredPermissions.add(checkAccept(permission));
                    break;
                default:
                    throw new DegreeUnsupportedSecurityFeatureException();
            }
        }
        return requiredPermissions;
    }

    /**
     * Allows the caller to use the credential to initiate a security context with a service
     * principal.
     *
     * @param permission The ServicePermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkInitiate(final ServicePermission permission) {
        return null;
    }

    /**
     * Allows the caller to use the credential to accept security context as a particular
     * principal.
     *
     * @param permission The ServicePermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkAccept(final ServicePermission permission) {
        return null;
    }

}

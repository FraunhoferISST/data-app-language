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

import javax.security.auth.AuthPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link AuthPermission} of the D° security manager.
 */
public final class DegreeAuthPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeAuthPermissionSecurityModule() {
    }

    /**
     * The javax.security.auth.AuthPermission class is for authentication permissions. An
     * AuthPermission contains a name (also referred to as a "target name") but no actions list; you
     * either have the named permission or you don't.
     * <p>
     * Currently the AuthPermission object is used to guard access to the Subject,
     * SubjectDomainCombiner, LoginContext and Configuration objects.
     *
     * @param permission the AuthPermission which will be checked
     * @return a list of required permissions
     * @see AuthPermission
     */
    public static List<RequiredPermission> checkAuthPermission(final AuthPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "doAs":
                return checkDoAs(permission);
            case "doAsPrivileged":
                return checkDoAsPrivileged(permission);
            case "getSubject":
                return checkGetSubject(permission);
            case "getSubjectFromDomainCombiner":
                return checkGetSubjectFromDomainCombiner(permission);
            case "setReadOnly":
                return checkSetReadOnly(permission);
            case "modifyPrincipals":
                return checkModifyPrincipals(permission);
            case "modifyPublicCredentials":
                return checkModifyPublicCredentials(permission);
            case "modifyPrivateCredentials":
                return checkModifyPrivateCredentials(permission);
            case "refreshCredential":
                return checkRefreshCredential(permission);
            case "destroyCredential":
                return checkDestroyCredential(permission);
            case "getLoginConfiguration":
                return checkGetLoginConfiguration(permission);
            case "setLoginConfiguration":
                return checkSetLoginConfiguration(permission);
            case "refreshLoginConfiguration":
                return checkRefreshLoginConfiguration(permission);
            default:
                if (targetName.startsWith("createLoginContext")) {
                    return checkCreateLoginContext(permission);
                } else if (targetName.startsWith("createLoginConfiguration")) {
                    return checkCreateLoginConfiguration(permission);
                } else {
                    throw new DegreeUnsupportedSecurityFeatureException();
                }
        }
    }

    /**
     * What the permission allows: Invocation of the Subject.doAs methods
     * <p>
     * Risk of Allowing this permission This enables an application to invoke code (Actions) under
     * the identity of any Subject specified to the doAs method.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkDoAs(final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Invocation of the Subject.doAsPrivileged methods
     * <p>
     * Risk of Allowing this permission This enables an application to invoke code (Actions) under
     * the identity of any Subject specified to the doAsPrivileged method. Additionally, the caller
     * may remove itself from the call stack (and hence from subsequent security decisions) if it
     * passes null as the AccessControlContext.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkDoAsPrivileged(final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieving the Subject from the provided AccessControlContext
     * <p>
     * Risk of Allowing this permission This permits an application to gain access to an
     * authenticated Subject. The application can then access the Subject's authenticated Principals
     * and public credentials.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetSubject(final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieving the Subject from a SubjectDomainCombiner
     * <p>
     * Risk of Allowing this permission This permits an application to gain access to the
     * authenticated Subject associated with a SubjectDomainCombiner. The application can then
     * access the Subject's authenticated Principals and public credentials.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetSubjectFromDomainCombiner(
            final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting a Subject read-only
     * <p>
     * Risk of Allowing this permission This permits an application to set a Subject's Principal,
     * public credential and private credential sets to be read-only. This can be potentially used
     * as a type of denial of service attack.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetReadOnly(final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Make modifications to a Subject's Principal set
     * <p>
     * Risk of Allowing this permission Access control decisions are based on the Principals
     * associated with a Subject. This permission permits an application to make any modifications
     * to a Subject's Principal set, thereby affecting subsequent security decisions.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkModifyPrincipals(final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Make modifications to a Subject's public credential set
     * <p>
     * Risk of Allowing this permission This permission permits an application to add or remove
     * public credentials from a Subject. This may affect code that relies on the proper set of
     * private credentials to exist in that Subject.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkModifyPublicCredentials(
            final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Make modifications to a Subject's private credential set
     * <p>
     * Risk of Allowing this permission This permission permits an application to add or remove
     * private credentials from a Subject. This may affect code that relies on the proper set of
     * private credentials to exist in that Subject.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkModifyPrivateCredentials(
            final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Refresh a credential Object that implements the Refreshable
     * interface
     * <p>
     * Risk of Allowing this permission This permission permits an application to refresh a
     * credential that is intended to expire.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkRefreshCredential(
            final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Destroy a credential Object that implements the Destroyable
     * interface
     * <p>
     * Risk of Allowing this permission This permission permits an application to potentially
     * destroy a credential as a denial of service attack.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkDestroyCredential(
            final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Instantiate a LoginContext with the specified name
     * <p>
     * Risk of Allowing this permission For security purposes, an administrator might not want an
     * application to be able to authenticate to any LoginModule. This permission permits an
     * application to authenticate to the LoginModules configured for the specified name.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCreateLoginContext(
            final AuthPermission permission) {
        String name = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieve the system-wide login Configuration
     * <p>
     * Risk of Allowing this permission Allows an application to determine all the LoginModules that
     * are configured for every application in the system.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetLoginConfiguration(
            final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Set the system-wide login Configuration
     * <p>
     * Risk of Allowing this permission Allows an application to configure the LoginModules for
     * every application in the system.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetLoginConfiguration(
            final AuthPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Obtain a Configuration object via Configuration.getInstance
     * <p>
     * Risk of Allowing this permission Allows an application to see all the LoginModules that are
     * specified in the configuration.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCreateLoginConfiguration(
            final AuthPermission permission) {
        String configurationType = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Refresh the system-wide login Configuration
     * <p>
     * Risk of Allowing this permission Allows an application to refresh the login Configuration.
     *
     * @param permission The AuthPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkRefreshLoginConfiguration(
            final AuthPermission permission) {
        return new ArrayList<>();
    }

}

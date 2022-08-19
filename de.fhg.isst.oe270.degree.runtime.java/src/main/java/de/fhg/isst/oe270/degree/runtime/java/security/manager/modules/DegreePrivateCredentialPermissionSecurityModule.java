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

import javax.security.auth.PrivateCredentialPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link PrivateCredentialPermission} of the D° security manager.
 */
public final class DegreePrivateCredentialPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreePrivateCredentialPermissionSecurityModule() {
    }

    /**
     * The javax.security.auth.PrivateCredentialPermission class is used to protect access to
     * private Credentials belonging to a particular Subject. The Subject is represented by a Set of
     * Principals. The target name of this Permission specifies a Credential class name, and a Set
     * of Principals. The only valid value for this Permission's actions is, "read". The target name
     * must abide by the following syntax:
     * <p>
     * CredentialClass {PrincipalClass "PrincipalName"}* For example, the following permission
     * grants access to the com.sun.PrivateCredential owned by Subjects which have a
     * com.sun.Principal with the name, "duke". Note: Although this example, as well as all the
     * examples below, do not contain Codebase, SignedBy, or Principal information in the grant
     * statement (for simplicity reasons), actual policy configurations should specify that
     * information when appropriate.
     * <p>
     * grant { permission javax.security.auth.PrivateCredentialPermission "com.sun.PrivateCredential
     * com.sun.Principal \"duke\"", "read"; }; If CredentialClass is "*", then access is granted to
     * all private Credentials belonging to the specified Subject. If "PrincipalName" is "*", then
     * access is granted to the specified Credential owned by any Subject that has the specified
     * Principal (the actual PrincipalName doesn't matter). For example, the following grants access
     * to the a.b.Credential owned by any Subject that has an a.b.Principal. grant { permission
     * javax.security.auth.PrivateCredentialPermission "a.b.Credential a.b.Principal "*"", "read";
     * }; If both the PrincipalClass and "PrincipalName" are "*", then access is granted to the
     * specified Credential owned by any Subject. In addition, the PrincipalClass/PrincipalName
     * pairing may be repeated: grant { permission javax.security.auth.PrivateCredentialPermission
     * "a.b.Credential a.b.Principal "duke" c.d.Principal "dukette"", "read"; }; The above code
     * grants access to the private Credential, "a.b.Credential", belonging to a Subject with at
     * least two associated Principals: "a.b.Principal" with the name, "duke", and "c.d.Principal",
     * with the name, "dukette".
     *
     * @param permission the PrivateCredentialPermission which will be checked
     * @return a list of required permissions
     * @see PrivateCredentialPermission
     */
    public static List<RequiredPermission> checkPrivateCredentialPermission(
            final PrivateCredentialPermission permission) {
        String[] targetActions = permission.getActions().split(",");

        List<RequiredPermission> requiredPermissions = new ArrayList<>();

        for (String targetAction : targetActions) {
            switch (targetAction) {
                case "read":
                    requiredPermissions.add(checkRead(permission));
                    break;
                default:
                    throw new DegreeUnsupportedSecurityFeatureException();
            }
        }

        return requiredPermissions;
    }

    /**
     * @param permission The PrivateCredentialPermission that will be checked
     * @return a required permission
     */
    private static RequiredPermission checkRead(final PrivateCredentialPermission permission) {
        return null;
    }
}

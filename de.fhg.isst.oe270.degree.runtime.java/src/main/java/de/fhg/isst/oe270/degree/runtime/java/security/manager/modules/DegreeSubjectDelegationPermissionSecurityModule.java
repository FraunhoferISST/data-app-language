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

import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;

import javax.management.remote.SubjectDelegationPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link SubjectDelegationPermission} of the D° security manager.
 */
public final class DegreeSubjectDelegationPermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeSubjectDelegationPermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Permission required by an authentication identity to perform operations on behalf of an
     * authorization identity.
     * <p>
     * A SubjectDelegationPermission contains a name (also referred to as a "target name") but no
     * actions list; you either have the named permission or you don't.
     * <p>
     * The target name is the name of the authorization principal classname followed by a period and
     * the authorization principal name, that is "PrincipalClassName.PrincipalName".
     * <p>
     * An asterisk may appear by itself, or if immediately preceded by a "." may appear at the end
     * of the target name, to signify a wildcard match.
     * <p>
     * For example, "*", "javax.management.remote.JMXPrincipal.*" and
     * "javax.management.remote.JMXPrincipal.delegate"
     * are valid target names. The first one denotes any principal name from any principal class,
     * the second one denotes any principal name of the concrete principal class
     * javax.management.remote.JMXPrincipal and the third one denotes a concrete principal name
     * delegate of the concrete principal class javax.management.remote.JMXPrincipal.
     *
     * @param permission the SubjectDelegationPermission which will be checked
     * @return a list of required permissions
     * @see SubjectDelegationPermission
     */
    public static List<RequiredPermission> checkSubjectDelegationPermission(
            final SubjectDelegationPermission permission) {
        String targetName = permission.getName();

        return new ArrayList<>();
    }

}

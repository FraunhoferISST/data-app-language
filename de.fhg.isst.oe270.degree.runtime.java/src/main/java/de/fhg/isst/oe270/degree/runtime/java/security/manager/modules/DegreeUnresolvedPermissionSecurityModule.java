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

import java.security.UnresolvedPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link UnresolvedPermission} of the D° security manager.
 */
public final class DegreeUnresolvedPermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeUnresolvedPermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * The java.security.UnresolvedPermission class is used to hold Permissions that were
     * "unresolved" when the Policy was initialized. An unresolved permission is one whose actual
     * Permission class does not yet exist at the time the Policy is initialized (see below).
     * <p>
     * The policy for a Java runtime (specifying which permissions are available for code from
     * various principals) is represented by a Policy object. Whenever a Policy is initialized or
     * refreshed, Permission objects of appropriate classes are created for all permissions allowed
     * by the Policy.
     * <p>
     * Many permission class types referenced by the policy configuration are ones that exist
     * locally (i.e., ones that can be found on CLASSPATH). Objects for such permissions can be
     * instantiated during Policy initialization. For example, it is always possible to instantiate
     * a java.io.FilePermission, since the FilePermission class is found on the CLASSPATH.
     * <p>
     * Other permission classes may not yet exist during Policy initialization. For example, a
     * referenced permission class may be in a JAR file that will later be loaded. For each such
     * class, an UnresolvedPermission is instantiated. Thus, an UnresolvedPermission is essentially
     * a "placeholder" containing information about the permission.
     * <p>
     * Later, when code calls AccessController.checkPermission on a permission of a type that was
     * previously unresolved, but whose class has since been loaded, previously-unresolved
     * permissions of that type are "resolved". That is, for each such UnresolvedPermission, a new
     * object of the appropriate class type is instantiated, based on the information in the
     * UnresolvedPermission. This new object replaces the UnresolvedPermission, which is removed.
     *
     * @param permission the UnresolvedPermission which will be checked
     * @return a list of required permissions
     * @see UnresolvedPermission
     */
    public static List<RequiredPermission> checkUnresolvedPermission(
            final UnresolvedPermission permission) {
        return new ArrayList<>();
    }

}

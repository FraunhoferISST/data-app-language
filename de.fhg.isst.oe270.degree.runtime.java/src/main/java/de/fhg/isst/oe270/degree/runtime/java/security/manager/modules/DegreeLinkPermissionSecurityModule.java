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

import java.nio.file.LinkPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link LinkPermission} of the D° security manager.
 */
public final class DegreeLinkPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeLinkPermissionSecurityModule() {
    }

    /**
     * The Permission class for link creation operations.
     *
     * @param permission the LinkPermission which will be checked
     * @return a list of required permissions
     * @see LinkPermission
     */
    public static List<RequiredPermission> checkLinkPermission(final LinkPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "hard":
                return checkHard(permission);
            case "symbolic":
                return checkSymbolic(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: Ability to add an existing file to a directory. This is sometimes
     * known as creating a link, or hard link.
     * <p>
     * Risk of Allowing this permission Extreme care should be taken when granting this permission.
     * It allows linking to any file or directory in the file system thus allowing the attacker
     * access to all files.
     *
     * @param permission The LinkPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkHard(final LinkPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Ability to create symbolic links.
     * <p>
     * Risk of Allowing this permission Extreme care should be taken when granting this permission.
     * It allows linking to any file or directory in the file system thus allowing the attacker to
     * access to all files.
     *
     * @param permission The LinkPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSymbolic(final LinkPermission permission) {
        return new ArrayList<>();
    }


}

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

import javax.management.MBeanTrustPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link MBeanTrustPermission} of the D° security manager.
 */
public final class DegreeMBeanTrustPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeMBeanTrustPermissionSecurityModule() {
    }

    /**
     * This permission represents "trust" in a signer or codebase.
     * <p>
     * MBeanTrustPermission contains a target name but no actions list. A single target name,
     * "register", is defined for this permission. The target "*" is also allowed, permitting
     * "register" and any future targets that may be defined. Only the null value or the empty
     * string are allowed for the action to allow the policy object to create the permissions
     * specified in the policy file.
     * <p>
     * If a signer, or codesource is granted this permission, then it is considered a trusted source
     * for MBeans. Only MBeans from trusted sources may be registered in the MBeanServer.
     *
     * @param permission the MBeanTrustPermission which will be checked
     * @return a list of required permissions
     * @see MBeanTrustPermission
     */
    public static List<RequiredPermission> checkMBeanTrustPermission(
            final MBeanTrustPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "register":
                return checkRegister(permission);
            case "*":
                return checkAll(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * @param permission The MBeanTrustPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkRegister(final MBeanTrustPermission permission) {
        return new ArrayList<>();
    }

    /**
     * @param permission The MBeanTrustPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkAll(final MBeanTrustPermission permission) {
        return new ArrayList<>();
    }

}

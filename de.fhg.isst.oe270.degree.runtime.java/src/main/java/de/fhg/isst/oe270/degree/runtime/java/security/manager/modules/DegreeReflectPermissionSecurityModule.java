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

import java.lang.reflect.ReflectPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link ReflectPermission} of the D° security manager.
 */
public final class DegreeReflectPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeReflectPermissionSecurityModule() {
    }

    /**
     * A java.lang.reflect.ReflectPermission is for reflective operations. A ReflectPermission is a
     * named permission and has no actions. The suppressAccessChecks name allows suppressing the
     * standard language access checks -- for public, default (package) access, protected, and
     * private members -- performed by reflected objects at their point of use. The
     * newProxyInPackage.{package name} provide the ability to create a proxy instance in the
     * specified package of which the non-public interface that the proxy class implements.
     *
     * @param permission the ReflectPermission which will be checked
     * @return a list of required permissions
     * @see ReflectPermission
     */
    public static List<RequiredPermission> checkReflectPermission(
            final ReflectPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "suppressAccessChecks":
                return checkSuppressAccessChecks(permission);
            default:
                if (targetName.startsWith("newProxyInPackage")) {
                    return checkNewProxyInPackage(permission);
                } else {
                    throw new DegreeUnsupportedSecurityFeatureException();
                }
        }
    }

    /**
     * What the permission allows: Warning: Extreme caution should be taken before granting this
     * permission to code, for it provides the ability to access fields and invoke methods in a
     * class. This includes not only public, but protected and private fields and methods as well.
     * <p>
     * Risk of Allowing this permission This is dangerous in that information (possibly
     * confidential) and methods normally unavailable would be accessible to malicious code.
     *
     * @param permission The ReflectPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSuppressAccessChecks(
            final ReflectPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Ability to create a proxy instance in the specified package of
     * which the non-public interface that the proxy class implements.
     * <p>
     * Risk of Allowing this permission This gives code access to classes in packages to which it
     * normally does not have access and the dynamic proxy class is in the system protection domain.
     * Malicious code may use these classes to help in its attempt to compromise security in the
     * system.
     *
     * @param permission The ReflectPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkNewProxyInPackage(
            final ReflectPermission permission) {
        String packageName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

}

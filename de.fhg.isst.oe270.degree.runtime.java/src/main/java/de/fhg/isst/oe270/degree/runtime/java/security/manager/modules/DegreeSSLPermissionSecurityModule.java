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

import javax.net.ssl.SSLPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link SSLPermission} of the D° security manager.
 */
public final class DegreeSSLPermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeSSLPermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * The javax.net.ssl.SSLPermission class is for various network permissions. An SSLPermission
     * contains a name (also referred to as a "target name") but no actions list; you either have
     * the named permission or you don't.
     * <p>
     * The target name is the name of the network permission (see below). The naming convention
     * follows the hierarchical property naming convention. Also, an asterisk may appear at the end
     * of the name, following a ".", or by itself, to signify a wildcard match. For example: "foo.*"
     * or "*" is valid, "*foo" or "a*b" is not valid.
     *
     * @param permission the SSLPermission which will be checked
     * @return a list of required permissions
     * @see SSLPermission
     */
    public static List<RequiredPermission> checkSSLPermission(final SSLPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "setHostnameVerifier":
                return checkSetHostnameVerifier(permission);
            case "getSSLSessionContext":
                return checkSSLPermission(permission);
            case "setDefaultSSLContext":
                return checkSetDefaultSSLContext(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: The ability to set a callback which can decide whether to allow a
     * mismatch between the host being connected to by an HttpsURLConnection and the common name
     * field in server certificate.
     * <p>
     * Risk of Allowing this permission Malicious code can set a verifier that monitors host names
     * visited by HttpsURLConnection requests or that allows server certificates with invalid common
     * names.
     *
     * @param permission The SSLPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetHostnameVerifier(
            final SSLPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to get the SSLSessionContext of an SSLSession.
     * <p>
     * Risk of Allowing this permission Malicious code may monitor sessions which have been
     * established with SSL peers or might invalidate sessions to slow down performance.
     *
     * @param permission The SSLPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetSSLSessionContext(
            final SSLPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to set the default SSL context.
     * <p>
     * Risk of Allowing this permission When applications use default SSLContext, by setting the
     * default SSL context, malicious code may use unproved trust material, key material and random
     * generator, or use dangerous SSL socket factory and SSL server socket factory.
     *
     * @param permission The SSLPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetDefaultSSLContext(
            final SSLPermission permission) {
        return new ArrayList<>();
    }

}

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

import java.net.NetPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link NetPermission} of the D° security manager.
 */
public final class DegreeNetPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeNetPermissionSecurityModule() {
    }

    /**
     * A java.net.NetPermission is for various network permissions. A NetPermission contains a name
     * but no actions list; you either have the named permission or you don't.
     *
     * @param permission the NetPermission which will be checked
     * @return a list of required permissions
     * @see NetPermission
     */
    public static List<RequiredPermission> checkNetPermission(final NetPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "setDefaultAuthenticator":
                return checkSetDefaultAuthenticator(permission);
            case "requestPasswordAuthentication":
                return checkRequestPasswordAuthentication(permission);
            case "specifyStreamHandler":
                return checkSpecifyStreamHandler(permission);
            case "setProxySelector":
                return checkSetProxySelector(permission);
            case "getProxySelector":
                return checkGetProxySelector(permission);
            case "setCookieHandler":
                return checkSetCookieHandler(permission);
            case "getCookieHandler":
                return checkGetCookieHandler(permission);
            case "setResponseCache":
                return checkSetResponseCache(permission);
            case "getResponseCache":
                return checkGetResponseCache(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: The ability to set the way authentication information is
     * retrieved when a proxy or HTTP server asks for authentication
     * <p>
     * Risk of Allowing this permission Malicious code can set an authenticator that monitors and
     * steals user authentication input as it retrieves the input from the user.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetDefaultAuthenticator(
            final NetPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to ask the authenticator registered with the system
     * for a password
     * <p>
     * Risk of Allowing this permission Malicious code may steal this password.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkRequestPasswordAuthentication(
            final NetPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to specify a stream handler when constructing a URL
     * <p>
     * Risk of Allowing this permission Malicious code may create a URL with resources that it would
     * normally not have access to (like file:/foo/fum/), specifying a stream handler that gets the
     * actual bytes from someplace it does have access to. Thus it might be able to trick the system
     * into creating a ProtectionDomain/CodeSource for a class even though that class really didn't
     * come from that location.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSpecifyStreamHandler(
            final NetPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to set the proxy selector used to make decisions on
     * which proxies to use when making network connections.
     * <p>
     * Risk of Allowing this permission Malicious code can set a ProxySelector that directs network
     * traffic to an arbitrary network host.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetProxySelector(final NetPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to get the proxy selector used to make decisions on
     * which proxies to use when making network connections.
     * <p>
     * Risk of Allowing this permission Malicious code can get a ProxySelector to discover proxy
     * hosts and ports on internal networks, which could then become targets for attack.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetProxySelector(final NetPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to set the cookie handler that processes highly
     * security sensitive cookie information for an Http session.
     * <p>
     * Risk of Allowing this permission Malicious code can set a cookie handler to obtain access to
     * highly security sensitive cookie information. Some web servers use cookies to save user
     * private information such as access control information, or to track user browsing habit.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetCookieHandler(final NetPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to get the cookie handler that processes highly
     * security sensitive cookie information for an Http session.
     * <p>
     * Risk of Allowing this permission Malicious code can get a cookie handler to obtain access to
     * highly security sensitive cookie information. Some web servers use cookies to save user
     * private information such as access control information, or to track user browsing habit.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetCookieHandler(final NetPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to set the response cache that provides access to a
     * local response cache.
     * <p>
     * Risk of Allowing this permission Malicious code getting access to the local response cache
     * could access security sensitive information, or create false entries in the response cache.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetResponseCache(final NetPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: The ability to get the response cache that provides access to a
     * local response cache.
     * <p>
     * Risk of Allowing this permission Malicious code getting access to the local response cache
     * could access security sensitive information.
     *
     * @param permission The NetPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetResponseCache(final NetPermission permission) {
        return new ArrayList<>();
    }

}

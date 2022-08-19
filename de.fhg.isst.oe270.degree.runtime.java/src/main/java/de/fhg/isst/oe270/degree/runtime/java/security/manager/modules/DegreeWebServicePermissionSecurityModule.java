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

import javax.xml.ws.WebServicePermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link WebServicePermission} of the D° security manager.
 */
public final class DegreeWebServicePermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeWebServicePermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * This class defines web service permissions. Web service Permissions are identified by name
     * (also referred to as a "target name") alone. There are no actions associated with them.
     *
     * @param permission the WebServicePermission which will be checked
     * @return a list of required permissions
     * @see WebServicePermission
     */
    public static List<RequiredPermission> checkWebServicePermission(
            final WebServicePermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "publishEndpoint":
                return checkPublishEndpoint(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * The publishEndpoint permission allows publishing a web service endpoint using the publish
     * methods defined by the javax.xml.ws.Endpoint class.
     * <p>
     * Granting publishEndpoint allows the application to be exposed as a network service. Depending
     * on the security of the runtime and the security of the application, this may introduce a
     * security hole that is remotely exploitable.
     *
     * @param permission The WebServicePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkPublishEndpoint(
            final WebServicePermission permission) {
        return new ArrayList<>();
    }

}

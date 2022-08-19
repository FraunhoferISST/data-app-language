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

import javax.xml.bind.JAXBPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link JAXBPermission} of the D° security manager.
 */
public final class DegreeJAXBPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeJAXBPermissionSecurityModule() {
    }

    /**
     * This class is for JAXB permissions. A JAXBPermission contains a name (also referred to as a
     * "target name") but no actions list; you either have the named permission or you don't. The
     * target name is the name of the JAXB permission (see below).
     *
     * @param permission the JAXBPermission which will be checked
     * @return a list of required permissions
     * @see JAXBPermission
     */
    public static List<RequiredPermission> checkJAXBPermission(final JAXBPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "setDatatypeConverter":
                return checkSetDatatypeConverter(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: Allows the code to set VM-wide DatatypeConverterInterface via the
     * setDatatypeConverter method that all the methods on DatatypeConverter uses.
     * <p>
     * Risk of Allowing this permission Malicious code can set DatatypeConverterInterface, which has
     * VM-wide singleton semantics, before a genuine JAXB implementation sets one. This allows
     * malicious code to gain access to objects that it may otherwise not have access to, such as
     * Frame.getFrames() that belongs to another application running in the same JVM.
     *
     * @param permission The JAXBPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetDatatypeConverter(
            final JAXBPermission permission) {
        return new ArrayList<>();
    }
}

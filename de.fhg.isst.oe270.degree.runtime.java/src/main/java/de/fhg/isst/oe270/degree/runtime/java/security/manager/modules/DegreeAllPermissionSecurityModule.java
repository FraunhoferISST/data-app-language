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

import de.fhg.isst.oe270.degree.runtime.java.exceptions.security.DegreeForbiddenSecurityFeatureException;
import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AllPermission;
import java.util.List;

/**
 * Module for {@link AllPermission} of the D° security manager.
 */
public final class DegreeAllPermissionSecurityModule {

    /**
     * The used logger.
     */
    protected static final Logger LOGGER =
            LoggerFactory.getLogger("DegreeAllPermissionSecurityModule");
    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeAllPermissionSecurityModule() {
    }

    /**
     * The Permission class for link creation operations.
     *
     * @param permission the AllPermission which will be checked
     * @return a list of required permissions
     * @see AllPermission
     */
    public static List<RequiredPermission> checkAllPermission(final AllPermission permission) {
        LOGGER.error("Tried to request AllPermission, which will never be granted.");
        throw new DegreeForbiddenSecurityFeatureException(
                "Tried to request AllPermission, which will never be granted.");
    }

}

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

import java.io.SerializablePermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link SerializablePermission} of the D° security manager.
 */
public final class DegreeSerializablePermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeSerializablePermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * A java.io.SerializablePermission is for serializable permissions. A SerializablePermission
     * contains a name (also referred to as a "target name") but no actions list; you either have
     * the named permission or you don't.
     * <p>
     * The target name is the name of the Serializable permission (see below).
     *
     * @param permission the SerializablePermission which will be checked
     * @return a list of required permissions
     * @see SerializablePermission
     */
    public static List<RequiredPermission> checkSerializablePermission(
            final SerializablePermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "enableSubclassImplementation":
            case "enableSubstitution":
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: Implementing a subclass of ObjectOutputStream or
     * ObjectInputStream to override the default serialization or deserialization, respectively, of
     * objects
     * <p>
     * Risk of Allowing this permission Code can use this to serialize or deserialize classes in a
     * purposefully malfeasant manner. For example, during serialization, malicious code can use
     * this to purposefully store confidential private field data in a way easily accessible to
     * attackers. Or, during deserializaiton it could, for example, deserialize a class with all its
     * private fields zeroed out.
     *
     * @param permission The SerializablePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkEnableSubclassImplementation(
            final SerializablePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Substitution of one object for another during serialization or
     * deserialization
     * <p>
     * Risk of Allowing this permission This is dangerous because malicious code can replace the
     * actual object with one which has incorrect or malignant data.
     *
     * @param permission The SerializablePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkEnableSubstitution(
            final SerializablePermission permission) {
        return new ArrayList<>();
    }

}

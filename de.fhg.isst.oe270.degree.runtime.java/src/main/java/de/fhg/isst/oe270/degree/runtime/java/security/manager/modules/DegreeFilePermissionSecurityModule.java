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
import de.fhg.isst.oe270.degree.runtime.java.security.functionality.modules.DegreeFileOperations;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.DegreeSecurityManager;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType;

import java.io.FilePermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link FilePermission} of the D° security manager.
 */
public final class DegreeFilePermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeFilePermissionSecurityModule() {
    }

    /**
     * A java.io.FilePermission represents access to a file or directory. A FilePermission consists
     * of a pathname and a set of actions valid for that pathname.
     * <p>
     * Pathname is the pathname of the file or directory granted the specified actions. A pathname
     * that ends in "/*" (where "/" is the file separator character, File.separatorChar) indicates a
     * directory and all the files contained in that directory. A pathname that ends with "/-"
     * indicates (recursively) all files and subdirectories contained in that directory. A pathname
     * consisting of the special token "<<ALL FILES>>" matches any file.
     * <p>
     * A pathname consisting of a single "*" indicates all the files in the current directory, while
     * a pathname consisting of a single "-" indicates all the files in the current directory and
     * (recursively) all files and subdirectories contained in the current directory.
     * <p>
     * The actions to be granted are passed to the constructor in a string containing a list of zero
     * or more comma- separated keywords. The possible keywords are "read", "write", "execute",
     * "delete", and "readLink."
     * <p>
     * The actions string is converted to lowercase before processing.
     * <p>
     * Be careful when granting FilePermissions. Think about the implications of granting read and
     * especially write access to various files and directories. The "<<ALL FILES>>" permission with
     * write action is especially dangerous. This grants permission to write to the entire file
     * system. One thing this effectively allows is replacement of the system binary, including the
     * JVM runtime environment.
     * <p>
     * Note: Code can always read a file from the same directory it is in (or a subdirectory of that
     * directory); it does not need explicit permission to do so. Code can also obtain the pathname
     * of the directory it is executed from, and this pathname may contain sensitive information.
     * For example, if code is executed from a home directory (or a subdirectory of the home
     * directory), the pathname may reveal the name of the current user.
     *
     * @param permission the FilePermission which will be checked
     * @return a list of required permissions
     * @see FilePermission
     */
    public static List<RequiredPermission> checkFilePermission(final FilePermission permission) {
        // ensure that only calls from DegreeFileOperations are accepted
        DegreeSecurityManager.checkCall(DegreeFileOperations.class.getName(), permission);

        String[] targetActions = permission.getActions().split(",");

        List<RequiredPermission> requiredPermissions = new ArrayList<>();

        for (String targetAction : targetActions) {
            switch (targetAction) {
                case "read":
                    requiredPermissions.add(checkRead(permission));
                    break;
                case "write":
                    requiredPermissions.add(checkWrite(permission));
                    break;
                case "execute":
                    requiredPermissions.add(checkExecute(permission));
                    break;
                case "delete":
                    requiredPermissions.add(checkDelete(permission));
                    break;
                case "readLink":
                    requiredPermissions.add(checkReadLink(permission));
                    break;
                default:
                    throw new DegreeUnsupportedSecurityFeatureException();
            }
        }

        return requiredPermissions;
    }

    /**
     * What the permission allows: Permission to read.
     *
     * @param permission The FilePermission that will be checked
     * @return a required permission
     */
    private static RequiredPermission checkRead(final FilePermission permission) {
        return new RequiredPermission(
                DegreePermissionType.READ_FILE,
                permission.getName()
        );
    }

    /**
     * What the permission allows: Permission to write (which includes permission to create).
     *
     * @param permission The FilePermission that will be checked
     * @return a required permission
     */
    private static RequiredPermission checkWrite(final FilePermission permission) {
        return new RequiredPermission(
                DegreePermissionType.WRITE_FILE,
                permission.getName()
        );
    }

    /**
     * What the permission allows: Permission to execute. Allows Runtime.exec to be called.
     * Corresponds to SecurityManager.checkExec.
     *
     * @param permission The FilePermission that will be checked
     * @return a required permission
     */
    private static RequiredPermission checkExecute(final FilePermission permission) {
        return new RequiredPermission(
                DegreePermissionType.EXECUTE_FILE,
                permission.getName()
        );
    }

    /**
     * What the permission allows: Permission to delete. Allows File.delete to be called.
     * Corresponds to SecurityManager.checkDelete.
     *
     * @param permission The FilePermission that will be checked
     * @return a required permission
     */
    private static RequiredPermission checkDelete(final FilePermission permission) {
        return new RequiredPermission(
                DegreePermissionType.DELETE_FILE,
                permission.getName()
        );
    }

    /**
     * What the permission allows: Permission to read links. Allows the target of a symbolic link to
     * be read by invoking the readSymbolicLink method.
     *
     * @param permission The FilePermission that will be checked
     * @return a required permission
     */
    private static RequiredPermission checkReadLink(final FilePermission permission) {
        return null;
    }
}

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

import java.sql.SQLPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link SQLPermission} of the D° security manager.
 */
public final class DegreeSQLPermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeSQLPermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * The permission for which the SecurityManager will check when code that is running in an
     * applet, or an application with an instance of SecurityManager enabled, calls one of the
     * following methods:
     * <p>
     * java.sql.DriverManager.setLogWriter java.sql.DriverManager.setLogStream (deprecated)
     * javax.sql.rowset.spi.SyncFactory.setJNDIContext javax.sql.rowset.spi.SyncFactory.setLogger
     * java.sql.Connection.setNetworktimeout java.sql.Connection.abort If there is no SQLPermission
     * object, these methods throw a java.lang.SecurityException as a runtime exception.
     * <p>
     * A SQLPermission object contains a name (also referred to as a "target name") but no actions
     * list; there is either a named permission or there is not. The target name is the name of the
     * permission (see the following table that lists all the possible SQLPermission names). The
     * naming convention follows the hierarchical property naming convention. In addition, an
     * asterisk (*) may appear at the end of the name, following a dot (.), or by itself, to signify
     * a wildcard match. For example: loadLibrary.* or * is valid, but *loadLibrary or a*b is not
     * valid.
     *
     * @param permission the SQLPermission which will be checked
     * @return a list of required permissions
     * @see SQLPermission
     */
    public static List<RequiredPermission> checkSQLPermission(final SQLPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "setLog":
                return checkSetLog(permission);
            case "callAbort":
                return checkCallAbort(permission);
            case "setSyncFactory":
                return checkSetSyncFactory(permission);
            case "setNetworkTimeout":
                return checkSetNetworkTimeout(permission);
            case "deregisterDriver":
                return checkDeregisterDriver(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: Setting of the logging stream
     * <p>
     * Risk of Allowing this permission This is a dangerous permission to grant. The contents of the
     * log can contain usernames and passwords, SQL statements, and SQL data.
     *
     * @param permission The SQLPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetLog(final SQLPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Invocation of the Connection method abort
     * <p>
     * Risk of Allowing this permission Permits an application to terminate a physical connection to
     * a database.
     *
     * @param permission The SQLPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCallAbort(final SQLPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Invocation of the SyncFactory methods setJNDIContext and
     * setLogger
     * <p>
     * Risk of Allowing this permission Permits an application to specify the JNDI context from
     * which the SyncProvider implementations can be retrieved from and the logging object to be
     * used by the SyncProvider implementation.
     *
     * @param permission The SQLPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetSyncFactory(final SQLPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Invocation of the Connection method setNetworkTimeout
     * <p>
     * Risk of Allowing this permission Permits an application to specify the maximum period a
     * Connection or objects created from the Connection object will wait for the database to reply
     * to any one request.
     *
     * @param permission The SQLPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetNetworkTimeout(final SQLPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Allows the invocation of the DriverManager method
     * deregisterDriver.
     * <p>
     * Risk of Allowing this permission Permits an application to remove a JDBC driver from the list
     * of registered Drivers and release its resources.
     *
     * @param permission The SQLPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkDeregisterDriver(final SQLPermission permission) {
        return new ArrayList<>();
    }

}

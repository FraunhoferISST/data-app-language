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

import javax.management.MBeanPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link MBeanPermission} of the D° security manager.
 */
public final class DegreeMBeanPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeMBeanPermissionSecurityModule() {
    }

    /**
     * Permission controlling access to MBeanServer operations. If a security manager has been set
     * using System.setSecurityManager(java.lang.SecurityManager), most operations on the
     * MBeanServer require that the caller's permissions imply an MBeanPermission appropriate for
     * the operation. This is described in detail in the documentation for the MBeanServer
     * interface.
     * <p>
     * As with other Permission objects, an MBeanPermission can represent either a permission that
     * you have or a permission that you need. When a sensitive operation is being checked for
     * permission, an MBeanPermission is constructed representing the permission you need. The
     * operation is only allowed if the permissions you have imply the permission you need.
     * <p>
     * An MBeanPermission contains four items of information:
     * <p>
     * The action. For a permission you need, this is one of the actions in the list below. For a
     * permission you have, this is a comma-separated list of those actions, or *, representing all
     * actions.
     * <p>
     * The action is returned by getActions().
     * <p>
     * The class name.
     * <p>
     * For a permission you need, this is the class name of an MBean you are accessing, as returned
     * by MBeanServer.getMBeanInfo(name).getClassName(). Certain operations do not reference a class
     * name, in which case the class name is null.
     * <p>
     * For a permission you have, this is either empty or a class name pattern. A class name pattern
     * is a string following the Java conventions for dot-separated class names. It may end with
     * ".*" meaning that the permission grants access to any class that begins with the string
     * preceding ".*". For instance, "javax.management.*" grants access to
     * javax.management.MBeanServerDelegate and javax.management.timer.Timer, among other classes.
     * <p>
     * A class name pattern can also be empty or the single character "*", both of which grant
     * access to any class.
     * <p>
     * The member.
     * <p>
     * For a permission you need, this is the name of the attribute or operation you are accessing.
     * For operations that do not reference an attribute or operation, the member is null.
     * <p>
     * For a permission you have, this is either the name of an attribute or operation you can
     * access, or it is empty or the single character "*", both of which grant access to any
     * member.
     * <p>
     * The object name.
     * <p>
     * For a permission you need, this is the ObjectName of the MBean you are accessing. For
     * operations that do not reference a single MBean, it is null. It is never an object name
     * pattern.
     * <p>
     * For a permission you have, this is the ObjectName of the MBean or MBeans you can access. It
     * may be an object name pattern to grant access to all MBeans whose names match the pattern. It
     * may also be empty, which grants access to all MBeans whatever their name.
     * <p>
     * If you have an MBeanPermission, it allows operations only if all four of the items match.
     * <p>
     * The class name, member, and object name can be written together as a single string, which is
     * the name of this permission. The name of the permission is the string returned by getName().
     * The format of the string is:
     * <p>
     * className#member[objectName] The object name is written using the usual syntax for
     * ObjectName. It may contain any legal characters, including ]. It is terminated by a ]
     * character that is the last character in the string.
     * <p>
     * One or more of the className, member, or objectName may be omitted. If the member is omitted,
     * the # may be too (but does not have to be). If the objectName is omitted, the [] may be too
     * (but does not have to be). It is not legal to omit all three items, that is to have a name
     * that is the empty string.
     * <p>
     * One or more of the className, member, or objectName may be the character "-", which is
     * equivalent to a null value. A null value is implied by any value (including another null
     * value) but does not imply any other value.
     *
     * @param permission the MBeanPermission which will be checked
     * @return a list of required permissions
     * @see MBeanPermission
     */
    public static List<RequiredPermission> checkMBeanPermission(final MBeanPermission permission) {
        String[] targetActions = permission.getActions().split(",");

        List<RequiredPermission> requiredPermissions = new ArrayList<>();

        for (String targetAction : targetActions) {
            switch (targetAction) {
                case "addNotificationListener":
                    requiredPermissions.add(checkAddNotificationListener(permission));
                    break;
                case "getAttribute":
                    requiredPermissions.add(checkGetAttribute(permission));
                    break;
                case "getClassLoader":
                    requiredPermissions.add(checkGetClassLoader(permission));
                    break;
                case "getClassLoaderFor":
                    requiredPermissions.add(checkGetClassLoaderFor(permission));
                    break;
                case "getClassLoaderRepository":
                    requiredPermissions.add(checkGetClassLoaderRepository(permission));
                    break;
                case "getDomains":
                    requiredPermissions.add(checkGetDomains(permission));
                    break;
                case "getMBeanInfo":
                    requiredPermissions.add(checkGetMBeanInfo(permission));
                    break;
                case "getObjectInstance":
                    requiredPermissions.add(checkGetObjectInstance(permission));
                    break;
                case "instantiate":
                    requiredPermissions.add(checkInstantiate(permission));
                    break;
                case "invoke":
                    requiredPermissions.add(checkInvoke(permission));
                    break;
                case "isInstanceOf":
                    requiredPermissions.add(checkIsInstanceOf(permission));
                    break;
                case "queryMBeans":
                    requiredPermissions.add(checkQueryMBeans(permission));
                    break;
                case "queryNames":
                    requiredPermissions.add(checkQueryNames(permission));
                    break;
                case "registerMBean":
                    requiredPermissions.add(checkRegisterMBean(permission));
                    break;
                case "removeNotificationListener":
                    requiredPermissions.add(checkRemoveNotificationListener(permission));
                    break;
                case "setAttribute":
                    requiredPermissions.add(checkSetAttribute(permission));
                    break;
                case "unregisterMBean":
                    requiredPermissions.add(checkUnregisterMBean(permission));
                    break;
                default:
                    throw new DegreeUnsupportedSecurityFeatureException();
            }
        }

        return requiredPermissions;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkAddNotificationListener(
            final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkGetAttribute(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkGetClassLoader(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkGetClassLoaderFor(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkGetClassLoaderRepository(
            final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkGetDomains(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkGetMBeanInfo(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkGetObjectInstance(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkInstantiate(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkInvoke(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkIsInstanceOf(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkQueryMBeans(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkQueryNames(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkRegisterMBean(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkRemoveNotificationListener(
            final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkSetAttribute(final MBeanPermission permission) {
        return null;
    }

    /**
     * @param permission The MBeanPermission that will be checked
     * @return the required permission
     */
    private static RequiredPermission checkUnregisterMBean(final MBeanPermission permission) {
        return null;
    }

}

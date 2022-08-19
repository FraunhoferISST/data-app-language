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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link RuntimePermission} of the D° security manager.
 */
public final class DegreeRuntimePermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeRuntimePermissionSecurityModule() {
    }

    /**
     * The used logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            DegreeRuntimePermissionSecurityModule.class
                    .getSimpleName());

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * A java.lang.RuntimePermission is for runtime permissions. A RuntimePermission contains a name
     * (also referred to as a "target name") but no actions list; you either have the named
     * permission or you don't.
     * <p>
     * The target name is the name of the runtime permission (see below). The naming convention
     * follows the hierarchical property naming convention. Also, an asterisk may appear at the end
     * of the name, following a ".", or by itself, to signify a wildcard match. For example:
     * "loadLibrary.*" or "*" is valid, "*loadLibrary" or "a*b" is not valid.
     *
     * @param permission the RuntimePermission which will be checked
     * @return a list of required permissions
     * @see RuntimePermission
     */
    public static List<RequiredPermission> checkRuntimePermission(
            final RuntimePermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "createClassLoader":
                return checkCreateClassLoader(permission);
            case "getClassLoader":
                return checkGetClassLoader(permission);
            case "setContextClassLoader":
                return checkSetContextClassLoader(permission);
            case "enableContextClassLoaderOverride":
                return checkEnableContextClassLoaderOverride(permission);
            case "closeClassLoader":
                return checkCloseClassLoader(permission);
            case "setSecurityManager":
                return checkSetSecurityManager(permission);
            case "createSecurityManager":
                return checkCreateSecurityManager(permission);
            case "shutdownHooks":
                return checkShutdownHooks(permission);
            case "setFactory":
                return checkSetFactory(permission);
            case "setIO":
                return checkSetIO(permission);
            case "modifyThread":
                return checkModifyThread(permission);
            case "stopThread":
                return checkStopThread(permission);
            case "modifyThreadGroup":
                return checkModifyThreadGroup(permission);
            case "getProtectionDomain":
                return checkGetProtectionDomain(permission);
            case "getFileSystemAttributes":
                return checkGetFileSystemAttributes(permission);
            case "readFileDescriptor":
                return checkReadFileDescriptor(permission);
            case "writeFileDescriptor":
                return checkWriteFileDescriptor(permission);
            case "accessDeclaredMembers":
                return checkAccessDeclaredMembers(permission);
            case "queuePrintJob":
                return checkQueuePrintJob(permission);
            case "getStackTrace":
                return checkGetStackTrace(permission);
            case "setDefaultUncaughtExceptionHandler":
                return checkSetDefaultUncaughtExceptionHandler(permission);
            case "preferences":
                return checkPreferences(permission);
            case "usePolicy":
                return checkUsePolicy(permission);
            case "selectorProvider":
                return checkSelectorProvider(permission);
            case "charsetProvider":
                return checkCharsetProvider(permission);
            default:
                if (targetName.startsWith("getenv")) {
                    return checkGetenv(permission);
                } else if (targetName.startsWith("exitVM")) {
                    return checkExitVM(permission);
                } else if (targetName.startsWith("loadLibrary")) {
                    return checkLoadLibrary(permission);
                } else if (targetName.startsWith("accessClassInPackage")) {
                    return checkAccessClassInPackage(permission);
                } else if (targetName.startsWith("defineClassInPackage")) {
                    return checkDefineClassInPackage(permission);
                } else if (targetName.startsWith("nashorn")) {
                    return checkNashorn(permission);
                } else {
                    LOGGER.error("Found an unknown RuntimePermission of type '"
                            + targetName + "'.");
                    throw new DegreeUnsupportedSecurityFeatureException();
                }
        }
    }

    /**
     * What the permission allows: Creation of a class loader
     * <p>
     * Risk of Allowing this permission This is an extremely dangerous permission to grant.
     * Malicious applications that can instantiate their own class loaders could then load their own
     * rogue classes into the system. These newly loaded classes could be placed into any protection
     * domain by the class loader, thereby automatically granting the classes the permissions for
     * that domain.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCreateClassLoader(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieval of a class loader (e.g., the class loader for the
     * calling class)
     * <p>
     * Risk of Allowing this permission This would grant an attacker permission to get the class
     * loader for a particular class. This is dangerous because having access to a class's class
     * loader allows the attacker to load other classes available to that class loader. The attacker
     * would typically otherwise not have access to those classes.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetClassLoader(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of the context class loader used by a thread
     * <p>
     * Risk of Allowing this permission The context class loader is used by system code and
     * extensions when they need to lookup resources that might not exist in the system class
     * loader. Granting setContextClassLoader permission would allow code to change which context
     * class loader is used for a particular thread, including system threads.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetContextClassLoader(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Subclass implementation of the thread context class loader
     * methods
     * <p>
     * Risk of Allowing this permission The context class loader is used by system code and
     * extensions when they need to lookup resources that might not exist in the system class
     * loader. Granting enableContextClassLoaderOverride permission would allow a subclass of Thread
     * to override the methods that are used to get or set the context class loader for a particular
     * thread.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkEnableContextClassLoaderOverride(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Closing of a ClassLoader
     * <p>
     * Risk of Allowing this permission Granting this permission allows code to close any
     * URLClassLoader that it has a reference to.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCloseClassLoader(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of the security manager (possibly replacing an existing
     * one)
     * <p>
     * Risk of Allowing this permission The security manager is a class that allows applications to
     * implement a security policy. Granting the setSecurityManager permission would allow code to
     * change which security manager is used by installing a different, possibly less restrictive
     * security manager, thereby bypassing checks that would have been enforced by the original
     * security manager.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetSecurityManager(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Creation of a new security manager
     * <p>
     * Risk of Allowing this permission This gives code access to protected, sensitive methods that
     * may disclose information about other classes or the execution stack.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCreateSecurityManager(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Reading of the value of the specified environment variable
     * <p>
     * Risk of Allowing this permission This would allow code to read the value, or determine the
     * existence, of a particular environment variable. This is dangerous if the variable contains
     * confidential data.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetenv(final RuntimePermission permission) {
        String variableName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Halting of the Java Virtual Machine with the specified exit
     * status
     * <p>
     * Risk of Allowing this permission This allows an attacker to mount a denial-of-service attack
     * by automatically forcing the virtual machine to halt. Note: The "exitVM.*" permission is
     * automatically granted to all code loaded from the application class path, thus enabling
     * applications to terminate themselves. Also, the "exitVM" permission is equivalent to
     * "exitVM.*".
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkExitVM(final RuntimePermission permission) {
        int exitStatus = Integer.valueOf(permission.getName().split("\\.")[1]);
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Registration and cancellation of virtual-machine shutdown hooks
     * <p>
     * Risk of Allowing this permission This allows an attacker to register a malicious shutdown
     * hook that interferes with the clean shutdown of the virtual machine.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkShutdownHooks(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of the socket factory used by ServerSocket or Socket, or
     * of the stream handler factory used by URL
     * <p>
     * Risk of Allowing this permission This allows code to set the actual implementation for the
     * socket, server socket, stream handler, or RMI socket factory. An attacker may set a faulty
     * implementation which mangles the data stream.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetFactory(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of System.out, System.in, and System.err
     * <p>
     * Risk of Allowing this permission This allows changing the value of the standard system
     * streams. An attacker may change System.in to monitor and steal user input, or may set
     * System.err to a "null" OutputSteam, which would hide any error messages sent to System.err.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetIO(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Modification of threads, e.g., via calls to Thread interrupt,
     * stop, suspend, resume, setDaemon, setPriority, setName and setUncaughtExceptionHandler
     * methods
     * <p>
     * Risk of Allowing this permission This allows an attacker to modify the behavior of any thread
     * in the system.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkModifyThread(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Stopping of threads via calls to the Thread stop method
     * <p>
     * Risk of Allowing this permission This allows code to stop any thread in the system provided
     * that it is already granted permission to access that thread. This poses as a threat, because
     * that code may corrupt the system by killing existing threads.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkStopThread(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Modification of thread groups, e.g., via calls to ThreadGroup
     * destroy, getParent, resume, setDaemon, setMaxPriority, stop, and suspend methods
     * <p>
     * Risk of Allowing this permission This allows an attacker to create thread groups and set
     * their run priority.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkModifyThreadGroup(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieval of the ProtectionDomain for a class
     * <p>
     * Risk of Allowing this permission This allows code to obtain policy information for a
     * particular code source. While obtaining policy information does not compromise the security
     * of the system, it does give attackers additional information, such as local file names for
     * example, to better aim an attack.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetProtectionDomain(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieval of file system attributes
     * <p>
     * Risk of Allowing this permission This allows code to obtain file system information such as
     * disk usage or disk space available to the caller. This is potentially dangerous because it
     * discloses information about the system hardware configuration and some information about the
     * caller's privilege to write files.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetFileSystemAttributes(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Reading of file descriptors
     * <p>
     * Risk of Allowing this permission This would allow code to read the particular file associated
     * with the file descriptor read. This is dangerous if the file contains confidential data.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkReadFileDescriptor(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Writing to file descriptors
     * <p>
     * Risk of Allowing this permission This allows code to write to a particular file associated
     * with the descriptor. This is dangerous because it may allow malicous code to plant viruses or
     * at the very least, fill up your entire disk.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkWriteFileDescriptor(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Dynamic linking of the specified library
     * <p>
     * Risk of Allowing this permission It is dangerous to allow an applet permission to load native
     * code libraries, because the Java security architecture is not designed to and does not
     * prevent malicious behavior at the level of native code.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkLoadLibrary(final RuntimePermission permission) {
        String libraryName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Access to the specified package via a class loader's loadClass
     * method when that class loader calls the SecurityManager checkPackageAcesss method
     * <p>
     * Risk of Allowing this permission This gives code access to classes in packages to which it
     * normally does not have access. Malicious code may use these classes to help in its attempt to
     * compromise security in the system.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkAccessClassInPackage(
            final RuntimePermission permission) {
        String packageName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Definition of classes in the specified package, via a class
     * loader's defineClass method when that class loader calls the SecurityManager
     * checkPackageDefinition method.
     * <p>
     * Risk of Allowing this permission This grants code permission to define a class in a
     * particular package. This is dangerous because malicious code with this permission may define
     * rogue classes in trusted packages like java.security or java.lang, for example.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkDefineClassInPackage(
            final RuntimePermission permission) {
        String packageName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Warning: Extreme caution should be taken before granting this
     * permission to code, for it provides access to the declared members of a class.
     * <p>
     * Risk of Allowing this permission This grants code permission to query a class for its public,
     * protected, default (package) access, and private fields and/or methods. Although the code
     * would have access to the private and protected field and method names, it would not have
     * access to the private/protected field data and would not be able to invoke any private
     * methods. Nevertheless, malicious code may use this information to better aim an attack.
     * Additionally, it may invoke any public methods and/or access public fields in the class. This
     * could be dangerous if the code would normally not be able to invoke those methods and/or
     * access the fields because it can't cast the object to the class/interface with those methods
     * and fields.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkAccessDeclaredMembers(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Initiation of a print job request
     * <p>
     * Risk of Allowing this permission This could print sensitive information to a printer, or
     * simply waste paper.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkQueuePrintJob(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieval of the stack trace information of another thread.
     * <p>
     * Risk of Allowing this permission This allows retrieval of the stack trace information of
     * another thread. This might allow malicious code to monitor the execution of threads and
     * discover vulnerabilities in applications.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetStackTrace(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting the default handler to be used when a thread terminates
     * abruptly due to an uncaught exception.
     * <p>
     * Risk of Allowing this permission This allows an attacker to register a malicious uncaught
     * exception handler that could interfere with termination of a thread.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetDefaultUncaughtExceptionHandler(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Represents the permission required to get access to the
     * java.util.prefs.Preferences implementations user or system root which in turn allows
     * retrieval or update operations within the Preferences persistent backing store.
     * <p>
     * Risk of Allowing this permission This permission allows the user to read from or write to the
     * preferences backing store if the user running the code has sufficient OS privileges to
     * read/write to that backing store. The actual backing store may reside within a traditional
     * filesystem directory or within a registry depending on the platform OS.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkPreferences(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Granting this permission disables the Java Plug-In's default
     * security prompting behavior.
     * <p>
     * Risk of Allowing this permission For more information, refer to Java Plug-In's guides, Applet
     * Security Basics and usePolicy Permission.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkUsePolicy(final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * These RuntimePermissions are required to be granted to classes which subclass and implement
     * java.nio.channel.spi.SelectorProvider or java.nio.charset.spi.CharsetProvider. The permission
     * is checked during invocation of the abstract base class constructor. These permissions ensure
     * trust in classes which implement these security-sensitive provider mechanisms.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSelectorProvider(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * These RuntimePermissions are required to be granted to classes which subclass and implement
     * java.nio.channel.spi.SelectorProvider or java.nio.charset.spi.CharsetProvider. The permission
     * is checked during invocation of the abstract base class constructor. These permissions ensure
     * trust in classes which implement these security-sensitive provider mechanisms.
     *
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCharsetProvider(
            final RuntimePermission permission) {
        return new ArrayList<>();
    }

    /**
     * @param permission The RuntimePermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkNashorn(final RuntimePermission permission) {
        return new ArrayList<>();
    }
}

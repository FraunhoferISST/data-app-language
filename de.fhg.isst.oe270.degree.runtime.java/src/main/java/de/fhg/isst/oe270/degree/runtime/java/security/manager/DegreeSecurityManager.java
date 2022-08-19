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
package de.fhg.isst.oe270.degree.runtime.java.security.manager;

import de.fhg.isst.degree.types.gen.degree.ConstraintInstance;
import de.fhg.isst.degree.types.gen.degree.PolicyInstance;
import de.fhg.isst.oe270.degree.activities.api.ActivityApi;
import de.fhg.isst.oe270.degree.runtime.java.data.app.CliDataApp;
import de.fhg.isst.oe270.degree.runtime.java.exceptions.security.DegreeForbiddenSecurityFeatureException;
import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox;
import de.fhg.isst.oe270.degree.runtime.java.security.evaluation.PermissionScope;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeAWTPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeAllPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeAudioPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeAuthPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeDelegationPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeFilePermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeJAXBPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeLinkPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeLoggingPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeMBeanPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeMBeanServerPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeMBeanTrustPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeManagementPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeNetPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreePrivateCredentialPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreePropertyPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeReflectPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeRuntimePermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeSQLPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeSSLPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeSecurityPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeSerializablePermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeServicePermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeSocketPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeSubjectDelegationPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeURLPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeUnresolvedPermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.modules.DegreeWebServicePermissionSecurityModule;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.IgnoredPermission;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType;
import nukleus.core.CompositeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanPermission;
import javax.management.MBeanServerPermission;
import javax.management.MBeanTrustPermission;
import javax.management.remote.SubjectDelegationPermission;
import javax.net.ssl.SSLPermission;
import javax.security.auth.AuthPermission;
import javax.security.auth.PrivateCredentialPermission;
import javax.security.auth.kerberos.DelegationPermission;
import javax.security.auth.kerberos.ServicePermission;
import javax.sound.sampled.AudioPermission;
import javax.xml.bind.JAXBPermission;
import javax.xml.ws.WebServicePermission;
import java.awt.AWTPermission;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.io.SerializablePermission;
import java.lang.management.ManagementPermission;
import java.lang.reflect.Member;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.net.URLPermission;
import java.nio.file.LinkPermission;
import java.nio.file.Paths;
import java.security.AccessControlContext;
import java.security.AllPermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.security.UnresolvedPermission;
import java.sql.SQLPermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

/**
 * Extension of the java default security manager.
 * makes the functionality of the java security manager available for D° applications.
 */
public final class DegreeSecurityManager extends SecurityManager {

    /**
     * The used logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger("DegreeSecurityManager");

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * The classes stored in the list are allowed to use functions of normally blocked APIs.
     */
    private static final HashMap<Class<? extends Permission>, List<String>> ALLOWED_BYPASSES
            = new HashMap();

    /**
     * The executed application.
     */
    private final CliDataApp dataApp;

    /**
     * Construct the security manager for a specific application.
     *
     * @param defaultDataApp the application
     */
    public DegreeSecurityManager(final CliDataApp defaultDataApp) {
        dataApp = defaultDataApp;
        IgnoredPermission.init();
    }

    /**
     * Define some special classes (like class loaders) which are allowed to bypass some kinds
     * of permissions (e.g. file permissions).
     */
    private static void populateAllowedBypasses() {
        ArrayList<String> filePermissionBypasses = new ArrayList<>();
        filePermissionBypasses.add("org.springframework.web.client.RestTemplate");
        filePermissionBypasses.add("java.lang.ClassLoader");
        filePermissionBypasses.add("java.util.jar.JarFile");
        filePermissionBypasses.add("java.lang.Class");
        filePermissionBypasses.add("org.apache.http.impl.client.HttpClients");

        ALLOWED_BYPASSES.put(FilePermission.class, filePermissionBypasses);
    }

    /**
     * Check if a performed call was performed by a given allowed class.
     *
     * @param allowedClass the class that is allowed to perform the call
     * @param permission   the required permission
     */
    public static void checkCall(final String allowedClass, final Permission permission) {
        if (ALLOWED_BYPASSES.isEmpty()) {
            populateAllowedBypasses();
        }
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // skip the first element since it will always be java.lang.Thread.getStackTrace
        // find the class which is responsible for this call
        for (int i = 1; i < stack.length; i++) {
            if (stack[i].getClassName().equals(allowedClass)
                    || ALLOWED_BYPASSES.get(permission.getClass())
                    .contains(stack[i].getClassName())) {
                return;
            }
        }
        String stackMsg = "";
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            stackMsg += e.toString() + "\n";
        }
        // In case the stack trace analysis did not terminate this function a not allowed call
        // is found
        throw new DegreeForbiddenSecurityFeatureException(
                "Error during validation of " + permission.getClass().getSimpleName()
                        + " with action(s) '" + permission.getActions() + "'. "
                        + "A method tried to use a feature which is only allowed for the class "
                        + allowedClass + ". " + "StackTrace:\n" + stackMsg
        );
    }

    /**
     * Check if a permission must be evaluated (--> not in application start up) and if so
     * start the evalutaion.
     *
     * @param permission the checked permission
     * @param context    the java access control context
     */
    public void checkDegreePermission(
            final Permission permission, final AccessControlContext context) {
        AccessControlContext ctx = context;
        // ensure context is available
        if (ctx == null) {
            ctx = (AccessControlContext) getSecurityContext();
        }

        // There is no need to restrict access to classes since the other permissions are
        // responsible for enforcement
        if (permission instanceof RuntimePermission && permission.getName()
                .contains("accessClassInPackage")) {
            return;
        }

        // check if the current permission is requestes by data app startup code
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // optimization because process method should be one of the earliest (--> last) entries
        //ArrayUtils.reverse(stack);
        /* FIXME if the above line is in use the compiled jar still works but the code can not
             be run in IntelliJ because of StackOverflow which is a result of loading ArrayUtil
              class vom lang3... */
        boolean startupCode = true;

        // required for stack trace analysis
        boolean dataAppFound = false;
        boolean activityFound = false;

        // analyze stack
        for (StackTraceElement element : stack) {
            try {
                // The process method of the data app is the entry point for all code that needs
                // to be evaluated. Since the exact class name is not known prior runtime it will
                // be checked if the class can be assigned to a data app --> extends it
                if (element.getMethodName().equals("process")
                        && Class.forName(element.getClassName())
                                .isAssignableFrom(dataApp.getClass())) {
                    dataAppFound = true;
                } else if (element.getMethodName().equals("run")
                        && ActivityApi.class.isAssignableFrom(Class
                                .forName(element.getClassName()))) {
                    activityFound = true;
                }
                // if we are inside an activity of a data app, we are not in startupCode
                // which will be ignored by the security manager
                if (dataAppFound && activityFound) {
                    startupCode = false;
                    break;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (startupCode) {
            return; //TODO maybe logging
        } else {
            // at this point the scope of the java security managers functionality is left
            // and the D° security manager delegates to the D° systems
            evaluateDegreePermission(permission, ctx);
        }
    }

    /**
     * Evaluate a permission in the context of D°.
     *
     * @param permission the permission to evaluate
     * @param context    java access control context
     */
    private void evaluateDegreePermission(
            final Permission permission, final AccessControlContext context) {
        List<RequiredPermission> newPermissions = new ArrayList();
        // delegate the permission to the correct submodule which will create a matching
        // RequiredPermission
        List<RequiredPermission> requiredPermissions = obtainRequiredPermissions(permission);
        // determine if some of the found permissions are on the ignorelist
        List<RequiredPermission> filteredPermissions =
                filterRequiredPermissions(requiredPermissions);
        // this flag indicates if a change happend and we need to evaluate the permissions
        boolean newRequiredPermission = false;
        // add the filtered list to the current permission scope
        // but only entries which are not yet part of the required permissions
        for (RequiredPermission requiredPermission : filteredPermissions) {
            if (!PermissionScope.getInstance().containsRequiredPermission(requiredPermission)) {
                PermissionScope.getInstance().addRequiredPermission(requiredPermission);
                newRequiredPermission = true;
                newPermissions.add(requiredPermission);
            }
        }
        // perform the actual resolving of required permissions, evaluation conditions,
        // and additional data. In case there is a resolving error an exception is thrown.
        if (newRequiredPermission) {
            // provide the new permissions to the sandbox so it is possible for
            // constraints to access them
            Sandbox.getInstance().setCurrentRequiredPermissions(newPermissions);

            // since a new required permission is going to be evaluated we need to obtain the set
            // of evaluation conditions from all currently relevant policies
            PermissionScope.getInstance().addEvaluationConditions(obtainEvaluationConditions());
            // resolving
            Sandbox.getInstance().resolveCurrentPermissionScope();
        }
    }

    /**
     * Get all evaluation conditions from current policies, stored in {@link Sandbox}.
     *
     * @return the current evaluation conditions
     */
    private Collection<EvaluationCondition> obtainEvaluationConditions() {
        Collection<EvaluationCondition> evaluationConditions = new ArrayList<>();
        for (CompositeInstance policy : Sandbox.getInstance().getCurrentPolicies()) {
            if (policy instanceof PolicyInstance) {
                continue;
            } else {
                LOGGER.info("Obtaining evaluation conditions from constraint '"
                        + ((ConstraintInstance) policy).getName().read() + "'.");
                evaluationConditions.addAll(
                        Sandbox.getInstance()
                                .evaluateSecurityManagerIntervention((ConstraintInstance) policy)
                );
            }
        }

        return evaluationConditions;
    }

    /**
     * Filter required permissions (e.g. remove ignored ones).
     *
     * @param requiredPermissions the required permissions
     * @return filtered required permissions
     */
    private List<RequiredPermission> filterRequiredPermissions(
            final List<RequiredPermission> requiredPermissions) {
        // remove duplicates
        ArrayList<RequiredPermission> requiredPermissionsNoDuplicates =
                new ArrayList<>(new HashSet<>(requiredPermissions));
        // get current filter list
        HashMap<DegreePermissionType, HashMap<String, ArrayList<String>>> ignoredPermissions =
                IgnoredPermission.getIGNORED_PERMISSION_MAP();
        List<RequiredPermission> filteredPermissions = new ArrayList<>();
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // build structure for searches
        HashMap<String, ArrayList<String>> stackData = new HashMap<>();
        for (StackTraceElement elem : stack) {
            if (!stackData.containsKey(elem.getClassName())) {
                stackData.put(elem.getClassName(), new ArrayList<>());
            }
            stackData.get(elem.getClassName()).add(elem.getMethodName());
        }

        // check each required permission
        for (RequiredPermission requiredPermission : requiredPermissionsNoDuplicates) {
            if (requiredPermission == null) {
                continue;
            }
            boolean filtered = false;

            if (ignoredPermissions.containsKey(requiredPermission.getCategory())) {
                HashMap<String, ArrayList<String>> ignoredElements =
                        ignoredPermissions.get(requiredPermission.getCategory());
                for (String ignoredClass : ignoredElements.keySet()) {
                    if (stackData.containsKey(ignoredClass)) {
                        for (String ignoredMethod : ignoredElements.get(ignoredClass)) {
                            if (stackData.get(ignoredClass).contains(ignoredMethod)) {
                                filtered = true;
                                break;
                            }
                        }
                    }
                    if (filtered) {
                        break;
                    }
                }
            }

            // check if the permission was filtered
            if (!filtered) {
                filteredPermissions.add(requiredPermission);
            }
        }

        return filteredPermissions;
    }

    /**
     * Map a java permission to D° permissions.
     *
     * @param permission the java permission
     * @return the corresponding D° permissions
     */
    private List<RequiredPermission> obtainRequiredPermissions(final Permission permission) {
        if (permission instanceof AWTPermission) {
            return DegreeAWTPermissionSecurityModule
                    .checkAWTPermission((AWTPermission) permission);
        } else if (permission instanceof FilePermission) {
            return DegreeFilePermissionSecurityModule
                    .checkFilePermission((FilePermission) permission);
        } else if (permission instanceof SerializablePermission) {
            return DegreeSerializablePermissionSecurityModule
                    .checkSerializablePermission((SerializablePermission) permission);
        } else if (permission instanceof ManagementPermission) {
            return DegreeManagementPermissionSecurityModule
                    .checkManagementPermission((ManagementPermission) permission);
        } else if (permission instanceof ReflectPermission) {
            return DegreeReflectPermissionSecurityModule
                    .checkReflectPermission((ReflectPermission) permission);
        } else if (permission instanceof RuntimePermission) {
            return DegreeRuntimePermissionSecurityModule
                    .checkRuntimePermission((RuntimePermission) permission);
        } else if (permission instanceof NetPermission) {
            return DegreeNetPermissionSecurityModule
                    .checkNetPermission((NetPermission) permission);
        } else if (permission instanceof SocketPermission) {
            return DegreeSocketPermissionSecurityModule
                    .checkSocketPermission((SocketPermission) permission);
        } else if (permission instanceof URLPermission) {
            return DegreeURLPermissionSecurityModule
                    .checkURLPermission((URLPermission) permission);
        } else if (permission instanceof LinkPermission) {
            return DegreeLinkPermissionSecurityModule
                    .checkLinkPermission((LinkPermission) permission);
        } else if (permission instanceof AllPermission) {
            return DegreeAllPermissionSecurityModule
                    .checkAllPermission((AllPermission) permission);
        } else if (permission instanceof SecurityPermission) {
            return DegreeSecurityPermissionSecurityModule
                    .checkSecurityPermission((SecurityPermission) permission);
        } else if (permission instanceof UnresolvedPermission) {
            return DegreeUnresolvedPermissionSecurityModule
                    .checkUnresolvedPermission((UnresolvedPermission) permission);
        } else if (permission instanceof SQLPermission) {
            return DegreeSQLPermissionSecurityModule
                    .checkSQLPermission((SQLPermission) permission);
        } else if (permission instanceof LoggingPermission) {
            return DegreeLoggingPermissionSecurityModule
                    .checkLoggingPermission((LoggingPermission) permission);
        } else if (permission instanceof PropertyPermission) {
            return DegreePropertyPermissionSecurityModule
                    .checkPropertyPermission((PropertyPermission) permission);
        } else if (permission instanceof MBeanPermission) {
            return DegreeMBeanPermissionSecurityModule
                    .checkMBeanPermission((MBeanPermission) permission);
        } else if (permission instanceof MBeanServerPermission) {
            return DegreeMBeanServerPermissionSecurityModule
                    .checkMBeanServerPermission((MBeanServerPermission) permission);
        } else if (permission instanceof MBeanTrustPermission) {
            return DegreeMBeanTrustPermissionSecurityModule
                    .checkMBeanTrustPermission((MBeanTrustPermission) permission);
        } else if (permission instanceof SubjectDelegationPermission) {
            return DegreeSubjectDelegationPermissionSecurityModule
                    .checkSubjectDelegationPermission((SubjectDelegationPermission) permission);
        } else if (permission instanceof SSLPermission) {
            return DegreeSSLPermissionSecurityModule
                    .checkSSLPermission((SSLPermission) permission);
        } else if (permission instanceof AuthPermission) {
            return DegreeAuthPermissionSecurityModule
                    .checkAuthPermission((AuthPermission) permission);
        } else if (permission instanceof DelegationPermission) {
            return DegreeDelegationPermissionSecurityModule
                    .checkDelegationPermission((DelegationPermission) permission);
        } else if (permission instanceof ServicePermission) {
            return DegreeServicePermissionSecurityModule
                    .checkServicePermission((ServicePermission) permission);
        } else if (permission instanceof PrivateCredentialPermission) {
            return DegreePrivateCredentialPermissionSecurityModule
                    .checkPrivateCredentialPermission((PrivateCredentialPermission) permission);
        } else if (permission instanceof AudioPermission) {
            return DegreeAudioPermissionSecurityModule
                    .checkAudioPermission((AudioPermission) permission);
        } else if (permission instanceof JAXBPermission) {
            return DegreeJAXBPermissionSecurityModule
                    .checkJAXBPermission((JAXBPermission) permission);
        } else if (permission instanceof WebServicePermission) {
            return DegreeWebServicePermissionSecurityModule
                    .checkWebServicePermission((WebServicePermission) permission);
        }
        LOGGER.warn("Received unknown type of permission: '"
                + permission.getClass().getSimpleName() + "'");
        return new ArrayList<>();
    }

    /**
     * Create and evaluate a socket permission with given host and port.
     *
     * @param host the host
     * @param port the port
     */
    @Override
    public void checkAccept(final String host, final int port) {
        checkPermission(new SocketPermission(host + ":" + port, "accept"));
    }

    /**
     * Create and evaluate a runtime permission for modifying a thread.
     *
     * @param thread the parameter is ignored but required by the API of the java security
     *               manager
     */
    @Override
    public void checkAccess(final Thread thread) {
        checkPermission(new RuntimePermission("modifyThread"));
    }

    /**
     * Create and evaluate a runtime permission for modifying a thread group.
     *
     * @param threadGroup the parameter is ignored but required by the API of the java security
     *                    manager
     */
    @Override
    public void checkAccess(final ThreadGroup threadGroup) {
        checkPermission(new RuntimePermission("modifyThreadGroup"));
    }

    /**
     * Create and evaluate a AWT permission for accesing the event queue.
     */
    @Override
    public void checkAwtEventQueueAccess() {
        checkPermission(new AWTPermission("accessEventQueue"));
    }

    /**
     * Create and resolve a socket permission, either for connecting to a given host:port
     * or resolving a given host.
     *
     * @param host the used host
     * @param port the used port or -1 if resolving
     */
    @Override
    public void checkConnect(final String host, final int port) {
        if (port == -1) {
            checkPermission(new SocketPermission(host, "resolve"));
        } else {
            checkPermission(new SocketPermission(host + ":" + port, "connect"));
        }
    }

    /**
     * Create and resolve a socket permission, either for connecting to a given host:port
     * or resolving a given host.
     *
     * @param host    the used host
     * @param port    the used port or -1 if resolving
     * @param context
     */
    @Override
    public void checkConnect(final String host, final int port, final Object context) {
        checkConnect(host, port);
    }

    /**
     * Create and evaluate a permission for creating a class loader.
     */
    @Override
    public void checkCreateClassLoader() {
        checkPermission(new RuntimePermission("createClassLoader"));
    }

    /**
     * Create and evaluate a file permission for deleting a file.
     *
     * @param filename the file to delete
     */
    @Override
    public void checkDelete(final String filename) {
        checkPermission(new FilePermission(filename, "delete"));
    }

    /**
     * Check and evaluate a file permission for executing a command.
     *
     * @param cmd the command to execute
     */
    @Override
    public void checkExec(final String cmd) {
        String path = cmd;
        if (!Paths.get(cmd).isAbsolute()) {
            path = "<<ALL_FILES>>";
        }
        checkPermission(new FilePermission(path, "execute"));
    }

    /**
     * @param status
     */
    @Override
    public void checkExit(final int status) {
        checkPermission(new RuntimePermission("exitVM." + status));
    }

    @Override
    public void checkLink(final String library) {
        checkPermission(new RuntimePermission("loadLibrary." + library));
    }

    @Override
    public void checkListen(final int port) {
        checkPermission(new SocketPermission("localhost:" + port, "listen"));
    }

    @Override
    public void checkMemberAccess(final Class clazz, final int which) {
        if (which != Member.PUBLIC) {
            if (currentClassLoader() != clazz.getClassLoader()) {
                checkPermission(
                        new java.lang.RuntimePermission("accessDeclaredMembers"));
            }
        }
    }

    @Override
    public void checkMulticast(final InetAddress maddr) {
        checkPermission(new SocketPermission(maddr.getHostAddress(), "accept,connect"));
    }

    @Override
    public void checkMulticast(final InetAddress maddr, final byte ttl) {
        checkPermission(new SocketPermission(maddr.getHostAddress(), "accept,connect"));
    }

    @Override
    public void checkPackageAccess(final String pkg) {
        checkPermission(new RuntimePermission("accessClassInPackage." + pkg));
    }

    @Override
    public void checkPackageDefinition(final String pkg) {
        checkPermission(new RuntimePermission("defineClassInPackage." + pkg));
    }

    @Override
    public void checkPrintJobAccess() {
        checkPermission(new RuntimePermission("queuePrintJob"));
    }

    @Override
    public void checkPropertiesAccess() {
        checkPermission(new PropertyPermission("*", "read,write"));
    }

    @Override
    public void checkPropertyAccess(final String key) {
        checkPermission(new PropertyPermission("key", "read,write"));
    }

    @Override
    public void checkRead(final FileDescriptor fileDescriptor) {
        checkPermission(new RuntimePermission("readFileDescriptor"));
    }

    @Override
    public void checkRead(final String filename, final Object o) {
        checkRead(filename);
    }

    @Override
    public void checkRead(final String filename) {
        checkPermission(new FilePermission(filename, "read"));
    }

    @Override
    public void checkSecurityAccess(final String target) {
        checkPermission(new SecurityPermission(target));
    }

    @Override
    public void checkSetFactory() {
        checkPermission(new RuntimePermission("setFactory"));
    }

    @Override
    public void checkSystemClipboardAccess() {
        checkPermission(new AWTPermission("accessClipboard"));
    }

    @Override
    public boolean checkTopLevelWindow(final Object window) {
        checkPermission(new AWTPermission("showWindowWithoutWarningBanner"));
        return true;
    }

    @Override
    public void checkWrite(final FileDescriptor fileDescriptor) {
        checkPermission(new RuntimePermission("writeFileDescriptor"));
    }

    @Override
    public void checkWrite(final String filename) {
        checkPermission(new FilePermission(filename, "write"));
    }

    /**
     * @param perm the required permission
     */
    @Override
    public void checkPermission(final Permission perm) {
        checkDegreePermission(perm, null);
    }

    /**
     * @param perm    the required permission
     * @param context access control context with more details.
     */
    @Override
    public void checkPermission(final Permission perm, final Object context) {
        checkDegreePermission(perm, (AccessControlContext) context);
    }

}

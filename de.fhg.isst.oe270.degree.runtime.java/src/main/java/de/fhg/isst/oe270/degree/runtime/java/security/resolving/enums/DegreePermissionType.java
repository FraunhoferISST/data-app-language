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
package de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums;

/**
 * This enum contains all permissions which are extracted from the java security manager.
 * <p>
 * Maybe not all of these attributes are covered by D°, but each entry which is handled by D° will
 * be documented.
 * <p>
 * These permission types are used to identify the permissions which are required/granted for the
 * execution of activities.
 */
public enum DegreePermissionType {
    /**
     * D° specific wildcard permission. Required to switch from blacklisting to whitelisting. For
     * evaluation conditions with wildcard type the D° matching strategy is ignored.
     */
    WILDCARD,                               // Handling complete

    /* From AWTPermission */

    /**
     * Access the clipboard.
     */
    ACCESS_CLIPBOARD,
    /**
     * Access the event queue.
     */
    ACCESS_EVENT_QUEUE,
    /**
     * Access the system tray.
     */
    ACCESS_SYSTEM_TRAY,
    /**
     * Create robot.
     */
    CREATE_ROBOT,
    /**
     * Full screen exclusive.
     */
    FULL_SCREEN_EXCLUSIVE,
    /**
     * Listen to all AWT events.
     */
    LISTEN_TO_ALL_AWT_EVENTS,
    /**
     * Read display pixels.
     */
    READ_DISPLAY_PIXELS,
    /**
     * Replace keyboard focus manager.
     */
    REPLACE_KEYBOARD_FOCUS_MANAGER,
    /**
     * Set applet stub.
     */
    SET_APPLET_STUB,
    /**
     * Set window always on top.
     */
    SET_WINDOWS_ALWAYS_ON_TOP,
    /**
     * Show window without warning banner.
     */
    SHOW_WINDOW_WITHOUT_WARNING_BANNER,
    /**
     * Toolkit modality.
     */
    TOOLKIT_MODALITY,
    /**
     * Watch mouse pointer.
     */
    WATCH_MOUSE_POINTER,

    /* From FilePermission */

    /**
     * Read file.
     */
    READ_FILE,                              // Handling complete
    /**
     * Write file.
     */
    WRITE_FILE,                             // Handling complete
    /**
     * Execute file.
     */
    EXECUTE_FILE,                           // Handling complete
    /**
     * Delete file.
     */
    DELETE_FILE,                            // Handling complete
    /**
     * Read link.
     */
    READ_LINK,

    /* From SerializablePermission */

    /**
     * Enable subclass implementation.
     */
    ENABLE_SUBCLASS_IMPLEMENTATION,
    /**
     * Enable substitution.
     */
    ENABLE_SUBSTITUTION,

    /* From ManagementPermission */

    /**
     * Management control.
     */
    MANAGEMENT_CONTROL,
    /**
     * Management monitor.
     */
    MANAGEMENT_MONITOR,

    /* From ReflectPermission */

    /**
     * Supress access checks.
     */
    SUPRESS_ACCESS_CHECKS,
    /**
     * New proxy in package.
     */
    NEW_PROXY_IN_PACKAGE,

    /* From RuntimePermission */

    /**
     * Create class loader.
     */
    CREATE_CLASS_LOADER,
    /**
     * Get class loader.
     */
    GET_CLASS_LOADER,
    /**
     * Set context class cloader.
     */
    SET_CONTEXT_CLASS_LOADER,
    /**
     * Enable context class loader override.
     */
    ENABLE_CONTEXT_CLASS_LOADER_OVERRIDE,
    /**
     * Close class loader.
     */
    CLOSE_CLASS_LOADER,
    /**
     * Set security manager.
     */
    SET_SECURITY_MANAGER,
    /**
     * Create security manager.
     */
    CREATE_SECURITY_MANAGER,
    /**
     * Shutdown hooks.
     */
    SHUTDOWN_HOOKS,
    /**
     * Set factory.
     */
    SET_FACTORY,
    /**
     * Set I/O.
     */
    SET_IO,
    /**
     * Modify thread.
     */
    MODIFY_THREAD,
    /**
     * Stop thread.
     */
    STOP_THREAD,
    /**
     * Modify thread group.
     */
    MODIFY_THREAD_GROUP,
    /**
     * Get protection domain.
     */
    GET_PROTECTION_DOMAIN,
    /**
     * Get file system attributes.
     */
    GET_FILE_SYSTEM_ATTRIBUTES,
    /**
     * Read file descriptor.
     */
    READ_FILE_DESCRIPTOR,
    /**
     * Write file description.
     */
    WRITE_FILE_DESCRIPTOR,
    /**
     * Access declared members.
     */
    ACCESS_DECLARED_MEMBERS,
    /**
     * Queue print job.
     */
    QUEUE_PRINT_JOB,
    /**
     * Get stack trace.
     */
    GET_STACK_TRACE,
    /**
     * Set deauflt uncaught exception handler.
     */
    SET_DEFAULT_UNCAUGHT_EXCEPTION_HANDLER,
    /**
     * Runtime preferences.
     */
    RUNTIME_PREFERENCES,
    /**
     * Use policy.
     */
    USE_POLICY,
    /**
     * Select provider.
     */
    SELECT_PROVIDER,
    /**
     * Charset provider.
     */
    CHARSET_PROVIDER,
    /**
     * Get env.
     */
    GET_ENV,
    /**
     * Exit VM.
     */
    EXIT_VM,
    /**
     * Load library.
     */
    LOAD_LIBRARY,
    /**
     * Access class in package.
     */
    ACCESS_CLASS_IN_PACKAGE,
    /**
     * Define class in package.
     */
    DEFINE_CLASS_IN_PACKAGE,

    /* From NetPermission */

    /**
     * Set default net authenticator.
     */
    SET_DEFAULT_NET_AUTHENTICATOR,
    /**
     * Request password authenticator.
     */
    REQUEST_PASSWORD_AUTHENTICATOR,
    /**
     * Specify stream handler.
     */
    SPECIFY_STREAM_HANDLER,
    /**
     * Set proxy selector.
     */
    SET_PROXY_SELECTOR,
    /**
     * Get proxy selector.
     */
    GET_PROXY_SELECTOR,
    /**
     * Set cookie handler.
     */
    SET_COOKIE_HANDLER,
    /**
     * Get cookie handler.
     */
    GET_COOKIE_HANDLER,
    /**
     * Set response cache.
     */
    SET_RESPONSE_CACHE,
    /**
     * Get response cache.
     */
    GET_RESPONSE_CACHE,

    /* From SocketPermission */

    /**
     * Accept socket.
     */
    ACCEPT_SOCKET,
    /**
     * Connect socket.
     */
    CONNECT_SOCKET,
    /**
     * Listen socket.
     */
    LISTEN_SOCKET,
    /**
     * Resolve socket.
     */
    RESOLVE_SOCKET,

    /* From URLPermission */

    /**
     * URL GET.
     */
    URL_GET,
    /**
     * URL HEAD.
     */
    URL_HEAD,
    /**
     * URL POST.
     */
    URL_POST,
    /**
     * URL PUT.
     */
    URL_PUT,
    /**
     * URL DELETE.
     */
    URL_DELETE,
    /**
     * URL CONNECT.
     */
    URL_CONNECT,
    /**
     * URL OPTIONS.
     */
    URL_OPTIONS,
    /**
     * URL TRACE.
     */
    URL_TRACE,
    /**
     * URL PATCH.
     */
    URL_PATCH,

    /* From LinkPermission */

    /**
     * Hard link.
     */
    HARD_LINK,
    /**
     * Symbolic link.
     */
    SYMBOLIC_LINK,

    /* From AllPermission */

    /**
     * Grant all.
     */
    GRANT_ALL,

    /* From SecurityPermission */

    /**
     * Create access control context.
     */
    CREATE_ACCESS_CONTROL_CONTEXT,
    /**
     * Get domain combiner.
     */
    GET_DOMAIN_COMBINER,
    /**
     * Get policy.
     */
    GET_POLICY,
    /**
     * Set policy.
     */
    SET_POLICY,
    /**
     * Insert provider.
     */
    INSERT_PROVIDER,
    /**
     * Set system scope.
     */
    SET_SYSTEM_SCOPE,
    /**
     * Set identity public key.
     */
    SET_IDENTITY_PUBLIC_KEY,
    /**
     * Set identity info.
     */
    SET_IDENTITY_INFO,
    /**
     * Add identity certificate.
     */
    ADD_IDENTITY_CERTIFICATE,
    /**
     * Remove identity certificate.
     */
    REMOVE_IDENTITY_CERTIFICATE,
    /**
     * Print identity.
     */
    PRINT_IDENTITY,
    /**
     * Get signer private key.
     */
    GET_SIGNER_PRIVATE_KEY,
    /**
     * Set signer key pair.
     */
    SET_SIGNER_KEY_PAIR,
    /**
     * Create policy.
     */
    CREATE_POLICY,
    /**
     * Get security property.
     */
    GET_SECURITY_PROPERTY,
    /**
     * Set security property.
     */
    SET_SECURITY_PROPERTY,
    /**
     * Remove provider.
     */
    REMOVE_PROVIDER,
    /**
     *  Clear provider properties.
     */
    CLEAR_PROVIDER_PROPERTIES,
    /**
     * Put provider properties.
     */
    PUT_PROVIDER_PROPERTIES,
    /**
     * Remove provider property.
     */
    REMOVE_PROVIDER_PROPERTY,
    /**
     * Insert provider named.
     */
    INSERT_PROVIDER_NAMED,

    /* From UnresolvedPermission */

    /**
     * Unresolved.
     */
    UNRESOLVED,

    /* From SQLPermission */

    /**
     * Set SQL log.
     */
    SET_SQL_LOG,
    /**
     * Call SQL abort.
     */
    CALL_SQL_ABORT,
    /**
     * Set SQL sync factory.
     */
    SET_SQL_SYNC_FACTORY,
    /**
     * Set SQL network timeout.
     */
    SET_SQL_NETWORK_TIMEOUT,
    /**
     * Deregister SQL driver.
     */
    DEREGISTER_SQL_DRIVER,

    /* From LoggingPermission */

    /**
     * Control log.
     */
    CONTROL_LOG,

    /* From PropertyPermission */

    /**
     * Read property.
     */
    READ_PROPERTY,
    /**
     * Write property.
     */
    WRITE_PROPERTY,

    /* From MBeanPermission */

    /**
     * Add mbean notification listener.
     */
    ADD_MBEAN_NOTIFICATION_LISTENER,
    /**
     * Ger mbean attribute.
     */
    GET_MBEAN_ATTRIBUTE,
    /**
     * Get mbean classloader.
     */
    GET_MBEAN_CLASSLOADER,
    /**
     * Get mbean classloader for.
     */
    GET_MBEAN_CLASSLOADER_FOR,
    /**
     * Get mbean classloader repository.
     */
    GET_MBEAN_CLASS_LOADER_REPOSITORY,
    /**
     * Get mbean domains.
     */
    GET_MBEAN_DOMAINS,
    /**
     * Get mbean info.
     */
    GET_MBEAN_INFO,
    /**
     * Get mbean object instance.
     */
    GET_MBEAN_OBJECT_INSTANCE,
    /**
     * Mbean instantiate.
     */
    MBEAN_INSTANTIATE,
    /**
     * Mbean invoke.
     */
    MBEAN_INVOKE,
    /**
     * Mbean is instance of.
     */
    MBEAN_IS_INSTANCE_OF,
    /**
     * Query mbeans.
     */
    QUERY_MBEANS,
    /**
     * Mbean query names.
     */
    MBEAN_QUERY_NAMES,
    /**
     * Register mbean.
     */
    REGISTER_MBEAN,
    /**
     * Remove mbean notification listener.
     */
    REMOVE_MBEAN_NOTIFICATION_LISTENER,
    /**
     * Set mbean attribute.
     */
    SET_MBEAN_ATTRIBUTE,
    /**
     * Unregister mbean.
     */
    UNREGISTER_MBEAN,

    /* From MBeanServerPermission */

    /**
     * Create mbean server.
     */
    CREATE_MBEAN_SERVER,
    /**
     * Find mbeam server.
     */
    FIND_MBEAN_SERVER,
    /**
     * New mbean server.
     */
    NEW_MBEAN_SERVER,
    /**
     * Release mbean server.
     */
    RELEASE_MBEAN_SERVER,

    /* From MBeanTrustPermission */

    /**
     * Register mbean 2.
     */
    REGISTER_MBEAN_2,
    /**
     * Mbean all.
     */
    MBEAN_ALL,

    /* From SubjectDelegationPermission */

    /**
     * Delegate subject.
     */
    DELEGATE_SUBJECT,

    /* From SSLPermission */

    /**
     * Set hostname verifier.
     */
    SET_HOSTNAME_VERIFIER,
    /**
     * Get SSL session context.
     */
    GET_SSL_SESSION_CONTEXT,
    /**
     * Set default SSL context.
     */
    SET_DEFAULT_SSL_CONTEXT,

    /* From AuthPermission */

    /**
     * Do as.
     */
    DO_AS,
    /**
     * Do as privileged.
     */
    DO_AS_PRIVILEGED,
    /**
     * Get subject.
     */
    GET_SUBJECT,
    /**
     * Get subject from domain combiner.
     */
    GET_SUBJECT_FROM_DOMAIN_COMBINER,
    /**
     * Set read only.
     */
    SET_READ_ONLY,
    /**
     * Modify principals.
     */
    MODIFY_PRINCIPALS,
    /**
     * Modify public credentials.
     */
    MODIFY_PUBLIC_CREDENTIALS,
    /**
     * Modify private credentials.
     */
    MODIFY_PRIVATE_CREDENTIALS,
    /**
     * Refresh credential.
     */
    REFRESH_CREDENTIAL,
    /**
     * Destroy credential.
     */
    DESTROY_CREDENTIAL,
    /**
     * Get login configuration.
     */
    GET_LOGIN_CONFIGURATION,
    /**
     * Set login configuration.
     */
    SET_LOGIN_CONFIGURATION,
    /**
     * Refresh login configuration.
     */
    REFRESH_LOGIN_CONFIGURATION,
    /**
     * Create login context.
     */
    CREATE_LOGIN_CONTEXT,
    /**
     * Create login configuration.
     */
    CREATE_LOGIN_CONFIGURATION,

    /* From DelegationPermission */

    /**
     * Delegate.
     */
    DELEGATE,

    /* From ServicePermission */

    /**
     * Initiate service.
     */
    INITIATE_SERVICE,
    /**
     * Accept service.
     */
    ACCEPT_SERVICE,

    /* From PrivateCredentialPermission */

    /**
     * Read private credentials.
     */
    READ_PRIVATE_CREDENTIALS,

    /* From AudioPermission */

    /**
     * Play audio.
     */
    PLAY_AUDIO,
    /**
     * Record audio.
     */
    RECORD_AUDIO,

    /* From JAXBPermission */

    /**
     * Set JAXB datatype converter.
     */
    SET_JAXB_DATATYPE_CONVERTER,

    /* From WebServicePermission */

    /**
     * Publish webservice endpoint.
     */
    PUBLISH_WEBSERVICE_ENDPOINT
}

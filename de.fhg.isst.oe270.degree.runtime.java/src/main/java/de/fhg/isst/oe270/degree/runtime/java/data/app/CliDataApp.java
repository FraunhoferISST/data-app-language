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
package de.fhg.isst.oe270.degree.runtime.java.data.app;

import de.fhg.isst.degree.types.gen.degree.Activity;
import de.fhg.isst.degree.types.gen.degree.ActivityInstance;
import de.fhg.isst.oe270.degree.activities.annotations.ActivityAnnotation;
import de.fhg.isst.oe270.degree.activities.api.ActivityApi;
import de.fhg.isst.oe270.degree.activities.execution.OutputScope;
import de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation;
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi;
import de.fhg.isst.oe270.degree.registry.instances.execution.container.EmbeddedExecutionContainer;
import de.fhg.isst.oe270.degree.registry.instances.execution.container.JavaExecutionContainer;
import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext;
import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox;
import de.fhg.isst.oe270.degree.runtime.java.security.functionality.modules.DegreeFileOperations;
import de.fhg.isst.oe270.degree.runtime.java.security.manager.DegreeSecurityManager;
import de.fhg.isst.oe270.degree.runtime.java.usage.control.object.UsageControlObjectFactory;
import de.fhg.isst.oe270.degree.runtime.java.usage.control.object.UsageControlObjectType;
import de.fhg.isst.oe270.degree.types.RuntimeDefinitionRegistry;
import de.fhg.isst.oe270.degree.types.TypeTaxonomy;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import nukleus.core.Format;
import nukleus.core.Identifier;
import nukleus.core.Instance;
import nukleus.core.Nukleus;
import nukleus.core.Scope;
import nukleus.core.Type;
import nukleus.core.custom.DegreeCustomization;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * D° application which offers a command line interface.
 */
@SpringBootApplication
public abstract class CliDataApp implements DataApp {

    /**
     * This key is used to identify the name item within the configuration map.
     */
    public static final String NAME_KEY = "name";
    /**
     * This key is used to identify the namespace item within the configuration map.
     */
    public static final String NAMESPACE_KEY = "namespace";
    /**
     * This key is used to identify the namespace item within the configuration map.
     */
    public static final String VERSION_KEY = "version";
    /**
     * This key is used to identify the startup policies item within the configuration map.
     */
    public static final String STARTUP_POLICIES_KEY = "startupPolicies";
    /**
     * This key is used to identify the tags item within the configuration map.
     */
    public static final String TAGS_KEY = "tags";
    /**
     * This key is used to identify the execution type item within the configuration map.
     */
    public static final String EXECUTION_BEHAVIOUR_KEY = "execution";
    /**
     * This key is used to identify the periodic time item within the configuration map.
     */
    public static final String PERIODIC_TIME_KEY = "periodicTime";
    /**
     * This key is used to identify the usage control object type item within the configuration map.
     */
    public static final String USAGE_CONTROL_OBJECT_TYPE_KEY = "usageControlObject";
    /**
     * This map contains all key value entries from data app configuration.
     */
    protected static final HashMap<String, String> CONFIGURATION_MAP = new HashMap<>();

    /**
     * This map contains all activity implementations, identified by their name.
     */
    protected static final HashMap<String, ActivityApi> ACTIVITY_MAP = new HashMap<>();

    /**
     * This map contains all policy implementations, identified by their name.
     */
    protected static final HashMap<String, EmbeddedPolicyApi> POLICY_MAP = new HashMap<>();

    /**
     * The used logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger("DefaultDataApp");

    /**
     * The type taxonomy for this application.
     */
    protected static final TypeTaxonomy TYPE_TAXONOMY = TypeTaxonomy.getInstance();

    /**
     * The sandbox is used to execute activity calls.
     */
    protected static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Time in ms to sleep between queries for execution results.
     */
    @SuppressWarnings("unused")
    protected static final long SLEEP_INTERVAL = 250L;

    /**
     * Flag indicating if the application has been initialized.
     */
    private static boolean isInitialized = false;

    static {
        // set the default namespace
        Nukleus.custom = new DegreeCustomization();
    }

    /**
     * The D° security manager, used by this application.
     */
    private final DegreeSecurityManager degreeSecurityManager = new DegreeSecurityManager(this);

    /**
     * The execution context, used by this application.
     */
    private final ExecutionContext executionContext = ExecutionContext.getInstance();

    /**
     * List of tags which are applied to this application.
     */
    private final List<String> tags = new ArrayList<>();

    /**
     * The policy instance registry, retrieved from the runtime definition registry.
     */
    private Scope policyInstanceRegistry;

    /**
     * The activity instance registry, retrieved from the runtime definition registry.
     */
    private Scope activityInstanceRegistry;

    /**
     * Get the execution context.
     *
     * @return the execution context of this application
     */
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Add a tag to the application.
     *
     * @param tag the tag to add
     */
    public void addTag(final String tag) {
        tags.add(tag);
    }

    /**
     * Get the tags of this application.
     *
     * @return the tags of this application
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Validate inputs and provide usage instructions to user if required.
     *
     * @param args Data App inputs
     */
    @SuppressWarnings("unused")
    protected static void validateInputs(final String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect usage of CliDataApp. Going to exit now.");
            String jarName = System.getProperty("java.class.path");
            if (jarName.contains(File.separator)) {
                jarName = jarName.substring(1 + jarName.lastIndexOf(File.separator));
            }
            System.out.println("Usage: java -jar " + jarName + " inputScope");
            System.exit(-1);
        }
    }

    /**
     * Shortcut to create an {@link OutputScope} with single error message.
     *
     * @param error The error message
     * @return An {@link OutputScope} which contains the given error message as
     * Error-{@link Instance}
     */
    protected static OutputScope createErrorOutputScope(final String error) {
        OutputScope r = new OutputScope();

        Instance e = TYPE_TAXONOMY.create(new Identifier("core.Error"));
        e.write(error);

        r.getValues().put("error", e);

        return r;
    }

    /**
     * Shortcut to create an {@link OutputScope} with single status message.
     *
     * @param status The status message
     * @return An {@link OutputScope} which contains the given error message as
     * Error-{@link Instance}
     */
    protected static OutputScope createStatusOutputScope(final String status) {
        OutputScope r = new OutputScope();

        Instance e = TYPE_TAXONOMY.create(new Identifier("core.Text"));
        e.write(status);

        r.getValues().put("status", e);

        return r;
    }

    /**
     * Log a debug message.
     *
     * @param msg the message to log
     */
    @SuppressWarnings("unused")
    protected static void logDebug(final String msg) {
        LOGGER.debug(msg);
    }

    /**
     * Log a debug message with a throwable.
     *
     * @param msg the message to log
     * @param t   the throwable to log
     */
    @SuppressWarnings("unused")
    protected static void logDebug(final String msg, final Throwable t) {
        LOGGER.debug(msg, t);
    }

    /**
     * Log a error message.
     *
     * @param msg the message to log
     */
    @SuppressWarnings("unused")
    protected static void logError(final String msg) {
        LOGGER.error(msg);
    }

    /**
     * Log a error message with a throwable.
     *
     * @param msg the message to log
     * @param t   the throwable to log
     */
    @SuppressWarnings("unused")
    protected static void logError(final String msg, final Throwable t) {
        LOGGER.error(msg, t);
    }

    /**
     * Log a info message.
     *
     * @param msg the message to log
     */
    @SuppressWarnings("unused")
    protected static void logInfo(final String msg) {
        LOGGER.info(msg);
    }

    /**
     * Log a info message with a throwable.
     *
     * @param msg the message to log
     * @param t   the throwable to log
     */
    @SuppressWarnings("unused")
    protected static void logInfo(final String msg, final Throwable t) {
        LOGGER.info(msg, t);
    }

    /**
     * Log a trace message.
     *
     * @param msg the message to log
     */
    @SuppressWarnings("unused")
    protected static void logTrace(final String msg) {
        LOGGER.trace(msg);
    }

    /**
     * Log a trace message with a throwable.
     *
     * @param msg the message to log
     * @param t   the throwable to log
     */
    @SuppressWarnings("unused")
    protected static void logTrace(final String msg, final Throwable t) {
        LOGGER.trace(msg, t);
    }

    /**
     * Log a warn message.
     *
     * @param msg the message to log
     */
    @SuppressWarnings("unused")
    protected static void logWarn(final String msg) {
        LOGGER.warn(msg);
    }

    /**
     * Log a warn message with a throwable.
     *
     * @param msg the message to log
     * @param t   the throwable to log
     */
    @SuppressWarnings("unused")
    protected static void logWarn(final String msg, final Throwable t) {
        LOGGER.warn(msg, t);
    }

    /**
     * Initialize the application.
     * If the application is already initialized, nothing will happen.
     */
    protected void init() {
        synchronized (CliDataApp.class) {
            if (isInitialized) {
                return;
            }
            isInitialized = true;
        }
        try {
            RuntimeDefinitionRegistry runtimeDefinitionRegistry =
                    RuntimeDefinitionRegistry.getInstance();
            // load the types into the type taxonomy
            TYPE_TAXONOMY.deserialize(
                    Format.json.parse(this.getClass().getClassLoader(), "types.json"));
            URL nukleusPolicies = this.getClass().getClassLoader()
                    .getResource("nukleus.policies.yaml");
            if (nukleusPolicies != null) {
                Nukleus.policy.deserialize(Format.yaml.parse(nukleusPolicies));
            }
            logInfo("Loaded " + TYPE_TAXONOMY.size() + " types.");

            StringBuilder loadedTypes = new StringBuilder("Loaded Types: ");
            for (Identifier type : TYPE_TAXONOMY.listTypes()) {
                loadedTypes.append("'").append(type.toString()).append("', ");
            }
            loadedTypes = new StringBuilder(loadedTypes.substring(0, loadedTypes.length() - 2));
            logDebug(loadedTypes.toString());

            // find all annotated java activities
            List<Class<?>> annotatedActivities =
                    findAnnotatedClasses(
                            "de.fhg.isst.oe270.degree.activities.annotations.ActivityAnnotation");
            // extract <activityName, activityApi> and store in map
            this.processAnnotatedClasses(
                    annotatedActivities, ACTIVITY_MAP, ActivityAnnotation.class);
            logInfo("Loaded " + ACTIVITY_MAP.size() + " annotated activities.");

            // find all annotated java policies
            List<Class<?>> annotatedPolicies =
                    findAnnotatedClasses(
                            "de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation");
            // extract <policyName, policyApi> and store in map
            this.processAnnotatedClasses(annotatedPolicies, POLICY_MAP, PolicyAnnotation.class);
            logInfo("Loaded " + POLICY_MAP.size() + " annotated policies.");

            // load the runtime definition registry
            RuntimeDefinitionRegistry.getInstance().load(
                    Format.json.parse(getClass().getClassLoader(), "registry.json"));

            // initialize subsystems
            policyInstanceRegistry = RuntimeDefinitionRegistry.getInstance().policyRegistry();
            activityInstanceRegistry = RuntimeDefinitionRegistry.getInstance().activityRegistry();

            logInfo("Loaded " + policyInstanceRegistry.size() + " policy ("
                    + policyInstanceRegistry.instanceSet(
                            transformIdentifierToType(policyInstanceRegistry,
                                    "degree.Policy")).size() + ") & constraint ("
                    + policyInstanceRegistry.instanceSet(
                            transformIdentifierToType(policyInstanceRegistry,
                                    "degree.Constraint")).size() + ") definitions,"
                    + "policy ("
                    + policyInstanceRegistry.instanceSet(
                            transformIdentifierToType(policyInstanceRegistry,
                                    "degree.PolicyInstance")).size() + ") & constraint ("
                    + policyInstanceRegistry.instanceSet(
                            transformIdentifierToType(policyInstanceRegistry,
                                    "degree.ConstraintInstance")).size() + ") instances.");
            logInfo("Loaded "
                    + activityInstanceRegistry.instanceSet(
                            transformIdentifierToType(activityInstanceRegistry,
                                    "degree.Activity")).size()
                    + " activity definitions.");
            logInfo("Loaded "
                    + activityInstanceRegistry.instanceSet(
                            transformIdentifierToType(activityInstanceRegistry,
                                    "degree.ActivityInstance")).size()
                    + " activity instances.");
            int embeddedActivityCount = 0;
            for (Instance instance : activityInstanceRegistry.instanceSet(
                    transformIdentifierToType(activityInstanceRegistry,
                            "degree.ActivityInstance"))) {
                ActivityInstance activityInstance = (ActivityInstance) instance;
                if (StringUtils.isNotBlank(activityInstance
                        .lookupDefinition().getCodeBlock().read())) {
                    embeddedActivityCount++;
                }
            }
            logInfo("Loaded " + embeddedActivityCount + " embedded activities.");

            // search for D°-Activities which are defined by D°-code
            AtomicInteger embeddedActivities = new AtomicInteger();
            (activityInstanceRegistry.instanceSet(transformIdentifierToType(
                    activityInstanceRegistry, "degree.Activity"))).forEach(it -> {
                if (((Activity) it).getCodeBlock() != null && !((Activity) it)
                        .getCodeBlock().read().isEmpty()) {
                    runtimeDefinitionRegistry.registerContainer(
                            Identifier.of(((Activity) it).getName().read()),
                            new EmbeddedExecutionContainer()
                    );
                    embeddedActivities.getAndIncrement();
                }
            });
            logInfo(embeddedActivities.get() + " of "
                    + activityInstanceRegistry.instanceSet(
                            transformIdentifierToType(activityInstanceRegistry,
                                    "degree.Activity")).size()
                    + " are purely defined within Degree.");

            // map implemented java activities to definitions
            AtomicInteger mappedActivities = new AtomicInteger();
            ACTIVITY_MAP.forEach((key, value) -> {
                Identifier instanceName = Identifier.of(key);
                // check if there is an activity definition within the scope which
                // matches the current annotation
                if (runtimeDefinitionRegistry.contains(instanceName)) {
                    runtimeDefinitionRegistry.registerContainer(instanceName,
                            new JavaExecutionContainer<>(value));
                    mappedActivities.getAndIncrement();
                }
            });
            logInfo("Mapped " + mappedActivities.get() + " of "
                    + annotatedActivities.size()
                    + " annotated java activities to activity definitions.");

            // map implemented java policies to definitions
            AtomicInteger mappedPolicies = new AtomicInteger();
            POLICY_MAP.forEach((key, value) -> {
                Identifier instanceName = Identifier.of(key);
                if (runtimeDefinitionRegistry.policyRegistry().contains(instanceName)) {
                    runtimeDefinitionRegistry.registerContainer(instanceName,
                            new JavaExecutionContainer<>(value));
                    mappedPolicies.getAndIncrement();
                }
            });
            logInfo("Mapped " + mappedPolicies.get() + " of " + annotatedPolicies.size()
                    + " annotated java policies and constraint to policy definitions.");

            // load all degree operation modules which allow access to functions
            // proteced by the security manager
            initOperationModules();

            // set the security manager
            System.setSecurityManager(degreeSecurityManager);
            logInfo("Activated Degree-SecurityManager.");

            SANDBOX.setDataApp(this);
            tags.add("DATA_APP");
        } catch (Exception e) {
            LOGGER.error("Error during initialization of data app.", e);
        }
    }

    /**
     * Retrieve a type with given identifier from scope's type system.
     *
     * @param scope Scope which type system will be used
     * @param ident The identifier which will be transformed
     * @return Type which is identified by ident
     */
    protected Type transformIdentifierToType(final Scope scope, final Identifier ident) {
        return scope.getTypeSystem().lookup(ident);
    }

    /**
     * Retrieve a type with given identifier from scope's type system.
     *
     * @param scope Scope which type system will be used
     * @param ident The identifier which will be transformed
     * @return Type which is identified by ident
     */
    protected Type transformIdentifierToType(final Scope scope, final String ident) {
        return transformIdentifierToType(scope, Identifier.of(ident));
    }

    /**
     * Build and set the UsageControlObject for this application.
     *
     * @param value configuration item with identifier 'usageControlObject'
     */
    protected void createUsageControlObject(final String value) {
        UsageControlObjectType type = UsageControlObjectType.DEGREE;
        switch (value) {
            case "D°":
                type = UsageControlObjectType.DEGREE;
                break;
            case "IDS":
                type = UsageControlObjectType.IDS;
                break;
            default:
                logError("Tried to create unknown type of usage control object.");
                break;
        }
        UsageControlObjectFactory.INSTANCE
                .type(type)
                .build();
    }

    private void initOperationModules() {
        DegreeFileOperations.init();
    }

    /**
     * Scan the class path for all classes annotated with a specific annotation.
     *
     * @param classname the name of the annotation
     * @return all annotated classes
     */
    protected List<Class<?>> findAnnotatedClasses(final String classname) {
        ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages("*")
                .scan();

        return scanResult.getClassesWithAnnotation(classname).loadClasses();
    }

    /**
     * Process a list of annotated classes, extract the qualified name from the specified
     * annotation, and store <qualified name, annotated class> pairs in a given result map.
     *
     * @param annotatedClasses the annotated classes
     * @param target           the map where results are stored
     * @param annotationClass  the used annotation class
     * @param <API>            the target class
     */
    @SuppressWarnings("unchecked")
    protected <API> void processAnnotatedClasses(
            final List<Class<?>> annotatedClasses,
            final HashMap<String, API> target,
            final Class<?> annotationClass) {
        // iterate over all annotated classes
        for (Class<?> clazz : annotatedClasses) {
            String key = null;
            API value = null;
            // iterate over all annotations
            for (Annotation annotation : clazz.getAnnotations()) {
                // check if we found the right annotation
                if (annotation.annotationType().equals(annotationClass)) {
                    try {
                        // extract key from annotation
                        key = (String) annotationClass.getMethod("qualifiedName")
                                .invoke(annotationClass.cast(annotation));
                        // create instance from annotation
                        value = (API) clazz.getConstructor().newInstance();
                    } catch (InstantiationException
                            | IllegalAccessException
                            | InvocationTargetException
                            | NoSuchMethodException e) {
                        logError("Error during instantiation of annotated class.", e);
                    }
                    break;
                }
            }
            if (key != null && value != null) {
                target.put(key, value);
            }
        }
    }

    /**
     * Get name of this Data App from Data App configuration.
     *
     * @return The name of this Data App, or an empty string if unknown.
     */
    @SuppressWarnings("unused")
    public String getName() {
        if (!CONFIGURATION_MAP.containsKey(NAME_KEY)) {
            logWarn("Could not resolve name for this Data App.");
            return "";
        }
        return CONFIGURATION_MAP.get(NAME_KEY);
    }

    /**
     * Get namespace of this Data App from Data App configuration.
     *
     * @return The namespace of this Data App, or an empty string if unknown.
     */
    @SuppressWarnings("unused")
    public String getNamespace() {
        if (!CONFIGURATION_MAP.containsKey(NAMESPACE_KEY)) {
            logWarn("Could not resolve namespace for this Data App.");
            return "";
        }
        return CONFIGURATION_MAP.get(NAMESPACE_KEY);
    }

    /**
     * Get version of this Data App from Data App configuration.
     *
     * @return The version of this Data App, or an empty string if unknown.
     */
    public String getVersion() {
        if (!CONFIGURATION_MAP.containsKey(VERSION_KEY)) {
            logWarn("Could not resolve version for this Data App.");
            return "";
        }
        return CONFIGURATION_MAP.get(VERSION_KEY);
    }

    /**
     * Get execution behaviour of this Data App from Data App configuration.
     *
     * @return The execution behaviour of this Data App, or an empty string if unknown.
     */
    @SuppressWarnings("unused")
    public String getExecutionBehaviour() {
        if (!CONFIGURATION_MAP.containsKey(EXECUTION_BEHAVIOUR_KEY)) {
            logError("Could not resolve execution behaviour for this Data App.");
            return "";
        }
        return CONFIGURATION_MAP.get(EXECUTION_BEHAVIOUR_KEY);
    }

    /**
     * Get startup policies of this Data App from Data App configuration.
     *
     * @return The version of this Data App, or an empty string if unknown.
     */
    @SuppressWarnings("unused")
    public List<String> getStartupPolicies() {
        if (!CONFIGURATION_MAP.containsKey(STARTUP_POLICIES_KEY)) {
            logInfo("There are no startup policies for this Data App.");
            return Collections.emptyList();
        }
        List<String> policies = Arrays.asList(CONFIGURATION_MAP.get(STARTUP_POLICIES_KEY)
                .split(","));
        for (int i = 0; i < policies.size(); i++) {
            policies.set(i, policies.get(i).trim());
        }

        return policies;
    }

    /**
     * Check if the application has a specific tag.
     *
     * @param tag the tag to check
     * @return true, if the appliaticon has the given tag, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean containsTag(final String tag) {
        return tags.contains(tag);
    }
}

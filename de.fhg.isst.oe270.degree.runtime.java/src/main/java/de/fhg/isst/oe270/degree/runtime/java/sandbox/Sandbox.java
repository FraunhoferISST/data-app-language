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
package de.fhg.isst.oe270.degree.runtime.java.sandbox;

import de.fhg.isst.degree.types.gen.degree.Activity;
import de.fhg.isst.degree.types.gen.degree.ActivityInstance;
import de.fhg.isst.degree.types.gen.degree.ConstraintInstance;
import de.fhg.isst.degree.types.gen.degree.MappedPolicyInstanceMap;
import de.fhg.isst.degree.types.gen.degree.ParameterMappingsMap;
import de.fhg.isst.degree.types.gen.degree.Policy;
import de.fhg.isst.degree.types.gen.degree.PolicyInstance;
import de.fhg.isst.oe270.degree.activities.api.ActivityApi;
import de.fhg.isst.oe270.degree.activities.execution.InputScope;
import de.fhg.isst.oe270.degree.activities.execution.OutputScope;
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi;
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope;
import de.fhg.isst.oe270.degree.registry.instances.execution.container.EmbeddedExecutionContainer;
import de.fhg.isst.oe270.degree.registry.instances.execution.container.ExecutionContainer;
import de.fhg.isst.oe270.degree.registry.instances.execution.container.JavaExecutionContainer;
import de.fhg.isst.oe270.degree.registry.instances.execution.container.NOOPExecutionContainer;
import de.fhg.isst.oe270.degree.runtime.java.data.app.CliDataApp;
import de.fhg.isst.oe270.degree.runtime.java.exceptions.security.DegreeForbiddenSecurityFeatureException;
import de.fhg.isst.oe270.degree.runtime.java.security.evaluation.PermissionScope;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType;
import de.fhg.isst.oe270.degree.runtime.java.usage.control.object.UsageControlObject;
import de.fhg.isst.oe270.degree.types.RuntimeDefinitionRegistry;
import de.fhg.isst.oe270.degree.types.TypeTaxonomy;
import nukleus.core.CompositeInstance;
import nukleus.core.Format;
import nukleus.core.Identifier;
import nukleus.core.Instance;
import nukleus.core.InstanceResolver;
import nukleus.core.PrimitiveInstance;
import nukleus.core.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.PermissionMatchingStrategy.ALL_FILES;
import static de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.PermissionMatchingStrategy.preparePath;

/**
 * The Sandbox is used within Data Apps to execute activities and evaluate their policies.
 * <p>
 * Contains various @SuppressWarnings("unused") annotations since many methods are called
 * during runtime by the generated code.
 */
public final class Sandbox {

    /**
     * The used logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Sandbox.class.getSimpleName());

    /**
     * Since the JVM runs one Data App at a time, the Sandbox can be used as singleton.
     */
    private static Sandbox singleton = null;

    /**
     * The Data App which is using the sandbox for execution.
     */
    private CliDataApp dataApp = null;

    /**
     * The currently executed activity.
     */
    private ActivityInstance currentActivity = null;

    /**
     * The currently used input scope.
     */
    private InputScope currentInputScope = null;

    /**
     * Collection of currently used policies.
     */
    private Collection<CompositeInstance> currentPolicies = new ArrayList<>();

    /**
     * List of currently required permissions.
     */
    private List<RequiredPermission> currentRequiredPermissions = new ArrayList();

    /**
     * Current permission scope.
     */
    private final PermissionScope currentPermissionScope = PermissionScope.getInstance();

    /**
     * To ensure that initialization is only performed once, this flag is used.
     */
    private boolean initialized = false;

    /**
     * Empty default constructor.
     */
    private Sandbox() {

    }

    /**
     * Get the singleton instance for this class.
     *
     * @return the singleton instance.
     */
    public static Sandbox getInstance() {
        if (singleton == null) {
            synchronized (Sandbox.class) {
                if (singleton == null) {
                    singleton = new Sandbox();
                }
            }
        }

        return singleton;
    }

    /**
     * Sets the data app of this sandbox once. Ensures that the data app can not be overwritten
     * if it is already set.
     * <p>
     * Logs an error in case the data app shall be overwritten.
     *
     * @param cliDataApp The dat app which will be bound to the sandbox
     */
    public void setDataApp(final CliDataApp cliDataApp) {
        if (this.dataApp != null) {
            LOGGER.error("Tried to overwrite the data app of the sandbox.");
            return;
        }
        if (cliDataApp == null) {
            LOGGER.warn("Provided data app is null.");
        }
        this.dataApp = cliDataApp;
    }

    /**
     * Initializes the sandbox for a given data app. This includes the following steps:
     * - Evaluation of pre- and postconditions of startup policies
     *
     * @param configuration the configuration, used for initialization
     * @return true, if the initialization was successful or if the app is already
     * initialized, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean initialize(final HashMap<String, String> configuration) {
        if (initialized) {
            return true;
        }
        initialized = true;

        Scope policyInstanceRegistry = RuntimeDefinitionRegistry.getInstance().policyRegistry();
        boolean validationResult = true;
        HashMap<String, Instance> policies = new HashMap<>();

        if (!configuration.containsKey(CliDataApp.STARTUP_POLICIES_KEY)
                || configuration.get(CliDataApp.STARTUP_POLICIES_KEY).isEmpty()) {
            return true;
        }

        // evaluation of startup policies
        String[] startupPolicies = configuration.get(CliDataApp.STARTUP_POLICIES_KEY)
                .replace(" ", "").split(",");

        // iterate over all startup policies
        for (String startupPolicy : startupPolicies) {
            // ensure the name is in form of a qualified identifier (with namespace)
            Identifier startupPolicyIdentifier = Identifier.of(startupPolicy);

            if (!policyInstanceRegistry.contains(startupPolicyIdentifier)) {
                LOGGER.error("Required startup policy '" + startupPolicy
                        + "' is not known to this data app.");

                validationResult = false;
                continue;
            }
            // collect the startup policy
            policies.put(
                    startupPolicy,
                    policyInstanceRegistry.read(startupPolicyIdentifier)
            );
        }

        // evaluate pre- & postconditions for all found startup policies
        validationResult &= validatePrecondition(policies) && validatePostcondition(policies);

        if (validationResult) {
            LOGGER.info("Validation of startup policies was successful. "
                    + startupPolicies.length + " startup policies have been validated.");
        } else {
            LOGGER.error("Validation of startup policies failed.");
        }
        return validationResult;
    }

    /**
     * Validates the preconditions of a given set of policy and constraint instances and
     * return the evaluation result.
     *
     * @param policies arbitrary amount of policy- and constraint-instances
     * @return true if the validation of all elements is successful, false otherwise
     */
    private boolean validatePrecondition(final HashMap<String, Instance> policies) {
        boolean result = true;
        for (Map.Entry<String, Instance> entry : policies.entrySet()) {
            String policyName = entry.getKey();
            // PolicyInstance policyInstance = (PolicyInstance) entry.getValue();
            if (entry.getValue() instanceof ConstraintInstance) {
                result &= validateConstraintPrecondition(policyName,
                        ((ConstraintInstance) entry.getValue()));
            } else if (entry.getValue() instanceof PolicyInstance) {
                HashMap<String, Instance> mappedElements = new HashMap<>();
                ((PolicyInstance) entry.getValue()).getMappedElements().split()
                        .forEach(e ->
                                mappedElements.put(
                                        ((e.lookupValue() instanceof PolicyInstance)
                                                ? ((PolicyInstance) e.lookupValue()).getName()
                                                .read()
                                                : ((ConstraintInstance) e.lookupValue()).getName()
                                                .read()), e.lookupValue())
                        );
                result &= validatePrecondition(mappedElements);
            }
        }
        return result;
    }

    /**
     * Validates the precondition of a given constraint instance.
     *
     * @param policyName     The name of the constraint which precondition will be validated.
     * @param policyInstance The constraint instance which will be used for validation.
     * @return true if the validation is successful, false otherwise
     */
    private boolean validateConstraintPrecondition(
            final String policyName, final ConstraintInstance policyInstance) {
        LOGGER.info("Validating precondition of constraint '"
                + policyName + "'.");
        PolicyInputScope policyInputScope = buildInputScopeForConstraint(policyInstance);

        ExecutionContainer executionContainer =
                retrieveConstraintExecutionContainer(policyInstance);

        if (executionContainer instanceof NOOPExecutionContainer) {
            LOGGER.error("The constraint " + policyName
                    + " does not provide an executable context.");
            return false;
        } else if (executionContainer instanceof JavaExecutionContainer) {
            return ((EmbeddedPolicyApi) ((JavaExecutionContainer) executionContainer)
                    .getApiObject()).acceptPrecondition(policyInputScope);
        }
        return false;
    }

    /**
     * If a security manager intervention occurs, this method is used to evaluate it.
     *
     * @param constraintInstance the constraint used for evaluation
     * @return the created evaluation conditions
     */
    public Collection<EvaluationCondition> evaluateSecurityManagerIntervention(
            final ConstraintInstance constraintInstance) {
        PolicyInputScope policyInputScope = buildInputScopeForConstraint(constraintInstance);

        ExecutionContainer executionContainer =
                retrieveConstraintExecutionContainer(constraintInstance);

        if (executionContainer instanceof NOOPExecutionContainer) {
            LOGGER.error("The constraint " + constraintInstance.getName().read()
                    + " does not provide an executable context.");
            return new ArrayList<>();
        } else if (executionContainer instanceof JavaExecutionContainer) {
            return ((EmbeddedPolicyApi) ((JavaExecutionContainer) executionContainer)
                    .getApiObject()).evaluateSecurityManagerIntervention(policyInputScope);
        }
        return new ArrayList<>();
    }

    /**
     * Validates the postconditions of a given set of policy and constraint instances and
     * return the evaluation result.
     *
     * @param policies arbitrary amount of policy- and constraint-instances
     * @return true if the validation of all elements is successful, false otherwise
     */
    private boolean validatePostcondition(final HashMap<String, Instance> policies) {
        boolean result = true;
        for (Map.Entry<String, Instance> entry : policies.entrySet()) {
            String policyName = entry.getKey();
            CompositeInstance policyInstance = (CompositeInstance) entry.getValue();
            if (policyInstance instanceof ConstraintInstance) {
                result &= validateConstraintPostcondition(policyName,
                        (ConstraintInstance) policyInstance);
            } else if (policyInstance instanceof PolicyInstance) {
                HashMap<String, Instance> mappedElements = new HashMap<>();
                ((PolicyInstance) entry.getValue()).getMappedElements().split()
                        .forEach(e -> mappedElements.put(
                                ((e.lookupValue() instanceof PolicyInstance)
                                        ? ((PolicyInstance) e.lookupValue()).getName().read()
                                        : ((ConstraintInstance) e.lookupValue()).getName().read()),
                                e.lookupValue()));
                result &= validatePostcondition(mappedElements);
            }
        }
        return result;
    }

    /**
     * Validates the postcondition of a given constraint instance.
     *
     * @param policyName     The name of the constraint which precondition will be validated.
     * @param policyInstance The constraint instance which will be used for validation.
     * @return true if the validation is successful, false otherwise
     */
    private boolean validateConstraintPostcondition(
            final String policyName, final ConstraintInstance policyInstance) {
        LOGGER.info("Validating postcondition of constraint '"
                + policyName + "'.");
        PolicyInputScope policyInputScope = buildInputScopeForConstraint(policyInstance);

        ExecutionContainer executionContainer =
                retrieveConstraintExecutionContainer(policyInstance);

        if (executionContainer instanceof NOOPExecutionContainer) {
            LOGGER.error("The constraint " + policyName
                    + " does not provide an executable context.");
            return false;
        } else if (executionContainer instanceof JavaExecutionContainer) {
            return ((EmbeddedPolicyApi) ((JavaExecutionContainer) executionContainer)
                    .getApiObject()).acceptPostcondition(policyInputScope);
        }
        return false;
    }

    /**
     * Update currently handled elements.
     *
     * @param activity The currently handled activity instance
     * @param input    The input scope for the currently handled activity instance
     * @param policies All policies and constraints which apply to the currently handled activity
     */
    private void updateCurrentCall(final ActivityInstance activity,
                                   final InputScope input,
                                   final Collection<CompositeInstance> policies) {
        currentActivity = activity;
        currentInputScope = input;
        currentPolicies = policies;
    }

    /**
     * Executes a given activity instance with given input scope
     * and with respect to the given policies/constraints.
     *
     * @param activity The called activity instance.
     * @param input    Input scope for the called activity.
     * @param policies Policies which apply to the called activity.
     * @return The return value(s) of the called activity
     */
    @SuppressWarnings("unused")
    public OutputScope callActivity(
            final ActivityInstance activity,
            final InputScope input,
            final Collection<MappedPolicyInstanceMap> policies
    ) {
        // ensure that there are no artifacts from the last call
        currentPermissionScope.reset();
        // at first we need to resolve the policies
        Collection<CompositeInstance> resolvedPolicies = new ArrayList<>();
        Scope policyRegistry = RuntimeDefinitionRegistry.getInstance().policyRegistry();
        for (MappedPolicyInstanceMap mappedPolicy : policies) {
            resolvedPolicies.add(mappedPolicy.getValue().lookup());
        }
        // ensure that the current fields contain correct information
        updateCurrentCall(activity, input, resolvedPolicies);

        HashMap<String, Instance> policyMap = new HashMap<>();
        for (CompositeInstance elem : resolvedPolicies) {
            policyMap.put(elem.read(Policy.NAME), elem);
        }

        // add policies which apply to the data used
        for (Instance instance : input.getValues().values()) {
            String id = "";
            if (instance instanceof CompositeInstance) {
                id = ((CompositeInstance) instance).getIdentity().linkValue();
            } else {
                // primitive instances use the unique JVM identifier
                id = System.identityHashCode(instance) + "";
            }

            List<Instance> dataPolicies =
                    UsageControlObject.Companion.getUCObject().getDataPolicies().get(id);
            if (dataPolicies == null) {
                dataPolicies = new ArrayList<>();
            }
            for (Instance pol : dataPolicies) {
                policyMap.put(((CompositeInstance) pol).read(Policy.NAME), pol);
            }
        }

        if (!validatePrecondition(policyMap)) {
            LOGGER.error("Cannot execute activity '" + activity.getClass().getSimpleName()
                    + "' because of violations of preconditions.");
            return null;
        }

        OutputScope output = new OutputScope();

        Activity definition = activity.lookupDefinition();
        ExecutionContainer executionContainer = RuntimeDefinitionRegistry.getInstance()
                .retrieveContainer(new Identifier(definition.getName().read()));
        if (executionContainer instanceof NOOPExecutionContainer) {
            LOGGER.error("Tried to call Activity '" + activity.getName().read()
                    + "' with NOOP execution container.");
        } else if (executionContainer instanceof JavaExecutionContainer) {
            output = ((ActivityApi) ((JavaExecutionContainer) executionContainer)
                    .getApiObject()).run(input);
        } else if (executionContainer instanceof EmbeddedExecutionContainer
                && definition.getCodeBlock() != null) {
            try {
                String methodName = activity.getName().toString()
                        .replace('.', '_');
                Method method = dataApp.getClass().getMethod(methodName, InputScope.class);
                output = (OutputScope) method.invoke(dataApp, input);
            } catch (NoSuchMethodException e) {
                LOGGER.error("Could not find matching Java-method for activity "
                        + activity.getName().toString() + ".");
                output = null;
            } catch (IllegalAccessException e) {
                LOGGER.error("Could not access the Java-method for activity "
                        + activity.getName().toString() + ".");
                output = null;
            } catch (InvocationTargetException e) {
                LOGGER.error("Could not invoke Java-method for activity "
                        + activity.getName().toString() + " on Data App.");
                output = null;
            }
        } else {
            LOGGER.error("Tried to call unknown activity '"
                    + activity.getName().toString() + "'.");
        }

        if (!validatePostcondition(policyMap)) {
            LOGGER.error("Cannot execute activity '"
                    + activity.getClass().getSimpleName()
                    + "' because of violations of postconditions.");
            return null;
        }

        return output;
    }

    /**
     * Uses the information from the current permission scope to decide if the execution
     * is allowed to proceed.
     * By default everything is allowed if nothing different is stated.
     * So a blacklisting approach is used.
     * The {@link EvaluationCondition}s are the core of this evaluation.
     * <p>
     * At first all forbidding conditions are used to build the blacklist of forbidden actions.
     * Afterwards the remaining allowing conditions are used as whitelist which patch the blacklist.
     * <p>
     * The required permission is the action to perform.
     * <p>
     * Take a look at this visualization:
     * <p>
     * The Action to perform falls down vertically and the first element that is hit
     * will determine the result of the evaluation.
     * So allowing EvaluationConditions overweight forbidding ones.
     * <p>
     * | Action to perform
     * | (RequiredPermission)
     * V
     * <p>
     * Whitelist (allowing EvaluationConditions)
     * -->    #####             ###        ####       ####       #####
     * Blacklist (forbidding EvaluationConditions)
     * -->       xxx     xxxxxxxxxxxxx           xx  xxx           xxxxxx
     * Default
     * --> ################################################################
     */
    public void resolveCurrentPermissionScope() {
        // we only want to evaluate the permissions if there are any
        if (currentPermissionScope.getRequiredPermissions().isEmpty()) {
            return;
        }
        // evaluate all required permissions
        for (RequiredPermission requiredPermission : currentPermissionScope
                .getRequiredPermissions()) {
            // skip permissions which have been evaluated before
            if (currentPermissionScope.isPermissionEvaluated(requiredPermission)) {
                continue;
            }
            if (!evaluateRequiredPermission(requiredPermission)) {
                throw new DegreeForbiddenSecurityFeatureException(
                        "An error occurred during the evaluation of the required permission '"
                                + requiredPermission.toPrettyString() + "'.");
            }
        }
        // add all permissions which have been evaluated during this run to the evaluated list
        // this ensures that permissions are not evaluated multiple times if the activity call
        // issues another required permission
        currentPermissionScope.addEvaluatedPermissions(
                currentPermissionScope.getRequiredPermissions());
        // clear the evaluation conditions since they are generated on the next permission
        // anyways and that way we are aware of changes
        currentPermissionScope.getEvaluationConditions().clear();
    }

    private boolean evaluateRequiredPermission(final RequiredPermission requiredPermission) {
        LOGGER.info("Evaluating the following required permission: "
                + requiredPermission.toPrettyString());
        // separate forbidding from allowing evaluation conditions
        Set<EvaluationCondition> allowingConditions = new HashSet<>();
        Set<EvaluationCondition> forbiddingConditions = new HashSet<>();
        // during the filtering we can remove all conditions which do not match
        // the current requirement
        for (EvaluationCondition evaluationCondition : currentPermissionScope
                .getEvaluationConditions()) {
            if (requiredPermission.getCategory().equals(evaluationCondition.getCategory())
                    || evaluationCondition.getCategory().equals(DegreePermissionType.WILDCARD)) {
                if (evaluationCondition.isForbid()) {
                    forbiddingConditions.add(evaluationCondition);
                } else {
                    allowingConditions.add(evaluationCondition);
                }
            }
        }

        // referring to the documentation of {@link resolveCurrentPermissionScope} if
        // an allowing condition, which allows the action, is found, we can abort now
        for (EvaluationCondition allowingCondition : allowingConditions) {
            // if a match was found the action is allowed
            if (evaluatePermissionConditionPair(requiredPermission, allowingCondition)) {
                return true;
            }
        }
        // since we did not found any allowing condition for the required permission we have
        // to check the forbidding ones
        for (EvaluationCondition forbiddingCondition : forbiddingConditions) {
            // if a match was found the action is forbidden
            if (evaluatePermissionConditionPair(requiredPermission, forbiddingCondition)) {
                return false;
            }
        }
        // by default everything is allowed
        return true;
    }

    /**
     * Checks if an evaluation condition matches to a required permission.
     * If so this can either be an allowing condition or a forbidding one.
     *
     * @param requiredPermission A permission which is required for executing an action
     * @param condition          A condition which can either allow or deny an action if
     *                           it matches the permission
     * @return true if the condition matches the permission, false otherwise
     */
    private boolean evaluatePermissionConditionPair(
            final RequiredPermission requiredPermission, final EvaluationCondition condition) {
        // handle wildcard
        if (condition.getCategory().equals(DegreePermissionType.WILDCARD)) {
            return true;
        }
        boolean matchFound = false;
        switch (condition.getMatchingStrategy()) {
            case EXACT_MATCH:
                // This is the simplest type of matching since it checks for simple equality
                if (requiredPermission.getAttribute().equals(condition.getAttribute())) {
                    matchFound = true;
                }
                break;
            case PATH_EXACT_MATCH:
                // since file permissions allow <<ALL_FILES>> special value
                // we need to handle it
                if (condition.getAttribute().equals(ALL_FILES)) {
                    matchFound = true;
                    break;
                }
                // at first we have to ensure the path is an absolute path
                String requiredPath = preparePath(requiredPermission.getAttribute());
                // This is the simplest type of matching since it checks for simple equality
                if (requiredPath.replace("\\", "/")
                        .equals(condition.getAttribute().replace("\\", "/"))) {
                    matchFound = true;
                }
                break;
            case PATH_SUBDIR:
                // since file permissions allow <<ALL_FILES>> special value
                // we need to handle it
                if (condition.getAttribute().equals(ALL_FILES)) {
                    matchFound = true;
                    break;
                }
                // at first we have to ensure the path is an absolute path
                requiredPath = preparePath(requiredPermission.getAttribute());
                if (condition.getAttribute().replace("\\", "/")
                        .startsWith(requiredPath.replace("\\", "/"))) {
                    matchFound = true;
                }
                break;
            default:
                break;
        }
        return matchFound;
    }


    /**
     * Get the currently executed activity.
     *
     * @return the currently executed activity
     */
    @SuppressWarnings("unused")
    public ActivityInstance getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Get the current input scope.
     *
     * @return the current input scope
     */
    @SuppressWarnings("unused")
    public InputScope getCurrentInputScope() {
        return currentInputScope;
    }

    /**
     * Get currently required permissions.
     *
     * @return list of currently required permissions.
     */
    public List<RequiredPermission> getCurrentRequiredPermissions() {
        return currentRequiredPermissions;
    }

    /**
     * Set the currently required permissions.
     *
     * @param permissions the new required permissions
     */
    public void setCurrentRequiredPermissions(
            final List<RequiredPermission> permissions) {
        this.currentRequiredPermissions = permissions;
    }

    /**
     * Get currently used policies.
     *
     * @return the current policies
     */
    @SuppressWarnings("unused")
    public Collection<CompositeInstance> getCurrentPolicies() {
        return Collections.unmodifiableList(currentPolicies.stream().collect(Collectors.toList()));
    }

    /**
     * Retrieve the execution context for a given constraint/policy instance.
     *
     * @param policyInstance The policy/constraint instance which execution container
     *                       shall be retrieve.
     * @return The execution container of the policy/constraint instance.
     */
    private ExecutionContainer retrieveConstraintExecutionContainer(
            final Instance policyInstance) {
        CompositeInstance instance = InstanceResolver.lookup(
                (CompositeInstance) policyInstance, PolicyInstance.DEFINITION);
        return RuntimeDefinitionRegistry.getInstance().retrieveContainer(
                Identifier.of(instance.get(Policy.NAME).read()));
    }

    /**
     * Build up an input scope for a given constraint instance which contains all necessary
     * values to execute the policy.
     *
     * @param constraintInstance The constraint instance which input scope will be created
     * @return The input scope which is passed to the policy
     */
    private PolicyInputScope buildInputScopeForConstraint(
            final ConstraintInstance constraintInstance) {
        PolicyInputScope policyInputScope = new PolicyInputScope();

        // at first add the mapped elements
        HashMap<String, Instance> mappedElements = new HashMap<>();
        constraintInstance.getMappedElements().split().forEach(
                e -> mappedElements.put(e.getKey().read(),
                        TypeTaxonomy.getInstance().newInstance(Format.json.parse(
                                e.getValue().read())))
        );
        policyInputScope.getValues().putAll(mappedElements);

        // this part is only relevant if this is NOT a startup policy
        // --> we have a current activity
        if (currentActivity != null) {
            // find definition name of current policy
            String currentName = "";
            // the name resolution is only reasonable if there are parameter mappings
            if (currentActivity.getParameterMappings().size() > 0) {
                for (int i = 0; i < currentActivity.getPolicies().size(); i++) {
                    CompositeInstance entry =
                            currentActivity.getPolicies().get(i).getValue().lookup();
                    if (entry.read(Policy.NAME).equals(constraintInstance.getName().read())) {
                        currentName = currentActivity.getPolicies().get(i).getKey().read();
                        break;
                    }
                }
            }
            // process the activity -> policy attribute mappings
            for (int i = 0; i < currentActivity.getParameterMappings().size(); i++) {
                ParameterMappingsMap mappingEntry = currentActivity.getParameterMappings().get(i);
                for (String policyAttribute : mappingEntry.split().stream().map(
                        ParameterMappingsMap::getValue)
                        .map(PrimitiveInstance::read).collect(Collectors.toList())) {
                    String[] splittedPolicyIdentifier = policyAttribute.split("\\.");
                    if (splittedPolicyIdentifier[0].equals(currentName)) {
                        policyInputScope.add(splittedPolicyIdentifier[1],
                                Objects.requireNonNull(currentInputScope.get(
                                        mappingEntry.getKey().read())));
                    }
                }
            }
        }
        return policyInputScope;
    }

    /**
     * Collect all IDs from a constraint.
     *
     * @param instance the constraint
     * @return all collected IDs
     */
    public List<String> collectIds(final ConstraintInstance instance) {
        List<String> result = new ArrayList<>();

        PolicyInputScope policyInputScope = buildInputScopeForConstraint(instance);
        ExecutionContainer executionContainer = retrieveConstraintExecutionContainer(instance);

        if (executionContainer instanceof JavaExecutionContainer) {
            result.addAll(((EmbeddedPolicyApi) ((JavaExecutionContainer) executionContainer)
                    .getApiObject())
                    .provideId(policyInputScope));
        }

        return result;
    }

    /**
     * Collect all IDs for a policy, including nested constraints and policies.
     *
     * @param instance the policy
     * @return the collected IDs
     */
    public List<String> collectIds(final PolicyInstance instance) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < instance.getMappedElements().size(); i++) {
            if (instance.getMappedElements().get(i).getValue().lookup().getType()
                    == TypeTaxonomy.getInstance().lookup(Identifier.of("degree.PolicyInstance"))) {
                result.addAll(collectIds((PolicyInstance) instance
                        .getMappedElements().get(i).getValue().lookup()));
            } else {
                result.addAll(collectIds((ConstraintInstance) instance
                        .getMappedElements().get(i).getValue().lookup()));
            }
        }

        return result;
    }

}

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
package de.fhg.isst.oe270.degree.runtime.java.security.evaluation;

import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This scope is used for policy evaluation and encapsulates current and necessary data.
 */
public final class PermissionScope {

    /**
     * The used logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PermissionScope.class.getSimpleName());

    /**
     * The singleton instance.
     */
    private static PermissionScope singleton;

    /**
     * The required permissions.
     */
    private final Set<RequiredPermission> requiredPermissions;

    /**
     * Evaluation conditions.
     */
    private final Set<EvaluationCondition> evaluationConditions;

    /**
     * Additional data for evaluation.
     */
    private final HashMap<String, Object> evaluationData;

    /**
     * Evaluation permissions.
     */
    private final Set<RequiredPermission> evaluatedPermissions;

    /**
     * Construct the permission scope.
     */
    private PermissionScope() {
        requiredPermissions = new HashSet<>();
        evaluationConditions = new HashSet<>();
        evaluationData = new HashMap<>();
        evaluatedPermissions = new HashSet<>();
    }

    /**
     * Access the singleton instance of this class.
     *
     * @return the singleton instance
     */
    public static PermissionScope getInstance() {
        if (singleton == null) {
            synchronized (PermissionScope.class) {
                if (singleton == null) {
                    singleton = new PermissionScope();
                }
            }
        }
        return singleton;
    }

    /**
     * Reset the evaluation scope and ensure that this is only called by the sandbox.
     */
    public void reset() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (!stack[2].getClassName().equals(Sandbox.class.getCanonicalName())
                || !stack[2].getMethodName().equals("callActivity")) {
            LOGGER.info("'" + stack[2].getClassName() + "." + stack[2].getMethodName() + "' tried "
                    + "to reset the current permission scope. The call was not executed.");
            return;
        }

        requiredPermissions.clear();
        evaluationConditions.clear();
        evaluationData.clear();
        evaluatedPermissions.clear();
    }

    /**
     * Add a required permission to the current permission scope.
     *
     * @param requiredPermission The required permission which is needed for execution
     * @return true if the condition was added, false otherwise
     */
    public boolean addRequiredPermission(final RequiredPermission requiredPermission) {
        return requiredPermissions.add(requiredPermission);
    }

    /**
     * Add multiple required permissions to the current permission scope.
     *
     * @param permissions The required permission which is needed for execution
     * @return true if the condition was added, false otherwise
     */
    public boolean addRequiredPermissions(final Collection<RequiredPermission> permissions) {
        return requiredPermissions.addAll(permissions);
    }

    /**
     * Check if a given RequiredPermission is part of this scope.
     *
     * @param permission The searched permission
     * @return true if the given permission is contained in this scope, false otherwise
     */
    public boolean containsRequiredPermission(final RequiredPermission permission) {
        return requiredPermissions.contains(permission);
    }

    /**
     * Add a evaluation condition to the current permission scope.
     *
     * @param evaluationCondition the evaluation condition to add
     * @return true if the condition was added, false otherwise
     */
    public boolean addEvaluationCondition(final EvaluationCondition evaluationCondition) {
        return evaluationConditions.add(evaluationCondition);
    }

    /**
     * Add a collection of evaluation conditions to the current permission scope.
     *
     * @param conditions the conditions to add
     * @return true if the collection was added (partially), false otherwise
     */
    public boolean addEvaluationConditions(
            final Collection<EvaluationCondition> conditions) {
        return this.evaluationConditions.addAll(conditions);
    }

    /**
     * Provide additional (key, value) pair to current permission scope.
     *
     * @param key   Identifier of the object
     * @param value Value of the object
     * @return null if the key was not used before, the previously stored object otherwise
     */
    public Object addAdditionalPermissionData(final String key, final Object value) {
        return evaluationData.put(key, value);
    }

    /**
     * Add an evaluation permission.
     *
     * @param permission the permission to add
     * @return true if adding is successful, false otherwise
     */
    public boolean addEvaluatedPermission(final RequiredPermission permission) {
        return evaluatedPermissions.add(permission);
    }

    /**
     * Add required permissions to the scope.
     *
     * @param permissions the permissions to add
     * @return true if adding was successful, false otherwise
     */
    public boolean addEvaluatedPermissions(final Collection<RequiredPermission> permissions) {
        return evaluatedPermissions.addAll(permissions);
    }

    /**
     * Check if a permission has been evaluated.
     *
     * @param permission the permission to check
     * @return true if the permission was evaluated, false otherwise
     */
    public boolean isPermissionEvaluated(final RequiredPermission permission) {
        return evaluatedPermissions.contains(permission);
    }

    /**
     * Get the required permissions.
     *
     * @return the required permissions.
     */
    public Set<RequiredPermission> getRequiredPermissions() {
        return requiredPermissions;
    }

    /**
     * Get the evaluation conditions.
     *
     * @return the evaluation conditions
     */
    public Set<EvaluationCondition> getEvaluationConditions() {
        return evaluationConditions;
    }

    /**
     * Get the evaluation data.
     *
     * @return the evaluation data
     */
    public HashMap<String, Object> getEvaluationData() {
        return evaluationData;
    }

    /**
     * Get a specific entry of the evaluation data.
     *
     * @param key the key of the requested data
     * @return the data
     */
    public Object getEvaluationData(final String key) {
        return evaluationData.get(key);
    }

    /**
     * Get evaluated permissions.
     *
     * @return evaluated permissions
     */
    public Set<RequiredPermission> getEvaluatedPermissions() {
        return evaluatedPermissions;
    }

}

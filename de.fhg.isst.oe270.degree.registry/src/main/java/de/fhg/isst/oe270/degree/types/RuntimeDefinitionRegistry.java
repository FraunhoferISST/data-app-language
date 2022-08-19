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
package de.fhg.isst.oe270.degree.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fhg.isst.degree.types.gen.core.DegreeTypeSystem;
import de.fhg.isst.oe270.degree.registry.instances.execution.container.ExecutionContainer;
import de.fhg.isst.oe270.degree.registry.instances.execution.container.NOOPExecutionContainer;
import nukleus.common.UniqueMap;
import nukleus.core.Identifier;
import nukleus.core.Instance;
import nukleus.core.Scope;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.apache.commons.lang3.BooleanUtils.negate;

/**
 * Central registry of all activities and policies which are used in a D° application.
 */
public class RuntimeDefinitionRegistry {

    /**
     * Set of currently loaded yaml files.
     */
    private static Set<String> loadedFiles = new HashSet();

    /**
     * The {@link Scope} which contains all policies and activities.
     */
    private Scope scope;

    /**
     * Mapping of identifiers (for activities) to corresponding {@link ExecutionContainer}s.
     */
    private UniqueMap<Identifier, ExecutionContainer> containers = new UniqueMap<>();

    /**
     * Scope which contains all policies known to this registry.
     */
    private Scope policyCache = null;

    /**
     * Scope which contains all activities known to this registry.
     */
    private Scope activityCache = null;

    /**
     * Create a new registry.
     */
    public RuntimeDefinitionRegistry() {
        this.scope = new Scope(DegreeTypeSystem.get());
    }

    /**
     * Add the content from a JsonNode to the registry.
     *
     * @param node JsonNode containing a scope in JSON representation
     */
    public void load(final JsonNode node) {
        this.scope.deserialize(node);
        policyCache = null;
        activityCache = null;
    }

    /**
     * Determine if a specific file is loaded by the registry.
     *
     * @param fileName name of the checked file
     * @return true, if a file with given name is loaded, false otherwise
     */
    public static boolean isFileLoaded(final String fileName) {
        return loadedFiles.contains(fileName);
    }

    /**
     * Add a filename to the set of loaded files.
     *
     * @param fileName name of the file to add
     * @return true, if the file could be added, false otherwise
     */
    public static boolean addLoadedFile(final String fileName) {
        return loadedFiles.add(fileName);
    }

    /**
     * Serialize the whole registry as JSON object.
     *
     * @return The serialized content of the registry
     */
    public ObjectNode serialize() {
        return this.scope.serialize();
    }

    /**
     * Get the total number of items, which are contained in the registry.
     *
     * @return the current size of the registry
     */
    public int size() {
        return scope.size();
    }

    /**
     * Adds a new element to the registry.
     *
     * @param name   the identifier used for the added element
     * @param entity the added entity
     */
    public void create(final Identifier name, final Instance entity) {
        scope.create(name, entity);
    }

    /**
     * Check if an element with given identifier is contained in the registry.
     *
     * @param name the searched identifier
     * @return true, if the registry contains an element with given identifier, false otherwise
     */
    public boolean contains(final Identifier name) {
        return this.scope.contains(name);
    }

    /**
     * Retrieve an element with given identifier from the registry.
     *
     * @param name the identifier of the searched element
     * @return the found element
     * @throws nukleus.core.exception.ScopeException if the registry does not contain an
     *                                               element with given identifier
     */
    public Instance lookup(final Identifier name) {
        return this.scope.read(name);
    }

    /**
     * Retrieve an element with given identifier from the registry.
     *
     * @param name the identifier of the searched element
     * @return the found element
     * @throws nukleus.core.exception.ScopeException if the registry does not contain an
     *                                               element with given identifier
     * @see RuntimeDefinitionRegistry#lookup
     */
    public Instance read(final Identifier name) {
        return this.lookup(name);
    }

    /**
     * Adds a new element to the registry.
     *
     * @param ident    the identifier used for the added element
     * @param instance the added entity
     * @see RuntimeDefinitionRegistry#create
     */
    public void write(final Identifier ident, final Instance instance) {
        scope.create(ident, instance);
    }

    /**
     * Iterate over all elements in the registry and execute given action.
     *
     * @param action the action which will be executed
     */
    public void foreach(final Consumer<? super Instance> action) {
        scope.forEach(action);
    }

    /**
     * Retrieve a set, containing all elements in the registry.
     *
     * @return set of all elements in the registry
     */
    public Set<Instance> instanceSet() {
        return scope.instanceSet();
    }

    /**
     * Retrieve a set, containing all elements with a given supertype in the registry.
     *
     * @param name the supertype used for filtering
     * @return set of all elements with given supertype in the registry
     */
    public Set<Instance> instanceSet(final Identifier name) {
        return scope.instanceSet(scope.getTypeSystem().lookup(name));
    }

    /**
     * Retrieve a set, containing all elements with one of the given supertypes in the registry.
     *
     * @param supertype the supertypes used for filtering
     * @return set of all elements with at least one of the given supertypes in the registry
     */
    public Scope filter(final Identifier... supertype) {
        return scope.filter(supertype);
    }

    /**
     * Retrieve a subset of the registry which contains all
     * {@link de.fhg.isst.degree.types.gen.degree.Constraint}s,
     * {@link de.fhg.isst.degree.types.gen.degree.ConstraintInstance}s,
     * {@link de.fhg.isst.degree.types.gen.degree.Policy}s, and
     * {@link de.fhg.isst.degree.types.gen.degree.PolicyInstance}s.
     *
     * @return A filtered scope of the registry
     */
    public synchronized Scope policyRegistry() {
        if (policyCache != null) {
            return policyCache;
        }
        policyCache = filter(Identifier.of("degree.Constraint"),
                Identifier.of("degree.ConstraintInstance"),
                Identifier.of("degree.Policy"),
                Identifier.of("degree.PolicyInstance"));
        return policyCache;
    }

    /**
     * Retrieve a subset of the registry which contains all
     * {@link de.fhg.isst.degree.types.gen.degree.Activity}s and
     * {@link de.fhg.isst.degree.types.gen.degree.ActivityInstance}s.
     *
     * @return A filtered scope of the registry
     */
    public synchronized Scope activityRegistry() {
        if (activityCache != null) {
            return activityCache;
        }
        activityCache = filter(Identifier.of("degree.Activity"),
                Identifier.of("degree.ActivityInstance"));
        return activityCache;
    }

    /**
     * Register an execution container in the registry.
     * The used name needs to be identically to the name of the element the
     * execution container refers to.
     *
     * @param name      identifier of the element, the execution container is refering to
     * @param container the added execution container
     */
    public void registerContainer(final Identifier name, final ExecutionContainer container) {
        containers.put(name, container);
    }

    /**
     * Retrieve the execution container for an element with given identifier.
     *
     * @param name the name of the element of which the execution container is requested
     * @return the execution container of the element, {@link NOOPExecutionContainer}
     * if no container is stored in the registry
     */
    public ExecutionContainer retrieveContainer(final Identifier name) {
        if (negate(containers.containsKey(name))) {
            return new NOOPExecutionContainer();
        }
        return containers.get(name);
    }

    /**
     * Singleton instance of the registry.
     */
    private static RuntimeDefinitionRegistry singleton = null;

    /**
     * Accessor to the singleton of the RuntimeDefinitionRegistry.
     *
     * @return the singleton instance for this class
     */
    public static RuntimeDefinitionRegistry getInstance() {
        if (singleton == null) {
            synchronized (RuntimeDefinitionRegistry.class) {
                if (singleton == null) {
                    singleton = new RuntimeDefinitionRegistry();
                }
            }
        }
        return singleton;
    }

    /**
     * Clear the singleton instance.
     *
     * @return the singleton instance for this class, completely cleared.
     */
    public static RuntimeDefinitionRegistry resetInstance() {
        loadedFiles.clear();
        singleton = null;
        getInstance();
        return singleton;
    }

}

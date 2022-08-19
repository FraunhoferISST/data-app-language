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
package de.fhg.isst.oe270.degree.runtime.java.context;

import de.fhg.isst.oe270.degree.runtime.java.context.exception.ContextEntityResolveException;
import de.fhg.isst.oe270.degree.runtime.java.context.exception.UnknownContextEntityException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The execution context contains all root context modules and can be accessed in D° applications.
 */
public final class ExecutionContext {

    /**
     * The used logger.
     */
    private static final  Logger LOGGER =
            LoggerFactory.getLogger(ExecutionContext.class.getSimpleName());

    /**
     * Singleton instance for this class.
     */
    private static ExecutionContext instance = null;

    /**
     * Map with all known root context modules, identified by their names.
     */
    private final HashMap<String, ContextModule> contextModules = new HashMap<>();

    /**
     * The module mappings contain selections for modules where different alternatives are
     * available. For example there are different module versions for user information
     * (OsUserInformation & JWTUserInformation). Based on the type of data app, only one is
     * valid (e.g. JWTUserInformation for HttpDataApp). To automatically resolve these cases
     * the mappings are used. A mapping (UserInformation, JWTUserInformation) redirects all
     * calls to user information to the correct instance. Nevertheless all available possibilities
     * are available in the execution context, but may contain invalid information.
     */
    private final HashMap<String, String> moduleMappings = new HashMap<>();

    /**
     * During construction all root context modules are identified and loaded.
     */
    private ExecutionContext() {
        findContextModules();
        LOGGER.info("Finished initialization. Loaded " + contextModules.size()
                + " context modules.");

        LOGGER.info("Loaded context modules:");
        for (String moduleName : contextModules.keySet()) {
            LOGGER.info("-> " + moduleName);
        }
    }

    /**
     * Get the singleton instance of this class.
     *
     * @return the singleton instance of this class
     */
    public static ExecutionContext getInstance() {
        if (instance == null) {
            synchronized (ExecutionContext.class) {
                if (instance == null) {
                    instance = new ExecutionContext();
                }
            }
        }
        return instance;
    }

    /**
     * Get a context module by its name.
     *
     * @param moduleName name of the wanted module
     * @return the context module, identified by given name
     * @throws UnknownContextEntityException if the context module is not known
     *                                       (as root context module)
     */
    public ContextModule getModule(final String moduleName) {
        if (contextModules.containsKey(moduleName)) {
            return contextModules.get(moduleName);
        } else if (moduleMappings.containsKey(moduleName)) {
            return contextModules.get(moduleMappings.get(moduleName));
        } else {
            throw new UnknownContextEntityException("Unknown context module '"
                    + moduleName + "' requested.");
        }
    }

    /**
     * Resolves a name of form 'a.b....c' in the current execution context.
     *
     * @param name unique identifier of object which will be resolved
     * @return the resolved object, may be a value or a context entity
     * @throws ContextEntityResolveException if the resolving failed
     */
    public Object resolve(final String name) {
        if (name == null || name.isEmpty()) {
            throw new ContextEntityResolveException(
                    "Tried to resolve empty/null name in execution context.");
        }
        ArrayList<String> names = new ArrayList<>(Arrays.asList(name.split("\\.")));

        // resolve mappings
        for (String curName : names) {
            if (moduleMappings.containsKey(curName)) {
                names.set(names.indexOf(curName), moduleMappings.get(curName));
            }
        }

        String initialName = names.remove(0);
        if (!contextModules.containsKey(initialName)) {
            throw new ContextEntityResolveException(
                    "Tried to resolve unknown context module with name '"
                            + initialName + "'.");
        } else {
            return contextModules.get(initialName).resolve(names);
        }
    }

    /**
     * Check if a specific element is contained in the execution context.
     * This method supports fully qualified names.
     *
     * @param name the element to check
     * @return true, if the element is part of the execution context, false otherwise
     */
    public boolean contains(final String name) {
        try {
            this.resolve(name);
            return true;
        } catch (ContextEntityResolveException e) {
            return false;
        }
    }

    /**
     * Add a custom mapping for a context module.
     *
     * @param key   the custom mapped name of the module
     * @param value the original name of the context module
     */
    public void changeMappingEntry(final String key, final String value) {
        moduleMappings.put(key, value);
    }

    /**
     * Creates a basic mapping for context module alternatives.
     * This mapping does not provide the best configuration for a given
     * use case but ensures that all "expected" keys are available and can be resolved.
     */
    private void createDefaultMappings() {
        moduleMappings.clear();
        moduleMappings.put("UserInformation", "OsUserInformation");
    }

    /**
     * Load all context modules from classpath.
     * A context module is identified by its superclass ContextModule.
     */
    private void findContextModules() {
        ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages("*")
                .scan();

        // the fully qualified name as string is used here instead of *.getClass().getName()
        // because the API documentation states problems with different class loaders that way

        for (Class<?> aClass : scanResult.getClassesWithAnnotation(
                "de.fhg.isst.oe270.degree.runtime.java.context.RootContextModule")
                .loadClasses()) {
            Class<? extends ContextModule> clazz = (Class<? extends ContextModule>) aClass;
            try {
                // create an instance of the module
                ContextModule contextModule = clazz.getConstructor().newInstance();
                // store the module in module map
                contextModules.put(contextModule.getEntityName(), contextModule);
            } catch (InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException
                    | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
    }

}

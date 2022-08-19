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
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration;
import nukleus.core.Identifier;
import nukleus.core.Instance;
import nukleus.core.Nukleus;
import nukleus.core.TypeSystem;
import nukleus.core.custom.DegreeCustomization;
import org.apache.commons.lang3.Validate;

import java.util.HashSet;
import java.util.Set;

/**
 * Copy of Nukleus singleton with changed default types.
 *
 * @see nukleus.core.Nukleus
 */
public class TypeTaxonomy extends TypeSystem {

    /**
     * Set of currently loaded yaml files.
     */
    private static Set<String> loadedFiles = new HashSet();

    /**
     * Default filename of the default/core types extension.
     * <p>
     * NOTE this is currently not used because the core types are loaded differently.
     */
    public static final String DEFAULT_TYPES = Configuration.CORE_FILE_NAME
            + Configuration.TYPES_IDENTIFIER + "."
            + Configuration.SUBSYSTEM_FILE_EXTENSION;

    /**
     * Currently used filename for default/core types.
     * This can differ from {@link TypeTaxonomy#DEFAULT_TYPES}.
     */
    private static String defaultTypes = DEFAULT_TYPES + "";

    /**
     * Determine if a specific file is loaded by the taxonomy.
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
     * Change the location of default/core types for the taxonomy.
     * Note: This method can only be used before the singleton instance is created.
     *
     * @param path the location of the default/core types
     */
    public static void setDefaultTypes(final String path) {
        Validate.isTrue(singleton == null, "Use setDefaultType() before first access!");
        Validate.isTrue(TypeTaxonomy.class.getClassLoader().getResource(defaultTypes) != null,
                "Default types not found: " + path);
        defaultTypes = path;
    }

    /**
     * Singleton instance for this class.
     */
    private static TypeTaxonomy singleton = null;

    /**
     * Accessor to the singleton of the TypeTaxonomy.
     *
     * @return the singleton instance for this class
     */
    public static TypeTaxonomy getInstance() {
        if (singleton == null) {
            synchronized (TypeTaxonomy.class) {
                if (singleton == null) {
                    Nukleus.custom = new DegreeCustomization();
                    singleton = new TypeTaxonomy();
                    loadedFiles.add("degree.types.yaml");
                }
            }
        }
        return singleton;
    }

    /**
     * Create a new and empty TypeTaxonomy instance and return it.
     * CAUTION: These instances should not be used for global tasks.
     * Use the singleton, provided by {@link #getInstance()}.
     *
     * @return A TypeTaxonomy instance which can be used for temporary tasks.
     */
    public static TypeTaxonomy createTempInstance() {
        return new TypeTaxonomy();
    }

    /**
     * Clear the singleton instance.
     *
     * @return the singleton instance for this class, completely cleared.
     */
    public static TypeTaxonomy resetInstance() {
        loadedFiles.clear();
        singleton = null;
        getInstance();
        return singleton;
    }

    /**
     * Create a new instance from an element in the taxonomy, identified by the given identifier.
     *
     * @param identifier the identifier of the element in the taxonomy that will be created
     * @return the newly created instance
     */
    public Instance create(final Identifier identifier) {
        return super.newInstance(identifier);
    }

    /**
     * Create a new instance from a given JsonNode.
     *
     * @param json JsonNode that will be used to create the instance.
     * @return the newly created instance, representing the given JsonNode
     */
    public Instance create(final JsonNode json) {
        return super.newInstance(json);
    }

    /**
     * Get the total number of items, which are contained in the taxonomy.
     *
     * @return the current size of the taxonomy
     */
    public int size() {
        return listTypes().size();
    }

}

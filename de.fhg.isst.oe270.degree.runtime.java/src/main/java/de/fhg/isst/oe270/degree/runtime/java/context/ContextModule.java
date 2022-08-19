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

import com.thoughtworks.xstream.XStream;
import de.fhg.isst.oe270.degree.runtime.java.context.exception.ContextEntityResolveException;
import de.fhg.isst.oe270.degree.runtime.java.context.exception.UnknownContextEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * A context module in D° is a context entity which contains an arbitrary amount of other
 * context entities.
 */
public abstract class ContextModule implements ContextEntity {

    /**
     * Value for unknown values.
     */
    public static final String NO_VALUE = "UNKNOWN";

    /**
     * The used logger.
     */
    protected static final Logger LOGGER =
            LoggerFactory.getLogger(ContextModule.class.getSimpleName());

    /**
     * The used tracer logger.
     */
    protected static final Logger TRACER = LoggerFactory.getLogger("tracer");
    /**
     * The xStream instance used by this module for de-/serialization.
     */
    private static final XStream X_STREAM = new XStream();
    /**
     * The name of this module.
     */
    private final String moduleName;
    /**
     * The parent of this context entity.
     */
    private ContextEntity parent = null;

    /**
     * Flag if the flag for this module has been set.
     */
    private boolean parentSet = false;
    /**
     * Map containing all context entities and their names, which are part of this module.
     */
    private final HashMap<String, ContextEntity> contextEntities = new HashMap<>();

    /**
     * Create a context module with given name.
     *
     * @param name the name for the context module
     */
    public ContextModule(final String name) {
        this.moduleName = name;

        init();
    }

    @Override
    public final String getEntityName() {
        return moduleName;
    }

    @Override
    public final String getFullyQualifiedEntityName() {
        if (parent == null) {
            return moduleName;
        } else {
            return parent.getFullyQualifiedEntityName() + "." + this.moduleName;
        }
    }

    @Override
    public final Object resolve(final List<String> names) {
        // if there are no more names left for resolving this instance is returned
        if (names.size() == 0) {
            return this;
        } else {
            if (contextEntities.containsKey(names.get(0))) {
                return contextEntities.get(names.remove(0)).resolve(names);
            } else {
                TRACER.error("Tried to resolve unknown entity name '" + names.get(0) + "' in"
                        + " module '" + moduleName + "'.");
                throw new ContextEntityResolveException("Tried to resolve unknown entity name '"
                        + names.get(0) + "' in" + " module '" + moduleName + "'.");
            }
        }
    }

    /**
     * Initialize the module.
     */
    public void init() {
        X_STREAM.allowTypesByWildcard(new String[]{
                "de.fhg.isst.oe270.degree.runtime.java.context.**"
        });
        if (!load(createDefaultContext())) {
            // if loading failed the default configuration is loaded
            // save default configuration
            save();
        }
    }

    /**
     * Try to load values for this module from a file.
     *
     * @param defaultConfiguration map containing the default values for this module
     * @return true if loading was successful, false otherwise
     */
    public boolean load(final HashMap<String, ContextEntity> defaultConfiguration) {
        FileInputStream fileInputStream = null;
        HashMap<String, ContextEntity> loadedConfiguration;

        // clear current content
        contextEntities.clear();
        try {
            // try to regular load the persisted version for this module
            fileInputStream = new FileInputStream(System.getProperty("user.dir")
                    + File.separator + "de/fhg/isst/oe270/degree/runtime/java/context"
                    + File.separator + moduleName + ".xml");
            loadedConfiguration =
                    (HashMap<String, ContextEntity>) X_STREAM.fromXML(fileInputStream);
        } catch (FileNotFoundException e) {
            warn("Could not find saved data for context module '" + moduleName
                    + "'. Going to use default " + "configuration.");
            // if there is no persisted version available the default configuration will be used
            contextEntities.putAll(defaultConfiguration);
            buildParentHierarchy();

            return false;
        } finally {
            // close the input stream
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                error("Could not close input stream for saved data of context module '"
                        + moduleName + "'.");
            }
        }
        // check if the values loaded from file match to the expected values
        // in the default configuration
        for (String expectedKey : defaultConfiguration.keySet()) {
            if (!loadedConfiguration.containsKey(expectedKey)) {
                warn("The loaded configuration does not contain the key '" + expectedKey
                        + "'. Going to use default configuration");
                contextEntities.putAll(defaultConfiguration);

                return false;
            }
        }
        if (loadedConfiguration.size() != defaultConfiguration.size()) {
            info("Cardinality of loaded configuration (" + loadedConfiguration.size()
                    + ") differs from " + "cardinality of default configuration ("
                    + defaultConfiguration.size() + ").");
        }
        info("Successfully loaded data for module '" + moduleName + "'.");
        contextEntities.putAll(loadedConfiguration);

        buildParentHierarchy();

        return true;
    }

    /**
     * Recursively create correct parent/child releations for all contained entities.
     */
    public void buildParentHierarchy() {
        for (ContextEntity currentElement : contextEntities.values()) {
            currentElement.setParent(this);
            if (currentElement instanceof ContextModule) {
                ((ContextModule) currentElement).buildParentHierarchy();
            }
        }
    }

    /**
     * Saves the whole content of this context module to a file.
     */
    public void save() {
        FileOutputStream fileOutputStream = null;
        try {
            File targetFile = new File(System.getProperty("user.dir")
                    + File.separator + "de/fhg/isst/oe270/degree/runtime/java/context"
                    + File.separator + moduleName + ".xml");

            // ensure folders and files are available
            targetFile.getParentFile().mkdirs();
            targetFile.createNewFile();

            fileOutputStream = new FileOutputStream(targetFile, false);
            X_STREAM.toXML(contextEntities, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Propagates changes in entities to parent and persists the new data.
     */
    public void persistChange() {
        if (parent != null) {
            ((ContextModule) parent).persistChange();
        } else {
            save();
        }
    }

    /**
     * In case no persistent version of this context module is available,
     * a default configuration will be created.
     *
     * @return default configuration as HashMap<String, ContextEntity>
     */
    public abstract HashMap<String, ContextEntity> createDefaultContext();

    /**
     * @param key lookup key
     * @return context entity which is stored inside this class and identified by given key
     */
    public ContextEntity getContextEntity(final String key) {
        if (contextEntities.containsKey(key)) {
            return contextEntities.get(key);
        } else {
            throw new UnknownContextEntityException("Unknown context entity '"
                    + moduleName + "' requested.");
        }
    }

    /**
     * Get a map of all entities and their names, which are contained in the module.
     *
     * @return map with <name, entity> pairs, with entries for every entity in this module
     */
    public HashMap<String, ContextEntity> getAllContextEntities() {
        return contextEntities;
    }

    /**
     * @param key   unique key of the new entity
     * @param value context entity which will be stored
     */
    public void addContextEntity(final String key, final ContextEntity value) {
        if (contextEntities.containsKey(key)) {
            warn("Tried to add entity '" + key
                    + "' with a name which is already in use to module '"
                    + moduleName + "'.");
        } else {
            contextEntities.put(key, value);
            value.setParent(this);
            info("Added new Context Entity '" + key + "' of type "
                    + value.getClass().getSimpleName() + ".");
            save();
        }
    }

    /**
     * This function allows to remove entities from this context module. The action will be logged.
     *
     * @param key unique key of the entity to remove
     */
    public void removeContextEntity(final String key) {
        if (contextEntities.containsKey(key)) {
            contextEntities.remove(key);
            warn("Removed entity '" + key + "' in module '" + moduleName + "'.");
        } else {
            warn("Tried to remove entity '" + key
                    + "' with a name which is not known to module '"
                    + moduleName + "'.");
        }
    }

    /**
     * Allows to set the parent of this entity a single time.
     *
     * @param contextEntity the parent of this entity
     */
    @Override
    public void setParent(final ContextEntity contextEntity) {
        if (contextEntity == null) {
            return;
        }
        if (parentSet) {
            warn("Tried to change existing value for parent entity in module '"
                    + moduleName + "'. Going to " + "ignore it.");

            return;
        } else {
            this.parent = contextEntity;
            this.parentSet = true;
        }
    }

    /**
     * Log an info message.
     *
     * @param msg the message to log
     */
    protected void info(final String msg) {
        TRACER.info(msg);
        LOGGER.info(msg);
    }

    /**
     * Log a warn message.
     *
     * @param msg the message to log
     */
    protected void warn(final String msg) {
        TRACER.warn(msg);
        LOGGER.warn(msg);
    }

    /**
     * Log an error message.
     *
     * @param msg the message to log
     */
    protected void error(final String msg) {
        TRACER.error(msg);
        LOGGER.error(msg);
    }

}

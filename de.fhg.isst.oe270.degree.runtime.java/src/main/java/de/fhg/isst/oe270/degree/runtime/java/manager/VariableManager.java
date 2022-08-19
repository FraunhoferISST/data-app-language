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
package de.fhg.isst.oe270.degree.runtime.java.manager;

import nukleus.core.Instance;

import java.util.HashMap;
import java.util.UUID;

/**
 * The variable manager is used to manage variables inside D° applications and supports
 * scopes.
 */
public class VariableManager {

    /**
     * The parent variable manager of this manager.
     */
    private final VariableManager parent;

    /**
     * Container for all managed variables.
     */
    private final HashMap<UUID, Instance> variables = new HashMap<UUID, Instance>();

    /**
     * Create the variable manager with given parent.
     *
     * @param par the parent of the constructed variable manager
     */
    public VariableManager(final VariableManager par) {
        this.parent = par;
    }

    /**
     * Creates a new unique identifier for a variable and mark the identifier as used.
     *
     * @return the UUID of the new variable
     */
    public UUID createVariable() {
        UUID result = UUID.randomUUID();
        while (this.variables.containsKey(result)) {
            result = UUID.randomUUID();
        }

        this.variables.put(result, null);

        return result;
    }

    /**
     * Tries to mark an given identifier as used.
     *
     * @param uuid the uuid which should be reserved
     * @return null if the given UUID is already in use, the UUID itself otherwise
     */
    public UUID registerVariable(final UUID uuid) {
        if (this.variables.containsKey(uuid)) {
            return null;
        }

        this.variables.put(uuid, null);

        return uuid;
    }

    /**
     * Initialize a reserved variable with given name. Can only be used with
     * uninitialized variables.
     *
     * @param uuid     the identifier of the variable
     * @param instance the value of the variable
     * @return null if the identifier is not registered or the variable already
     * has a value, the UUID itself otherwise
     */
    public UUID initializeVariable(final UUID uuid, final Instance instance) {
        if (!this.variables.containsKey(uuid) || this.variables.get(uuid) != null) {
            if (this.parent == null) {
                return null;
            } else {
                this.parent.initializeVariable(uuid, instance);
            }
        } else {
            this.variables.put(uuid, instance);
        }

        return uuid;
    }

    /**
     * Get the value for a variable. May traverse parent variable manager until
     * there is a match or no more parents.
     *
     * @param uuid the identifier of the variable
     * @return the value of the variable or null if the identifier is unknown
     */
    public Instance readVariable(final UUID uuid) {
        Instance result = this.variables.get(uuid);
        if (result == null) {
            if (this.parent == null) {
                return null;
            } else {
                return this.parent.readVariable(uuid);
            }
        } else {
            return result;
        }
    }

    /**
     * Updates a registered and initialized variable.
     *
     * @param uuid  the identifier of the variable
     * @param value the new value of the variable
     * @return null if the identifier is unknown or the variable has
     * not been initialized yet. The value of the variable otherwise
     */
    public Instance updateVariable(final UUID uuid, final Instance value) {
        if ((this.variables.containsKey(uuid) && !(this.variables.get(uuid) == null))) {
            return this.variables.put(uuid, value);
        } else {
            if (parent == null) {
                return null;
            } else {
                return parent.updateVariable(uuid, value);
            }
        }
    }

    /**
     * Deletes the variable with given identifier from this manager.
     *
     * @param uuid identifier of the variable
     * @return true if the variable was deleted, false otherwise
     */
    public boolean deleteVariable(final UUID uuid) {
        boolean success = false;
        boolean containsKey = this.variables.containsKey(uuid);
        if (containsKey) {
            this.variables.remove(uuid);
            success = true;
        } else if (parent != null) {
            parent.deleteVariable(uuid);
        }
        return success;
    }
}

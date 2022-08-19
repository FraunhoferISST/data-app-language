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
package de.fhg.isst.oe270.degree.runtime.java.context.entities;

import de.fhg.isst.oe270.degree.runtime.java.context.ContextEntity;
import de.fhg.isst.oe270.degree.runtime.java.context.exception.ContextEntityResolveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is a readable entity, which can store arbitrary values.
 */
public class ReadOnlyEntity implements ContextEntity {

    /**
     * The used tracer logger.
     */
    protected static final Logger TRACER = LoggerFactory.getLogger("tracer");
    /**
     * The used logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            ReadOnlyEntity.class.getSimpleName());
    /**
     * The name of the entity.
     */
    private final String entityName;

    /**
     * The value stored in the entity.
     */
    private Object value;

    /**
     * The parent of this entity.
     */
    private ContextEntity parent = null;

    /**
     * Flag indicating if the parent of this entity has been set.
     */
    private boolean parentSet = false;

    /**
     * Create the entity with given name and value.
     *
     * @param name the name of this entity
     * @param val  the value that will be used in this entity
     */
    public ReadOnlyEntity(final String name, final Object val) {
        this.entityName = name;
        this.value = val;
    }

    @Override
    public final String getEntityName() {
        return entityName;
    }

    @Override
    public final String getFullyQualifiedEntityName() {
        return parent.getFullyQualifiedEntityName() + "." + entityName;
    }

    @Override
    public final Object resolve(final List<String> names) {
        if (names.size() > 0) {
            throw new ContextEntityResolveException(
                    "There are still names to resolve at the end of resolve chain.");
        } else {
            return read();
        }
    }

    /**
     * Get the value of the entity.
     *
     * @return the value, stored in the entity
     */
    public Object read() {
        return value;
    }

    /**
     * Get the parent entity for this parent.
     *
     * @return the parent of this entity.
     */
    public ContextEntity getParent() {
        return parent;
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
            LOGGER.warn("Tried to change existing value for parent entity in entity '"
                    + entityName + "'. Going to " + "ignore it.");

            return;
        } else {
            this.parent = contextEntity;
            this.parentSet = true;
        }
    }

    /**
     * Set the value of this entity.
     *
     * @param newVal the new value
     */
    protected void setValue(final Object newVal) {
        this.value = newVal;
    }

}

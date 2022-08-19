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

import de.fhg.isst.oe270.degree.runtime.java.context.ContextModule;

/**
 * This is a readable and writable entity, which can store arbitrary values.
 */
public class ReadWriteEntity extends ReadOnlyEntity {

    /**
     * Create the entity with given name and start value.
     *
     * @param entityName the name of the entity
     * @param value      the start value of the entity
     */
    public ReadWriteEntity(final String entityName, final Object value) {
        super(entityName, value);
    }

    /**
     * Set the value of this entity.
     *
     * @param value the new value
     */
    public void write(final Object value) {
        TRACER.info("Changed value of ReadWriteEntity '"
                + getFullyQualifiedEntityName() + "'. Old value: ''"
                + read() + "'; new value: '" + value + "'");
        setValue(value);
        ((ContextModule) getParent()).persistChange();
    }

}

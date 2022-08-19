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
 * This is a boolean entity which can be switched.
 */
public final class SwitchEntity extends ReadOnlyEntity {

    /**
     * Create the entity with given name and start value.
     *
     * @param entityName the name of the entity
     * @param value      the start value of the entity
     */
    public SwitchEntity(final String entityName, final boolean value) {
        super(entityName, value);
    }

    /**
     * Switch the value of this entity.
     */
    public void toggle() {
        TRACER.info("Changed value of SwitchEntity '" + getFullyQualifiedEntityName()
                + "'. Old value: ''" + read() + "'; new value: '" + !((boolean) read()) + "'");
        setValue(!((boolean) read()));
        ((ContextModule) getParent()).persistChange();
    }

}

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

import java.util.List;

/**
 * A context entity is an arbitrary datum of metadata or a container for metadata
 * that is available in D° applications.
 */
public interface ContextEntity {

    /**
     * Get the name of this entity.
     *
     * @return the name of this entity
     */
    String getEntityName();

    /**
     * Get the fully qualified name of this entity.
     * This respects the actual placement of the entity in the {@link ExecutionContext}.
     *
     * @return the fully qualified name of this entity
     */
    String getFullyQualifiedEntityName();

    /**
     * This function allows to recursively resolve an unique identifier for nested context entities.
     * E.g. appModule.userModule.userName
     *
     * @param names Array of names which need to be resolved. E.g. [appModule, userModule, userName]
     * @return the resolved object
     */
    Object resolve(List<String> names);

    /**
     * Set the parent for this entity.
     *
     * @param contextEntity the new parent for this entity
     */
    void setParent(ContextEntity contextEntity);

}

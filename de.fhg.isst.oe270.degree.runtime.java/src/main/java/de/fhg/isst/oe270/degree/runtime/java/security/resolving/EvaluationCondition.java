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
package de.fhg.isst.oe270.degree.runtime.java.security.resolving;

import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.PermissionMatchingStrategy;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Each policy attached to the current activity can generate EvaluationCondition objects, after the
 * DegreeSecurityManager generated RequiredPermission objects.
 * <p>
 * These objects can either be allowing or forbidding.
 *
 * @see de.fhg.isst.oe270.degree.runtime.java.security.manager.DegreeSecurityManager
 * @see RequiredPermission
 */
@EqualsAndHashCode
@Getter
public final class EvaluationCondition {

    /**
     * The D° permission type of this condition.
     */
    private final DegreePermissionType category;

    /**
     * The attribute of the permission. Can be an empty string.
     */
    private final String attribute;

    /**
     * The matching strategy for this condition.
     */
    private final PermissionMatchingStrategy matchingStrategy;

    /**
     * Flag indicating if this condition is a allowing or forbidding.
     */
    private final boolean forbid;

    /**
     * Create an evaluation condition.
     *
     * @param cat        the category of the constructed condition
     * @param attr       the attribute for the condition
     * @param strategy   matching strategy
     * @param forbidding flag, if the condition is forbidding
     */
    public EvaluationCondition(
            final DegreePermissionType cat,
            final String attr,
            final PermissionMatchingStrategy strategy,
            final boolean forbidding) {
        this.category = cat;
        this.attribute = attr;
        this.matchingStrategy = strategy;
        this.forbid = forbidding;
    }

}

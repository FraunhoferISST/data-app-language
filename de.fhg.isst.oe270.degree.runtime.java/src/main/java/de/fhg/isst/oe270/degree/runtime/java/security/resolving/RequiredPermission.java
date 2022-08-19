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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The DegreeSecurityManager generates a RequiredPermission for each interception which contains
 * a supported permission time.
 *
 * @see de.fhg.isst.oe270.degree.runtime.java.security.manager.DegreeSecurityManager
 * @see EvaluationCondition
 */
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
public class RequiredPermission {

    /**
     * The permission type.
     */
    private final DegreePermissionType category;

    /**
     * The attribute for the permission.
     */
    private final String attribute;

    /**
     * Create a pretty string representation for this permission.
     *
     * @return formatted string representation of this permission
     */
    public String toPrettyString() {
        return "Category: " + category.toString() + "; Attribute: " + attribute;
    }

}

/**
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
package de.fhg.isst.oe270.degree.core.policies.wildcard

import de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.PermissionMatchingStrategy

@PolicyAnnotation("GrantAll")
class GrantAll : EmbeddedPolicyApi {

    override fun acceptPrecondition(policyInput: PolicyInputScope): Boolean {
        return true
    }

    override fun evaluateSecurityManagerIntervention(input: PolicyInputScope): Collection<EvaluationCondition> {
        return listOf(
            EvaluationCondition(
                DegreePermissionType.WILDCARD,
                "",
                PermissionMatchingStrategy.EXACT_MATCH,
                false
            )
        )
    }

    override fun acceptPostcondition(input: PolicyInputScope): Boolean {
        return true
    }

}
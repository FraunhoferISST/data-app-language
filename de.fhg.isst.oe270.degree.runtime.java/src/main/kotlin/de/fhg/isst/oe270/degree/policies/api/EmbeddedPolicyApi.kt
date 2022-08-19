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
package de.fhg.isst.oe270.degree.policies.api

import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.registry.instances.api.DegreeJavaApiImplementation
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition

/**
 * Uniform API which all Policies and Constraints must provide for functionality.
 */
interface EmbeddedPolicyApi : DegreeJavaApiImplementation {

    /**
     * Execute the precondition logic of this policy.
     *
     * @param policyInput scope which is used for the execution
     * @return true if the precondition is fulfilled, false otherwise
     */
    fun acceptPrecondition(policyInput: PolicyInputScope): Boolean

    /**
     * Execute the precondition logic of this policy.
     *
     * @param policyInput scope which is used for the execution
     * @return collection of evaluation conditions
     */
    fun evaluateSecurityManagerIntervention(input: PolicyInputScope)
            : Collection<EvaluationCondition>

    /**
     * Execute the postcondition logic of this policy.
     *
     * @param policyInput scope which is used for the execution
     * @return true if the postcondition is fulfilled, false otherwise
     */
    fun acceptPostcondition(input: PolicyInputScope): Boolean

    /**
     * Provide custom IDs which are used for mapping.
     *
     * @param input the input scope may be used to derive IDs
     * @return list of custom mapping IDs
     */
    fun provideId(input: PolicyInputScope): List<String> {
        return emptyList()
    }

}

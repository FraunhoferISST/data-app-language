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
package de.fhg.isst.oe270.degree.core.policies.authorization.jwt

import de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadOnlyEntity
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition
import org.slf4j.LoggerFactory

@PolicyAnnotation("DenyRoleJwt")
class DenyUserroleJwt : EmbeddedPolicyApi {

    val logger = LoggerFactory.getLogger("DenyRoleJwt")!!

    override fun acceptPrecondition(policyInput : PolicyInputScope): Boolean {
        val role : String
        try {
            role = policyInput.get("role")!!.read()
        } catch (e : Exception) {
            logger.error("Missing input.")
            return false
        }

        if (policyInput.get("role")!!.type.identifier.toString() != "core.Userrole") {
            logger.error("Input type does not match the expected type.")
            return false
        }

        val currentRoles = ((ExecutionContext.getInstance().getModule("JWTUserInformation")
                .getContextEntity("userroles") as ReadOnlyEntity).read() as String).split(",")

        if (currentRoles.contains(role)) {
            logger.error("Validation failed. The JWT role '$role' is forbidden but is contained in" +
                    "the current roles '${currentRoles.joinToString(", ")}'.")
            return false
        }

        return true
    }

    override fun evaluateSecurityManagerIntervention(input: PolicyInputScope): Collection<EvaluationCondition> {
        return ArrayList<EvaluationCondition>()
    }

    override fun acceptPostcondition(input: PolicyInputScope): Boolean {
        return true
    }

}
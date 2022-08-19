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
package de.fhg.isst.oe270.degree.core.policies.authorization

import de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadOnlyEntity
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition
import org.slf4j.LoggerFactory

@PolicyAnnotation("DenyUsername")
class DenyUsername : EmbeddedPolicyApi {

    val logger = LoggerFactory.getLogger("DenyUsername")!!

    override fun acceptPrecondition(policyInput : PolicyInputScope): Boolean {
        val username : String
        try {
            username = policyInput.get("username")!!.read()
        } catch (e : Exception) {
            logger.error("Missing input.")
            return false
        }

        if (policyInput.get("username")!!.type.identifier.toString() != "core.Username") {
            logger.error("Input type does not match the expected type.")
            return false
        }

        val currentUsername = (ExecutionContext.getInstance().getModule("UserInformation")
                .getContextEntity("username") as ReadOnlyEntity).read()

        if (username == currentUsername) {
            logger.error("Validation failed. Username '$username' is forbidden but " +
                    "the current username is '$currentUsername'.")
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
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
package de.fhg.isst.oe270.degree.core.policies.tags

import de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition
import org.slf4j.LoggerFactory

@PolicyAnnotation("DenyTag")
class DenyTag : EmbeddedPolicyApi {

    val logger = LoggerFactory.getLogger("DenyTag")!!

    override fun acceptPrecondition(policyInput : PolicyInputScope): Boolean {
        val tag : String
        try {
            tag = policyInput.get("tag")!!.read()
        } catch (e : Exception) {
            logger.error("Missing input value.")
            return false
        }

        if (policyInput.get("tag")!!.type.identifier.toString() != "core.Tag") {
            logger.error("Inputs do not have the expected types")
            return false
        }

        return if (ExecutionContext.getInstance().contains("TagsContextModule.$tag")) {
            logger.error("Data App is tagged with tag '$tag' which is forbidden for this Data App.")
            false
        } else {
            true
        }
    }

    override fun evaluateSecurityManagerIntervention(input: PolicyInputScope): Collection<EvaluationCondition> {
        return ArrayList<EvaluationCondition>()
    }

    override fun acceptPostcondition(input: PolicyInputScope): Boolean {
        return true
    }

}
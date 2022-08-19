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
package de.fhg.isst.oe270.degree.core.policies.text

import de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition
import nukleus.core.Instance
import nukleus.core.PrimitiveInstance
import org.slf4j.LoggerFactory

@PolicyAnnotation("MaxLength")
class MaxLength : EmbeddedPolicyApi {

    val logger = LoggerFactory.getLogger("MaxLength")!!

    override fun acceptPrecondition(policyInput : PolicyInputScope): Boolean {
        val maxLengthInstance : Instance
        val contentInstance : Instance
        try {
            maxLengthInstance = policyInput.get("maxLength")!!
            contentInstance = policyInput.get("content")!!
        } catch (e : Exception) {
            logger.error("Missing input value(s).")
            return false
        }

        if (maxLengthInstance.type.identifier.toString() != "core.UnsignedInt" ||
                contentInstance.type.identifier.toString() != "core.Text") {
            logger.error("Input type(s) are mismatching the required ones. (core.UnsignedInt, core.Text) was " +
                    "required but (${maxLengthInstance.type.identifier}, ${contentInstance.type.identifier.toString()}) " +
                    "was found.")
            return false
        }

        val maxLengthValue = (maxLengthInstance as PrimitiveInstance).read().toInt()
        val content = (contentInstance as PrimitiveInstance).read()

        return if (content.length <= (maxLengthValue)) {
            true
        } else {
            logger.error("Validation failed. Content length is ${content.length} and exceeds the maximum allowed value " +
                    "of ${(maxLengthValue)}.")
            false
        }
    }

    override fun evaluateSecurityManagerIntervention(input: PolicyInputScope): Collection<EvaluationCondition> {
        return ArrayList<EvaluationCondition>()
    }

    override fun acceptPostcondition(input: PolicyInputScope): Boolean {
        return true
    }

}
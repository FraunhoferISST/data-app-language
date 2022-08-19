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
package de.fhg.isst.oe270.degree.core.policies.date

import de.fhg.isst.oe270.degree.policies.annotations.PolicyAnnotation
import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@PolicyAnnotation("UseNotBeforeTimeStamp")
class UseNotBefore : EmbeddedPolicyApi {

    private val logger = LoggerFactory.getLogger("UseNotBefore")!!

    override fun acceptPrecondition(policyInput : PolicyInputScope): Boolean {
        val policyDate: LocalDateTime
        try {
            policyDate = DatePolicyUtils.convertLocalDateTimeToJava(policyInput, "timestamp")
        } catch (e: IllegalArgumentException) {
            logger.error("Could not parse date.", e)
            return false
        }
        val currentTime = LocalDateTime.now()

        if (currentTime.isBefore(policyDate)) {
            logger.error("Validation failed. Earliest allowed time is ${policyDate.format(DatePolicyUtils.dateFormatter)} but " +
                    "the current time is ${currentTime.format(DatePolicyUtils.dateFormatter)}.")
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
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
package de.fhg.isst.oe270.degree.core.policies.io.file

import de.fhg.isst.oe270.degree.policies.api.EmbeddedPolicyApi
import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.runtime.java.exceptions.security.DegreeForbiddenSecurityFeatureException
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.PermissionMatchingStrategy
import org.slf4j.LoggerFactory

abstract class FileConstraint: EmbeddedPolicyApi {

    val logger = LoggerFactory.getLogger("FileConstraint")!!

    abstract fun isForbidding() : Boolean

    abstract fun getPermissionType() : DegreePermissionType

    override fun acceptPrecondition(policyInput: PolicyInputScope): Boolean {
        return true
    }

    override fun evaluateSecurityManagerIntervention(input: PolicyInputScope): Collection<EvaluationCondition> {
        var matchingStrategy = PermissionMatchingStrategy.PATH_EXACT_MATCH
        val pathInstance = input.get("path")!!
        val matchingStrategyInstance = input.get("matchingStrategy")!!
        when (matchingStrategyInstance.read()) {
            "EXACT" -> matchingStrategy = PermissionMatchingStrategy.PATH_EXACT_MATCH
            "SUBDIR" -> matchingStrategy = PermissionMatchingStrategy.PATH_SUBDIR
        }
        if (pathInstance.type.identifier.toString() != "core.Path" ||
                matchingStrategyInstance.type.identifier.toString() != "core.PathMatchingStrategy") {
            logger.error("Input type does not match. Expected 'core.Path, core.PathMatchingStrategy' but found (${matchingStrategyInstance.type.identifier}, ${matchingStrategyInstance.type.identifier})")
            throw DegreeForbiddenSecurityFeatureException("Input type does not match. " +
                    "Expected 'core.PathMatchingStrategy' but found ${matchingStrategyInstance.type.identifier}")
        }
        val permission = EvaluationCondition(
                getPermissionType(),
                input.get("path")!!.read(),
                matchingStrategy,
                isForbidding()
        )

        return listOf(permission)
    }

    override fun acceptPostcondition(input: PolicyInputScope): Boolean {
        return true
    }
}
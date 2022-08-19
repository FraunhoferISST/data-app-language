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
import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox
import de.fhg.isst.oe270.degree.runtime.java.security.evaluation.PermissionScope
import de.fhg.isst.oe270.degree.runtime.java.security.functionality.modules.DegreeFileOperations
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.EvaluationCondition
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.DegreePermissionType
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.enums.PermissionMatchingStrategy
import nukleus.core.Instance
import org.slf4j.LoggerFactory

abstract class QuotaFile: EmbeddedPolicyApi {

    val logger = LoggerFactory.getLogger("QuotaReadFile")!!

    private var readBytes: Long = 0

    abstract fun getPermissionType() : DegreePermissionType

    abstract fun getByteOptionName() : String

    override fun acceptPrecondition(policyInput: PolicyInputScope): Boolean {
        // skip all non relevant permissions
        if (!isAffected()) return true

        val (quotaInstance: Instance, unitInstance: Instance, pathInstance: Instance) =
                try {
                    validateInputs(policyInput)
                } catch (e : DegreeForbiddenSecurityFeatureException) {
                    return false
                }
        val matchingStrategy = retrievePermissionMatchingStrategy(policyInput)
        // check if the call affects this constraint
        var matchFound = false
        Sandbox.getInstance().currentRequiredPermissions.forEach { permission ->
            if (!evaluatePaths(
                            pathInstance.read(),
                            PermissionMatchingStrategy.preparePath(
                                    permission.attribute
                            ),
                            matchingStrategy
                    )
            ) {
                matchFound = true
                return@forEach
            }
        }
        if (!matchFound) {
            return true
        }

        val availableQuota = ByteUnitUtils.toByte(
                quotaInstance.read().toLong(),
                unitInstance.read()
        )

        return if (readBytes < availableQuota) {
            true
        } else {
            logger.error("Validation failed. Granted quota is ${quotaInstance.read()}${unitInstance.read()} but " +
                    "${ByteUnitUtils.toBytePrefix(readBytes, unitInstance.read())}${unitInstance.read()}.")
            false
        }
    }

    override fun evaluateSecurityManagerIntervention(input: PolicyInputScope): Collection<EvaluationCondition> {
        // skip all non relevant permissions
        if (!isAffected()) {
            return listOf()
        }

        val (quotaInstance: Instance, unitInstance: Instance, pathInstance: Instance) = validateInputs(input)
        val matchingStrategy = retrievePermissionMatchingStrategy(input)

        val givenQuota = ByteUnitUtils.toByte(quotaInstance.read().toLong(), unitInstance.read())
        val quotaUsedByThisRequest = PermissionScope.getInstance().evaluationData[getByteOptionName()] as Long
        logger.info(
                "${"%.2f".format((readBytes.toFloat()/givenQuota) * 100)}% " +
                "(${ByteUnitUtils.toBytePrefix(readBytes, unitInstance.read())}${unitInstance.read()}/" +
                "${quotaInstance.read()}${unitInstance.read()}) " +
                "of the given quota for ${retrieveOperationString()} file/location ${pathInstance.read()} is consumed. " +
                "This request will consume another ${"%.2f".format((quotaUsedByThisRequest.toFloat()/givenQuota) * 100)}% " +
                "(${ByteUnitUtils.toBytePrefix(quotaUsedByThisRequest, unitInstance.read())}${unitInstance.read()}) of" +
                " the granted quota.")
        // check if the call affects this constraint
        if (!evaluatePaths(
                        pathInstance.read(),
                        PermissionMatchingStrategy.preparePath(
                                PermissionScope.getInstance().evaluationData[DegreeFileOperations.FILE_PATH] as String
                        ),
                        matchingStrategy
                        )
        ) {
            return listOf()
        }

        // we need to check if this action would exceeds the granted quota
        return if ((quotaUsedByThisRequest + readBytes) > givenQuota) {
            val exceedValue = quotaUsedByThisRequest + readBytes - givenQuota
            throw DegreeForbiddenSecurityFeatureException("The execution is aborted because a requested file operation " +
                    "operation exceeds the granted quota of ${quotaInstance.read()}${unitInstance.read()}" +
                    " by ${exceedValue}B.")
        } else {
            listOf(
                EvaluationCondition(
                    getPermissionType(),
                    input.get("path")!!.read(),
                    matchingStrategy,
                    false
                )
            )
        }
    }

    /**
     * The postcondition cannot fail since all validation is already performed.
     * But it is necessary that we adjust the value which indicated the used amount of the quota.
     */
    override fun acceptPostcondition(input: PolicyInputScope): Boolean {
        // skip all non relevant permissions
        if (!isAffected()) {
            return true
        }

        val (_, _, pathInstance: Instance) =
                try {
                    validateInputs(input)
                } catch (e : DegreeForbiddenSecurityFeatureException) {
                    return false
                }

        val matchingStrategy = retrievePermissionMatchingStrategy(input)
        // check if the call affects this constraint
        if (!evaluatePaths(
                        pathInstance.read(),
                        PermissionMatchingStrategy.preparePath(
                                PermissionScope.getInstance().evaluationData[DegreeFileOperations.FILE_PATH] as String
                        ),
                        matchingStrategy
                )
        ) {
            return true
        }

        readBytes +=  PermissionScope.getInstance().evaluationData[getByteOptionName()] as Long
        return true

    }

    private fun retrievePermissionMatchingStrategy(input: PolicyInputScope): PermissionMatchingStrategy {
        return when (input.get("matchingStrategy")!!.read()) {
            "EXACT" -> PermissionMatchingStrategy.PATH_EXACT_MATCH
            "SUBDIR" -> PermissionMatchingStrategy.PATH_SUBDIR
            else -> PermissionMatchingStrategy.PATH_EXACT_MATCH
        }
    }

    /**
     * Decides if the path of a policy matches the path of a real operation with given matching strategy.
     *
     * @param givenPath the path defined in the policy
     * @param realPath the file which is accessed
     * @param strategy the strategy that is used to match paths
     * @return true if the paths match, false otherwise
     */
    private fun evaluatePaths(
            givenPath : String,
            realPath : String,
            strategy : PermissionMatchingStrategy) : Boolean {
        val givenPathPrepared = PermissionMatchingStrategy.preparePath(givenPath)
        val realPathPrepared = PermissionMatchingStrategy.preparePath(realPath)
        // check for the special wildcard string
        if (givenPathPrepared.equals("<<ALL_FILES>>")) {
            return true
        }
        if (strategy.equals(PermissionMatchingStrategy.PATH_EXACT_MATCH)) {
            // exact matching
            return realPathPrepared == givenPathPrepared
        } else if (strategy == PermissionMatchingStrategy.PATH_SUBDIR) {
            return realPathPrepared.startsWith(givenPathPrepared)
        }

        return false
    }

    private fun validateInputs(input: PolicyInputScope): Triple<Instance, Instance, Instance> {
        val quotaInstance: Instance
        val unitInstance: Instance
        val pathInstance: Instance
        val strategyInstance: Instance
        try {
            quotaInstance = input.get("quota")!!
            unitInstance = input.get("unit")!!
            pathInstance = input.get("path")!!
            strategyInstance = input.get("matchingStrategy")!!
        } catch (e: Exception) {
            logger.error("Missing input value(s).")
            throw DegreeForbiddenSecurityFeatureException("Missing input value(s).")
        }

        if (quotaInstance.type.identifier.toString() != "core.UnsignedInt" ||
                unitInstance.type.identifier.toString() != "core.ByteUnit" ||
                pathInstance.type.identifier.toString() != "core.Path" ||
                strategyInstance.type.identifier.toString() != "core.PathMatchingStrategy") {
            logger.error(
                    "Input type(s) are mismatching the required ones. (core.UnsignedInt, core.ByteUnit, " +
                            "core.Path, core.PathMatchingStrategy) was required but (" +
                            "${quotaInstance.type.identifier}, ${unitInstance.type.identifier}, " +
                            "${pathInstance.type.identifier}, ${strategyInstance.type.identifier}) " +
                            "was found.")
            throw DegreeForbiddenSecurityFeatureException(
                    "Input type(s) are mismatching the required ones. (core.UnsignedInt, core.ByteUnit, " +
                            "core.Path, core.PathMatchingStrategy) was required but (" +
                            "${quotaInstance.type.identifier}, ${unitInstance.type.identifier}, " +
                            "${pathInstance.type.identifier}, ${strategyInstance.type.identifier}) " +
                            "was found.")
        }
        return Triple(quotaInstance, unitInstance, pathInstance)
    }

    /**
     * Decise if the constraint is affected by the current call.
     */
    private fun isAffected(): Boolean {
        var affected = false
        Sandbox.getInstance().currentRequiredPermissions.forEach { requiredPermission ->
            if (requiredPermission != null &&
                    requiredPermission.category == getPermissionType()) {
                affected = true
                return@forEach
            }
        }
        return affected
    }

    /**
     * Simple helper function to improve logging messages.
     */
    private fun retrieveOperationString() : String {
        return when (getByteOptionName()) {
            DegreeFileOperations.WRITTEN_BYTES -> "writing"
            DegreeFileOperations.READ_BYTES -> "reading"
            else -> "interacting with"
        }
    }

}
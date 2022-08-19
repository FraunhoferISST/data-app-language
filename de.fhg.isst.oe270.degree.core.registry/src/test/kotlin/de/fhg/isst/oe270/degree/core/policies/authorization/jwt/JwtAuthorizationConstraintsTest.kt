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

import de.fhg.isst.oe270.degree.policies.execution.PolicyInputScope
import de.fhg.isst.oe270.degree.parsing.configuration.Configuration

import de.fhg.isst.oe270.degree.runtime.java.context.ContextModule
import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadOnlyEntity
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import de.fhg.isst.oe270.degree.util.SubSystemUtils
import nukleus.core.Identifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtAuthorizationConstraintsTest {

    private val typeTaxonomy : TypeTaxonomy = TypeTaxonomy.getInstance()
    
    private val jwtModuleName = "JWTUserInformation"

    private val usernameA = "userA"

    private val usernameB = "userB"

    private val userroleA = "roleA"

    private val userroleB = "roleB"

    private val userrolesA = "roleC,roleD,roleA"

    private val userrolesB = "roleC,roleD,roleB"

    private val moduleName = "UserInformation"

    private val executionModule : ContextModule

    init {
        if (typeTaxonomy.size() == 0) {
            SubSystemUtils.updateSubSystems()
            typeTaxonomy.load(Paths.get(Configuration.CORE_TYPES_FILE_PATH))
        }

        provideUserInformationMapping(jwtModuleName)
        executionModule = ExecutionContext.getInstance().getModule(moduleName)
    }

    private fun provideUsername(name : String) {
        executionModule.addContextEntity("username", ReadOnlyEntity("username", name))
    }

    private fun provideUserroles(roles : String) {
        executionModule.addContextEntity("userroles", ReadOnlyEntity("userroles", roles))
    }

    private fun removeUsername() {
        executionModule.removeContextEntity("username")
    }

    private fun removeUserroles() {
        executionModule.removeContextEntity("userroles")
    }

    private fun provideUserInformationMapping(module : String) {
        ExecutionContext.getInstance().changeMappingEntry("UserInformation", module)
    }

    @Test
    fun `Test that constraint core_RequireUserroleJwt does reject missing input`() {
        val inputScope = PolicyInputScope()

        val constraint = RequireUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_RequireUserroleJwt does reject wrong input type`() {
        val role = typeTaxonomy.create(Identifier.of("core.Error"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userroleA)

        val constraint = RequireUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_RequireUserroleJwt does reject wrong role`() {
        val role = typeTaxonomy.create(Identifier.of("core.Userrole"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userroleB)

        val constraint = RequireUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_RequireUserroleJwt does reject wrong role from list`() {
        val role = typeTaxonomy.create(Identifier.of("core.Userrole"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userrolesB)

        val constraint = RequireUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_RequireUserroleJwt does accept correct role`() {
        val role = typeTaxonomy.create(Identifier.of("core.Userrole"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userroleA)

        val constraint = RequireUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertTrue(result)
    }

    @Test
    fun `Test that constraint core_RequireUserroleJwt does accept correct role from list`() {
        val role = typeTaxonomy.create(Identifier.of("core.Userrole"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userrolesA)

        val constraint = RequireUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertTrue(result)
    }

    @Test
    fun `Test that constraint core_RequireUsernameJwt does reject missing input`() {
        val inputScope = PolicyInputScope()

        val constraint = RequireUsernameJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_RequireUsernameJwt does reject wrong input type`() {
        val username = typeTaxonomy.create(Identifier.of("core.Error"))
        username.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("username", username)

        removeUsername()
        provideUsername(usernameA)

        val constraint = RequireUsernameJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_RequireUsernameJwt does reject wrong username`() {
        val username = typeTaxonomy.create(Identifier.of("core.Username"))
        username.write(usernameA)
        val inputScope = PolicyInputScope()
        inputScope.add("username", username)

        removeUsername()
        provideUsername(usernameB)

        val constraint = RequireUsernameJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_RequireUsernameJwt does accepts correct username`() {
        val username = typeTaxonomy.create(Identifier.of("core.Username"))
        username.write(usernameA)
        val inputScope = PolicyInputScope()
        inputScope.add("username", username)

        removeUsername()
        provideUsername(usernameA)

        val constraint = RequireUsernameJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertTrue(result)
    }

    @Test
    fun `Test that constraint core_DenyUserroleJwt does reject missing input`() {
        val inputScope = PolicyInputScope()

        val constraint = DenyUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_DenyUserroleJwt does reject wrong input type`() {
        val role = typeTaxonomy.create(Identifier.of("core.Error"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userroleA)

        val constraint = DenyUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_DenyUserroleJwt does reject matching role`() {
        val role = typeTaxonomy.create(Identifier.of("core.Userrole"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userroleA)

        val constraint = DenyUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_DenyUserroleJwt does reject matching role from list`() {
        val role = typeTaxonomy.create(Identifier.of("core.Userrole"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userrolesA)

        val constraint = DenyUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_DenyUserroleJwt does accepts non-matching role`() {
        val role = typeTaxonomy.create(Identifier.of("core.Userrole"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userroleB)

        val constraint = DenyUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertTrue(result)
    }

    @Test
    fun `Test that constraint core_DenyUserroleJwt does accepts non-matching role from list`() {
        val role = typeTaxonomy.create(Identifier.of("core.Userrole"))
        role.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("role", role)

        removeUserroles()
        provideUserroles(userrolesB)

        val constraint = DenyUserroleJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertTrue(result)
    }

    @Test
    fun `Test that constraint core_DenyUsernameJwt does reject missing input`() {
        val inputScope = PolicyInputScope()

        val constraint = DenyUsernameJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_DenyUsernameJwt does reject wrong input type`() {
        val username = typeTaxonomy.create(Identifier.of("core.Error"))
        username.write(userroleA)
        val inputScope = PolicyInputScope()
        inputScope.add("username", username)

        removeUsername()
        provideUsername(usernameA)

        val constraint = DenyUsernameJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

    @Test
    fun `Test that constraint core_DenyUsernameJwt does accept non-matching username`() {
        val username = typeTaxonomy.create(Identifier.of("core.Username"))
        username.write(usernameA)
        val inputScope = PolicyInputScope()
        inputScope.add("username", username)

        removeUsername()
        provideUsername(usernameB)

        val constraint = DenyUsernameJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertTrue(result)
    }

    @Test
    fun `Test that constraint core_DenyUsernameJwt rejects matching username`() {
        val username = typeTaxonomy.create(Identifier.of("core.Username"))
        username.write(usernameA)
        val inputScope = PolicyInputScope()
        inputScope.add("username", username)

        removeUsername()
        provideUsername(usernameA)

        val constraint = DenyUsernameJwt()
        val result = constraint.acceptPrecondition(inputScope)

        assertFalse(result)
    }

}
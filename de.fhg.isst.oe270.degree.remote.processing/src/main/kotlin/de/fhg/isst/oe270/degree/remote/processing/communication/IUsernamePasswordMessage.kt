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
package de.fhg.isst.oe270.degree.remote.processing.communication

interface IUsernamePasswordMessage: IMessage {

    val usernameKey: String
        get() = "username"

    val passwordKey: String
        get() = "password"

    fun getUsername(): String {
        return values[usernameKey] ?: ""
    }

    fun setUsername(username: String) {
        values[usernameKey] = username
    }

    fun getPassword(): String {
        return values[passwordKey] ?: ""
    }

    fun setPassword(password: String) {
        values[passwordKey] = password
    }

    override fun validateFieldValues() {
        val errors = mutableListOf<String>()
        try {
            super.validateFieldValues()
        } catch (e: Exception) {
            errors.add(e.message!!)
        }

        if (getUsername().isBlank()) {
            errors.add("Missing username.")
        }

        if (getPassword().isBlank()) {
            errors.add("Missing password.")
        }

        if (errors.isNotEmpty()) {
            throw RuntimeException(errors.joinToString(separator = "\n"))
        }
    }

}
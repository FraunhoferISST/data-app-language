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

import de.fhg.isst.oe270.degree.remote.processing.DataAppState
import java.util.*

interface IDataAppStateMessage: IMessage {

    val dataAppStateKey: String
        get() = "dataAppState"

    fun getDataAppState(): DataAppState {
        return DataAppState.valueOf(values[dataAppStateKey] ?: "")
    }

    fun setDataAppState(value: DataAppState) {
        values[dataAppStateKey] = value.toString()
    }

    override fun validateFieldValues() {
        val errors = mutableListOf<String>()
        try {
            super.validateFieldValues()
        } catch (e: Exception) {
            errors.add(e.message!!)
        }

        try {
            DataAppState.valueOf(getDataAppState().toString())
        } catch (e: Exception) {
            errors.add("DataAppState is not well-formed.")
        }

        if (errors.isNotEmpty()) {
            throw RuntimeException(errors.joinToString(separator = "\n"))
        }
    }

}
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

class Statuscode {

    val ioError = 1L shl 0
    val inputFormatError = 1L shl 1
    val outputFormatError = 1L shl 2
    val dataAppGitError = 1L shl 3
    val compileStateError = 1L shl 4
    val deployStateError = 1L shl 5
    val startStateError = 1L shl 6
    val stopStateError = 1L shl 7
    val dispatchStateError = 1L shl 8
    val updateStateError = 1L shl 9
    val deleteStateError = 1L shl 10

    private val messages = Array(64) {""}

    private var bitmask : Long = 0L

    private val appendedErrors = mutableListOf<String>()

    init {
        messages[0] = "An IO-Error occurred."
        messages[1] = "The input does not provide the required fields or the JSON is malformed."
        messages[2] = "An error occurred during the creation of the output message."
        messages[3] = "An error occurred during interaction with a Data App's git repository."
        messages[4] = "Data Apps can only be compiled if they are not running and code is available."
        messages[5] = "Data Apps can only be deployed if they are compiled and not running."
        messages[6] = "Data Apps can only be started if they are in DEPLOYED state."
        messages[7] = "Data Apps can only be stopped if they are in RUNNING state."
        messages[8] = "Messages can only be dispatched to Data Apps which are in RUNNING state."
        messages[9] = "A Data App cannot be updated if it is running/deleted or during compilation/deployment/termination."
        messages[10] = "A Data App cannot be deleted if it is running/deleted or during compilation/deployment/termination."
    }

    fun createMessage() : String {
        var message = ""
        // in case everything went right
        if (isSuccess()) {
            message = "Success."
        } else {
            // collect errors
            for (i in 0 until 64) {
                if ((bitmask ushr i) and 1L == 1L && messages[i].isNotBlank()) {
                    message += messages[i] + "\n"
                }
            }

            // append additional messages
            if (appendedErrors.isNotEmpty()) {
                message += "\nAdditional data:\n\n- " + appendedErrors.joinToString(separator = "\n- ")
            }
        }

        return message
    }

    fun getStatuscode() : Long {
        return bitmask
    }


    fun setBit(value : Long) {
        bitmask = bitmask or value
    }

    fun appendError(error : String) {
        appendedErrors.add(error)
    }

    fun isSuccess(): Boolean {
        return bitmask == 0L
    }

}
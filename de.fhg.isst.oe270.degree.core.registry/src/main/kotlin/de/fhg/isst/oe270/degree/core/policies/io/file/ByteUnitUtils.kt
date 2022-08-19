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

object ByteUnitUtils {

    fun toByte(count : Long, unit : String) : Long {
        return when(unit) {
            "B" -> count
            "kB" -> Math.multiplyExact(count, Math.pow(10.0, 3.0).toLong())
            "MB" -> Math.multiplyExact(count, Math.pow(10.0, 6.0).toLong())
            "GB" -> Math.multiplyExact(count, Math.pow(10.0, 9.0).toLong())
            "TB" -> Math.multiplyExact(count, Math.pow(10.0, 12.0).toLong())
            "PB" -> Math.multiplyExact(count, Math.pow(10.0, 15.0).toLong())
            "EB" -> Math.multiplyExact(count, Math.pow(10.0, 18.0).toLong())
            "KiB" -> Math.multiplyExact(count, Math.pow(2.0, 10.0).toLong())
            "MiB" -> Math.multiplyExact(count, Math.pow(2.0, 20.0).toLong())
            "GiB" -> Math.multiplyExact(count, Math.pow(2.0, 30.0).toLong())
            "TiB" -> Math.multiplyExact(count, Math.pow(2.0, 40.0).toLong())
            "PiB" -> Math.multiplyExact(count, Math.pow(2.0, 50.0).toLong())
            "EiB" -> Math.multiplyExact(count, Math.pow(2.0, 60.0).toLong())
            else -> throw RuntimeException("Unknown unit for bytes '$unit'.")
        }
    }

    fun toBytePrefix(count : Long, unit : String) : Double {
        return when(unit) {
            "B" -> count.toDouble()
            "kB" -> count / Math.pow(10.0, 3.0)
            "MB" -> count / Math.pow(10.0, 6.0)
            "GB" -> count / Math.pow(10.0, 9.0)
            "TB" -> count / Math.pow(10.0, 12.0)
            "PB" -> count / Math.pow(10.0, 15.0)
            "EB" -> count / Math.pow(10.0, 18.0)
            "KiB" -> count / Math.pow(2.0, 10.0)
            "MiB" -> count / Math.pow(2.0, 20.0)
            "GiB" -> count / Math.pow(2.0, 30.0)
            "TiB" -> count / Math.pow(2.0, 40.0)
            "PiB" -> count / Math.pow(2.0, 50.0)
            "EiB" -> count / Math.pow(2.0, 60.0)
            else -> throw RuntimeException("Unknown unit for bytes '$unit'.")
        }
    }

}
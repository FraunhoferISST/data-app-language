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
package de.fhg.isst.oe270.degree.parsing.types

/**
 * A point refers a specific character in a D° source file.
 *
 * @param line the line number of this position
 * @param column the column in line of this position
 */
data class Point(val line: Int, val column: Int) {

    /**
     * Check if this point is before another point.
     *
     * @param other the other point
     * @return true if this point is before the other, false otherwise
     */
    fun isBefore(other: Point) = line < other.line || (line == other.line && column < other.column)

    /**
     * Check if this point is after another point.
     *
     * @param other the other point
     * @return true if this point is after the other, false otherwise
     */
    fun isAfter(other: Point) = line > other.line || (line == other.line && column > other.column)

    override fun toString(): String {
        return "[$line,$column]"
    }
}

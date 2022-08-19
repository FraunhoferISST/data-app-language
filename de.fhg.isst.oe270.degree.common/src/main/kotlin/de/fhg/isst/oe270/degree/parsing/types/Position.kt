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
 * Position identifies a range in a D° source file, which is identified by a starting and an end point.
 *
 * @param start start point of this position
 * @param end end point of this position
 */
data class Position(val start: Point, val end: Point) {

    /**
     * Create a position from start and end column & line.
     *
     * @param startLine position's start line
     * @param startColumn position's start column
     * @param endLine position's end line
     * @param endColumn position's end column
     */
    constructor(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) : this(
        Point(startLine, startColumn),
        Point(endLine, endColumn)
    )

    /**
     * Check if this position is before another position.
     *
     * @param other the other position
     * @return true if this position is before the other, false otherwise
     */
    fun isBefore(other: Position) = end.isBefore(other.start)

    /**
     * Check if this position starts before another position.
     *
     * @param other the other position
     * @return true if this position starts before the other, false otherwise
     */
    fun startsBefore(other: Position) = start.isBefore(other.start)

    /**
     * Check if this position is contained in another position.
     *
     * @param other the other position
     * @return true if this position is contained in the other, false otherwise
     */
    fun contains(other: Position) = startsBefore(other) && endsAfter(other)

    /**
     * Check if this position is after another position.
     *
     * @param other the other position
     * @return true if this position is after the other, false otherwise
     */
    fun isAfter(other: Position) = start.isAfter(other.end)

    /**
     * Check if this position ends after another position.
     *
     * @param other the other position
     * @return true if this position ends after the other, false otherwise
     */
    fun endsAfter(other: Position) = end.isAfter(other.end)
}

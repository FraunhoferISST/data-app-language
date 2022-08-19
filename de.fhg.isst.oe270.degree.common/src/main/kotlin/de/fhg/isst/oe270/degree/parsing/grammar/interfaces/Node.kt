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
package de.fhg.isst.oe270.degree.parsing.grammar.interfaces

import de.fhg.isst.oe270.degree.parsing.types.Position
import java.io.File
import kotlin.reflect.full.memberProperties

/**
 * A node is an arbitrary subsection of a D° source file.
 *
 * @param file the file this node refers to
 * @param position the position of this node
 * @param parent an optional parent node into which this node is embedded
 */
interface Node {
    val file: String
    val position: Position
    var parent: Node?

    /**
     * Check if two nodes refer the same file.
     *
     * @param other the other node
     * @return true if they refer the same file, false otherwise
     */
    fun isSameFile(other: Node) = File(file) == File(other.file)

    /**
     * Check if this node is before another one.
     *
     * @param other the other node
     * @return true if this node is before the other, false otherwise
     */
    fun isBefore(other: Node) = isSameFile(other) && position.isBefore(other.position)

    /**
     * Check if this node starts before another one.
     *
     * @param other the other node
     * @return true if this node starts before the other, false otherwise
     */
    fun startsBefore(other: Node) = isSameFile(other) && position.isBefore(other.position)

    /**
     * Check if this node is contained in another one.
     *
     * @param other the other node
     * @return true if this node is contained in the other, false otherwise
     */
    fun contains(other: Node) = isSameFile(other) && startsBefore(other) && endsAfter(other)

    /**
     * Check if this node is after another one.
     *
     * @param other the other node
     * @return true if this node is after the other, false otherwise
     */
    fun isAfter(other: Node) = isSameFile(other) && position.isAfter(other.position)

    /**
     * Check if this node ends after another one.
     *
     * @param other the other node
     * @return true if this node ends after the other, false otherwise
     */
    fun endsAfter(other: Node) = isSameFile(other) && position.isAfter(other.position)

    /**
     * Execute a given operation on this node.
     *
     * @param operation the operation to execute
     */
    fun process(operation: (Node) -> Unit) {
        operation(this)
        this.javaClass.kotlin.memberProperties.forEach {
            val propertyValue = it.get(this)
            when (propertyValue) {
                is Node -> propertyValue.process(operation)
                is Collection<*> -> propertyValue.forEach { (it as? Node)?.process(operation) }
            }
        }
    }

    /**
     * Execute a given operation on this node if the types are compatible.
     *
     * @param klass the class to check compatibility with
     * @param operation the operation to execute
     */
    fun <T : Node> specificProcess(klass: Class<T>, operation: (T) -> Unit) {
        process {
            if (klass.isInstance(it)) {
                operation(it as T)
            }
        }
    }

    /**
     * Reflection based to string method.
     *
     * @return string representation of this object
     */
    fun reflectiveToString(): String {
        val sb = StringBuilder("${this.javaClass.simpleName}(")
        this.javaClass.kotlin.memberProperties
            .filter { it.name != "parent" }
            .joinTo(sb, ", ") { "${it.name}=${it.get(this)}" }
        sb.append(")");
        return sb.toString()
    }

    /**
     * Reflection based hash code calculation.
     *
     * @return hash code for this node
     */
    fun reflectiveHashCode(): Int {
        var hashCode = 0
        this.javaClass.kotlin.memberProperties
            .filter { it.name != "parent" }
            .map {
                val value = it.get(this)
                when {
                    value is Node -> value.reflectiveHashCode()
                    value != null -> value.hashCode()
                    else -> 0
                }
            }
            .let {
                it.forEachIndexed { index, i ->
                    hashCode = (hashCode + i)
                    if (index < it.size - 1) { //index points to an element before the last one
                        hashCode *= 31
                    }
                }
            }
        return hashCode
    }

    /**
     * Reflection based equals.
     *
     * @param other some other object
     * @return true if the objects are equal, false otherwise
     */
    fun reflectiveEquals(other: Any?): Boolean {
        when {
            this === other -> return true
            other === null -> return false
            this.javaClass == other.javaClass -> {
                val otherAsNode = other as Node
                return this.javaClass.kotlin.memberProperties
                    .filter { it.name != "parent" }
                    .all {
                        val thisValue = it.get(this)
                        val otherValue = it.get(otherAsNode)
                        when (thisValue) {
                            is Node -> thisValue.reflectiveEquals(otherValue)
                            else -> thisValue == otherValue
                        }
                    }
            }
            else -> return false
        }
    }
}

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
package de.fhg.isst.oe270.degree.grammar

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

abstract class ParseTreeElement {
    abstract fun multiLineString(indentation : String = ""): String
}

class ParseTreeLeaf(private val tokenSymbolicName: String, private val text: String) : ParseTreeElement() {
    override fun toString(): String{
        return "$tokenSymbolicName[$text]"
    }

    override fun multiLineString(indentation : String): String = "$indentation$tokenSymbolicName[$text]\n"
}

class ParseTreeNode(private val ruleSymbolicName: String) : ParseTreeElement() {
    private val children = LinkedList<ParseTreeElement>()

    fun child(child: ParseTreeElement) : ParseTreeNode {
        children.add(child)
        return this
    }

    override fun toString(): String {
        return "Node($ruleSymbolicName) $children"
    }

    override fun multiLineString(indentation : String): String {
        val sb = StringBuilder("$indentation$ruleSymbolicName\n")
        children.forEach { sb.append(it.multiLineString("$indentation  ")) }
        return sb.toString()
    }
}

fun ParserRuleContext.toParseTree() : ParseTreeNode {
    val parseTreeNode = ParseTreeNode(this.javaClass.simpleName.removeSuffix("Context"))
    this.children.forEach {
        when (it) {
            is ParserRuleContext -> parseTreeNode.child(it.toParseTree())
            is TerminalNode -> parseTreeNode.child(ParseTreeLeaf(DegreeParser.VOCABULARY.getSymbolicName(it.symbol.type), it.text))
        }
    }
    return parseTreeNode
}
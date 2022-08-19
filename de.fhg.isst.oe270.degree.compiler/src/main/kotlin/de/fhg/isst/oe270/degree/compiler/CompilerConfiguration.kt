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
package de.fhg.isst.oe270.degree.compiler

import java.io.File
import java.util.*

class CompilerConfiguration {

    // ----------------------------------------------------------------------
    // Source Files
    // ----------------------------------------------------------------------

    var sourceRootDirs: MutableSet<String> = HashSet()

    val applicationProperties: Properties = Properties()

    /**
     * Each entry in this list represents a yaml-file which is stored inside a jar.
     * The key is the classloader which allows to load the resource.
     * The value is the list of names of the files inside the jar.
     */
    var typeYamlResources: MutableMap<ClassLoader, List<String>> = HashMap()

    /**
     * Each entry in this list represents a yaml-file which is stored inside a jar.
     * The key is the classloader which allows to load the resource.
     * The value is the list of names of the files inside the jar.
     */
    var registryYamlResources: MutableMap<ClassLoader, List<String>> = HashMap()

    /**
     * If this entry is set, there is a file which provides nukleus policies.
     */
    var nukleusPoliciesResource: File? = null

    fun addSourceLocation(sourceLocation: String) {
        sourceRootDirs.add(sourceLocation)
    }

    var dataAppSourceRootDir: String = ""

    var includes: MutableSet<String> = HashSet()
    fun addInclude(include: String) {
        includes.add(include)
    }

    var excludes: MutableSet<String> = HashSet()
    fun addExclude(exclude: String) {
        excludes.add(exclude)
    }

    // ----------------------------------------------------------------------
    // Compiler Settings
    // ----------------------------------------------------------------------

    var compilerVersion: String? = null

    // ----------------------------------------------------------------------
    // Docker
    // ----------------------------------------------------------------------

    var dockerEnable: Boolean = false

    var dockerApi: String = ""

    var dockerHost: String = ""

    var dockerTlsVerify: Boolean = true

    var dockerCertPath: String = ""

    var dockerRegistryUsername: String = ""

    var dockerRegistryPassword: String = ""

    var dockerRegistryEmail: String = ""

    var dockerRegistryUrl: String = ""

}

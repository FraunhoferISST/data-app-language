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
package de.fhg.isst.oe270.degree.activities.execution

import com.beust.klaxon.Klaxon
import de.fhg.isst.oe270.degree.types.TypeTaxonomy
import nukleus.core.Format
import nukleus.core.Instance
import org.slf4j.LoggerFactory

/**
 * Superclass for Input- and OutputScopes used by Activities and as input/output for Data Apps.
 * This class cannot be instantiated.
 */
abstract class Scope(open val values: HashMap<String, Instance> = HashMap()) {

    /**
     * The used logger
     */
    private val logger = LoggerFactory.getLogger(Scope::class.java.simpleName)

    /**
     * Tries to insert a (String, Instance) pair into this scope.
     * Will not override existing values, instead the operation fails.
     *
     * @param key identifier which will be used for insertion and later access
     * @param value the instance which should be inserted
     * @return true if the key is free and the insertion was successful, false otherwise
     */
    fun add(key: String, value: Instance): Boolean {
        if (values.containsKey(key)) {
            logger.warn("Tried to add variable $key to scope, but the key is already in use.")
            return false
        }
        if (values.put(key, value) != null) {
            logger.warn("Tried to add variable $key to scope and a previous value was overwritten.")
            return false
        }
        return true
    }

    /**
     * Gets the instance for a given key if it is stored.
     *
     * @param key the identifier used for lookup
     * @return the searched instance if available, null otherwise
     */
    fun get(key: String): Instance? {
        if (!values.containsKey(key)) {
            logger.warn("Tried to obtain variable $key from scope, but it is not found in the scope.")
            return null
        }
        if (values[key] == null) {
            logger.warn("Tried to obtain variable $key from scope, but null is stored in the scope.")
            return null
        }
        return values[key]
    }

    /**
     * Create a json string from this scope.
     *
     * @return content of this scope as JSON string
     */
    fun toJson(): String {
        val jsonValues = HashMap<String, String>()
        values.map { entry ->
            // jsonValues.put(entry.key, entry.value.save(JsonWriter.getInstance()))
            jsonValues.put(entry.key, entry.value.serialize().toString())
        }
        return Klaxon().toJsonString(jsonValues)
    }

    /**
     * Create a formatted json string from this scope.
     *
     * @return content of this scope as formatted JSON string
     */
    fun toPrettyJson(): String {
        return (Klaxon().parseJsonObject(toJson().reader())).toJsonString(true)
    }

    /**
     * Populate this scope from a JSON object, coded as string.
     *
     * @param json the JSON object as string
     */
    fun fromJson(json: String) {
        val parsedValues = Klaxon().parse<Map<String, String>>(json)
        val loadedValues = HashMap<String, Instance>()

        parsedValues!!.map { entry ->
            loadedValues.put(
                entry.key,
                TypeTaxonomy.getInstance().create(Format.json.parse(entry.value))
            )
        }

        values.clear()
        values.putAll(loadedValues)
    }
}

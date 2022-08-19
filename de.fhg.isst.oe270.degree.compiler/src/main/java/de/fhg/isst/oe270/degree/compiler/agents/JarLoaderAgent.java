/*
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
package de.fhg.isst.oe270.degree.compiler.agents;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * This agent allows to add arbitrary jars to the classpath during runtime.
 */
public final class JarLoaderAgent {

    /**
     * Hidden default constructor.
     */
    private JarLoaderAgent() {
    }

    /**
     * The instrumentation of this agent.
     */
    private static Instrumentation instrumentation;

    /**
     * All class loaders which are registered by this agent.
     */
    public static final List<URLClassLoader> CLASS_LOADERS = new ArrayList<>();

    /**
     * List of jars that will be used as dependencies in compiled data app.
     */
    public static final List<URL> JAR_DEPENDENCIES = new ArrayList<>();

    /**
     * Required for agents.
     *
     * @param args arguments for the agent
     * @param inst instrumentation used by the agent
     */
    public static void premain(final String args, final Instrumentation inst) {
        instrumentation = inst;
    }

    /**
     * Add a given jar to classpath.
     *
     * @param arg the jar file to add
     * @return true if adding was successful, false otherwise
     */
    public static boolean addJarToClasspath(final String arg) {
        try {
            File file = new File(arg);
            URL url = file.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
            CLASS_LOADERS.add(classLoader);
        } catch (Exception e) {
            return false;
        }

        return true;
    }


}

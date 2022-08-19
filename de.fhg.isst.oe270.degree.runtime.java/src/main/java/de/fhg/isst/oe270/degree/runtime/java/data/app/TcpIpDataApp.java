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
package de.fhg.isst.oe270.degree.runtime.java.data.app;

/**
 * This application type cannot be directly used, but is a supertype for all D° applications which
 * offer some kind of TCP/IP interface.
 */
public abstract class TcpIpDataApp extends CliDataApp {

    /**
     * This key is used to identify the port item within the configuration map.
     */
    public static final String PORT_KEY = "port";

    /**
     * Get port of this Data App from Data App configuration.
     *
     * @return The port of this Data App, or an empty string if unknown.
     */
    public String getPort() {
        if (!CONFIGURATION_MAP.containsKey(PORT_KEY)) {
            logWarn("Could not resolve port for this Data App.");
            return "";
        }
        return CONFIGURATION_MAP.get(PORT_KEY);
    }

    /**
     * Initialize the app.
     */
    @Override
    protected void init() {
        super.init();
    }

}

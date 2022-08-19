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

import de.fhg.isst.oe270.degree.activities.execution.OutputScope;
import nukleus.core.Identifier;
import nukleus.core.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * D° application which offers a command line interface and a periodic execution of its logic.
 */
public abstract class CliDataAppPeriodic extends CliDataApp {

    /**
     * Indicator if the shutdown hook was already registered.
     */
    private static boolean shutdownHookRegistered = false;

    /**
     * Indicator if the periodic execution is running.
     */
    private final Map<UUID, Boolean> loops = new HashMap<>();

    /**
     * Used for thread safe access to {@link #loops}.
     */
    private final Map<UUID, Object> locks = new HashMap<>();

    /**
     * Thread which reacts to stopping signals (like CTRL+C) and properly shuts down the Data App.
     */
    private Thread shutdownHook;

    /**
     * This function will be called as last statement in the constructor.
     */
    @Override
    protected void init() {
        synchronized (CliDataAppPeriodic.class) {
            super.init();
            if (!shutdownHookRegistered) {
                shutdownHookRegistered = true;
                shutdownHook = new Thread(() -> {
                    // output "result"
                    logInfo(
                            "Data App was terminated either by an error or an external signal.");
                    OutputScope exitScope = new OutputScope();
                    Instance exitMessage = TYPE_TAXONOMY.create(new Identifier("core.Text"));
                    exitMessage.write(
                            "Data App was terminated either by an error or an external signal.");
                    exitScope.getValues().put("result", exitMessage);
                    System.out.println(exitScope.toJson());

                });
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }
        }
    }

    /**
     * Get periodic time of this Data App from Data App configuration.
     * This is the time in ms between two executions of the application
     * logic if operated in periodic mode.
     *
     * @return The periodic time of this Data App, or an empty string if unknown.
     */
    @SuppressWarnings("unused")
    public String getPeriodicTime() {
        if (!CONFIGURATION_MAP.containsKey(PERIODIC_TIME_KEY)) {
            logError("Could not resolve periodic time for this Data App.");
            return "";
        }
        return CONFIGURATION_MAP.get(PERIODIC_TIME_KEY);
    }

}

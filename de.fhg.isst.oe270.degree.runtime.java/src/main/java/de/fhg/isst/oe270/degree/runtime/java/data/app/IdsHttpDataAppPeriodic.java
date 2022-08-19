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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is a D° application which offers a HTTP-interface and IDS metadata endpoints
 * and periodic, controllable execution of its logic.
 */
public abstract class IdsHttpDataAppPeriodic extends IdsHttpDataApp {

    /**
     * Indicator if the periodic execution is running.
     */
    private final Map<UUID, Boolean> loops = new HashMap<>();

    /**
     * Used for thread safe access to {@link #loops}.
     */
    private final Map<UUID, Object> locks = new HashMap<>();

    /**
     * Get periodic time of this Data App from Data App configuration.
     * This is the time in ms between two executions of the application logic if operated
     * in periodic mode.
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

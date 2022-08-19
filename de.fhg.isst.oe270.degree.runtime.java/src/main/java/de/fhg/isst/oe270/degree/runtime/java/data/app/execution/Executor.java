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
package de.fhg.isst.oe270.degree.runtime.java.data.app.execution;

import de.fhg.isst.oe270.degree.activities.execution.OutputScope;
import de.fhg.isst.oe270.degree.runtime.java.data.app.DataApp;
import de.fhg.isst.oe270.degree.types.TypeTaxonomy;
import nukleus.core.Identifier;
import nukleus.core.Instance;

import java.util.UUID;

/**
 * This class is used to capsulate the execution of a D° application's logic into its own thread.
 */
public final class Executor implements Runnable {

    /**
     * This object is used as mutex for accesses to {@link #executing}.
     */
    private final Object executorLock = new Object();

    /**
     * This UUID is used for identifing the execution.
     */
    private final UUID sessionId = UUID.randomUUID();

    /**
     * The used type system.
     */
    private final TypeTaxonomy typeTaxonomy = TypeTaxonomy.getInstance();

    /**
     * Boolean indicator used to determine if the executor is currently running.
     */
    private boolean executing = false;

    /**
     * Boolean flag to indicate if the executor started its execution.
     */
    private boolean initalized = false;

    /**
     * Container used to transfer Data App inputs into the executor.
     */
    private String inputs = null;

    /**
     * Container used to transfer Data App outputs from the executor.
     */
    private String outputs;

    /**
     * The data app, which is executed by this executor.
     */
    private DataApp dataApp;

    /**
     * Execute the process method of the executor's data app.
     */
    @Override
    public void run() {
        // signal running execution
        synchronized (executorLock) {
            if (executing) {
                OutputScope outputScope = new OutputScope();
                Instance returnInstance = typeTaxonomy.create(new Identifier("Core.Text"));
                returnInstance.write(
                        "Data App is already executing its logic. This request is ignored.");
                outputScope.getValues().put("status", returnInstance);
                outputs = outputScope.toJson();

                return;
            }
            if (inputs == null) {
                OutputScope outputScope = new OutputScope();
                Instance returnInstance = typeTaxonomy.create(new Identifier("Core.Error"));
                returnInstance.write("Data App cannot be started without inputs.");
                outputScope.getValues().put("error", returnInstance);
                outputs = outputScope.toJson();

                return;
            }
            if (dataApp == null) {
                OutputScope outputScope = new OutputScope();
                Instance returnInstance = typeTaxonomy.create(new Identifier("Core.Error"));
                returnInstance.write("Data App cannot be started without set Data App.");
                outputScope.getValues().put("error", returnInstance);
                outputs = outputScope.toJson();

                return;
            }
            executing = true;
            initalized = true;
        }

        // actual execution
        try {
            outputs = dataApp.process(inputs, sessionId);
        } catch (Exception e) {
            OutputScope outputScope = new OutputScope();
            Instance returnInstance = typeTaxonomy.create(new Identifier("Core.Error"));
            returnInstance.write(
                    "An error occurred during the execution of the Data App. Message: "
                            + e.getMessage());
            outputScope.getValues().put("error", returnInstance);
            outputs = outputScope.toJson();
        } finally {
            // signal finished execution
            synchronized (executorLock) {
                executing = false;
            }
        }
    }

    /**
     * Check if the execution is finished and results are available.
     *
     * @return true if the execution is not running and the executor is initialized, false otherwise
     */
    public boolean isOutputReady() {
        synchronized (executorLock) {
            return !executing && initalized;
        }
    }

    /**
     * Try to obtain the outputs from the execution, which is performed by the executor.
     *
     * @param uuid A session ID
     * @return Either the execution result or an JSON representation of an {@link OutputScope}
     * signaling the ongoing execution or an error in case the given session ID does not match
     * the expected one.
     */
    public String tryRetrieveOutputs(final UUID uuid) {
        synchronized (executorLock) {
            if (uuid != sessionId) {
                OutputScope outputScope = new OutputScope();
                Instance returnInstance = typeTaxonomy.create(new Identifier("Core.Text"));
                returnInstance.write("Given session ID does not match expected session ID.");
                outputScope.getValues().put("error", returnInstance);

                return outputScope.toJson();
            }
            if (executing) {
                OutputScope outputScope = new OutputScope();
                Instance returnInstance = typeTaxonomy.create(new Identifier("Core.Text"));
                returnInstance.write("Data App execution not yet finished.");
                outputScope.getValues().put("status", returnInstance);
                Instance uuidInstance = typeTaxonomy.create(new Identifier("Core.UUID"));
                uuidInstance.write(sessionId.toString());
                outputScope.getValues().put("sessionId", uuidInstance);

                return outputScope.toJson();
            } else {
                return outputs;
            }
        }

    }

    /**
     * Allows to set the inputs for the executor once(!).
     *
     * @param input The desired inputs
     * @see #inputs
     */
    public void trySetInputs(final String input) {
        if (inputs != null) {
            return;
        }
        this.inputs = input;
    }

    /**
     * Set the executed D° application.
     *
     * @param app the data app
     */
    public void setDataApp(final DataApp app) {
        this.dataApp = app;
    }

    /**
     * Get the session id of this executor.
     *
     * @return the session id
     */
    public UUID getSessionId() {
        return sessionId;
    }

}

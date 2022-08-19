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
package de.fhg.isst.oe270.degree.runtime.java.context.core;

import de.fhg.isst.oe270.degree.runtime.java.context.ContextEntity;
import de.fhg.isst.oe270.degree.runtime.java.context.ContextModule;
import de.fhg.isst.oe270.degree.runtime.java.context.RootContextModule;
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadOnlyEntity;

import java.util.HashMap;

/**
 * This context module is used to store data about the user which has started the D° application.
 */
@RootContextModule
public class OsUserInformationContextModule extends ContextModule {

    /**
     * Create the module with correct name.
     */
    public OsUserInformationContextModule() {
        super("OsUserInformation");
    }

    @Override
    public final HashMap<String, ContextEntity> createDefaultContext() {
        HashMap<String, ContextEntity> defaultConfiguration = new HashMap<>();
        defaultConfiguration.put("username", new ReadOnlyEntity("username",
                System.getProperty("user.name")));
        defaultConfiguration.put("userroles", new ReadOnlyEntity("userroles",
                NO_VALUE));

        return defaultConfiguration;
    }

}

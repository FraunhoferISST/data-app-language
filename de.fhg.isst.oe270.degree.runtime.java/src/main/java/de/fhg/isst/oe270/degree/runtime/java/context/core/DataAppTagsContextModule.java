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

import java.util.HashMap;

/**
 * This context module is used for tags that can be applied to D° applications
 * and language elements.
 */
@RootContextModule
public class DataAppTagsContextModule extends ContextModule {

    /**
     * Create the module with correct name.
     */
    public DataAppTagsContextModule() {
        super("TagsContextModule");
    }

    @Override
    public final HashMap<String, ContextEntity> createDefaultContext() {
        return new HashMap<>();
    }
}

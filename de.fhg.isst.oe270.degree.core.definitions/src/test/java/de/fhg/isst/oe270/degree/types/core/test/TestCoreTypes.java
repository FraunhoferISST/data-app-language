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
package de.fhg.isst.oe270.degree.types.core.test;

import nukleus.core.Identifier;
import nukleus.core.Nukleus;
import nukleus.core.TypeSystem;
import nukleus.core.custom.DegreeCustomization;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCoreTypes {

    @Test
    public void testLoadCoreTypes() {
        Nukleus.custom = new DegreeCustomization();
        Nukleus.setDefaultTypes("core.types.yaml");
        TypeSystem types = Nukleus.getInstance();
        assertEquals("Text", types.lookup(Identifier.of("core.Text")).getIdentifier().parseIdentifier());
        assertEquals("Error", types.lookup(Identifier.of("core.Error")).getIdentifier().parseIdentifier());
    }

}

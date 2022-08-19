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
package de.fhg.isst.oe270.degree.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Tags which can be used to describe the usage of an activity's input parameters
 * for specific actions.
 */
public enum ActivityInputTag {

    /**
     *  The data in the parameter will be read. This is the default value.
     */
    READ,
    /**
     * The data in the parameter will be modified.
     */
    MODIFY,
    /**
     * The data in the parameter will be written to disk.
     */
    PERSIST,
    /**
     * The data in the parameter will be aggregated.
     */
    AGGREGATE,
    /**
     * The data in the parameter will be distributed.
     */
    DISTRIBUTE,
    /**
     * The data in the parameter will be anonymized.
     */
    ANONYMIZE;

    /**
     * Transform nukleus {@link de.fhg.isst.degree.types.gen.degree.ActivityInputTag} to
     * {@link ActivityInputTag}s.
     *
     * @param nukleusTag A valid instance of
     *                  {@link de.fhg.isst.degree.types.gen.degree.ActivityInputTag}
     *                   with arbitrary cardinality
     * @return {@link List} of {@link ActivityInputTag}s that match the content of the given
     *         {@link de.fhg.isst.degree.types.gen.degree.ActivityInputTag}
     */
    public static List<ActivityInputTag> parseFromNukleus(
            final de.fhg.isst.degree.types.gen.degree.ActivityInputTag nukleusTag
    ) {
        List<ActivityInputTag> result = new ArrayList<>();

        for (int i = 0; i < nukleusTag.size(); i++) {
            result.add(ActivityInputTag.valueOf(nukleusTag.get(i).read()));
        }

        return result;
    }
}

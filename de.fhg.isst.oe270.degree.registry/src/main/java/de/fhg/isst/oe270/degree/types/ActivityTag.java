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
 * Tags which can be used to describe an activity's purpose or behavior.
 */
public enum ActivityTag {
    /*
     * Tags describing the behaviour of activities
     */

    /**
     * Used for stateless activities.
     */
    STATELESS,
    /**
     * Used for stateful activities.
     */
    STATEFUL,
    /**
     * Used for activities, which persist ayn data in some way.
     */
    PERSISTING,

    /*
     * Tags describing the purpose of usage
     */

    /**
     * Used for activities, which are used for the purpose of risk management.
     */
    RISK_MANAGEMENT,
    /**
     * Used for activities, which are used for the purpose of marketing.
     */
    MARKETING;

    /**
     * Transform nukleus {@link de.fhg.isst.degree.types.gen.degree.ActivityTag} to
     * {@link ActivityTag}s.
     *
     * @param nukleusTag A valid instance of {@link de.fhg.isst.degree.types.gen.degree.ActivityTag}
     *                   with arbitrary cardinality
     * @return {@link List} of {@link ActivityTag}s that match the content of the given
     *         {@link de.fhg.isst.degree.types.gen.degree.ActivityTag}
     */
    public static List<ActivityTag> parseFromNukleus(
            final de.fhg.isst.degree.types.gen.degree.ActivityTag nukleusTag
    ) {
        java.util.List<de.fhg.isst.oe270.degree.types.ActivityTag> result = new ArrayList<>();

        for (int i = 0; i < nukleusTag.size(); i++) {
            result.add(valueOf(nukleusTag.get(i).read()));
        }

        return result;
    }

}

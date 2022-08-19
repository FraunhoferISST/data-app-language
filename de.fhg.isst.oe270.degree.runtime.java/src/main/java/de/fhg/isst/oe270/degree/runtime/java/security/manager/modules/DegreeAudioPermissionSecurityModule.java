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
package de.fhg.isst.oe270.degree.runtime.java.security.manager.modules;

import de.fhg.isst.oe270.degree.runtime.java.exceptions.security.DegreeUnsupportedSecurityFeatureException;
import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;

import javax.sound.sampled.AudioPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link AudioPermission} of the D° security manager.
 */
public final class DegreeAudioPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeAudioPermissionSecurityModule() {
    }

    /**
     * The javax.sound.sampled.AudioPermission class represents access rights to the audio system
     * resources. An AudioPermission contains a target name but no actions list; you either have the
     * named permission or you don't.
     * <p>
     * The target name is the name of the audio permission (see the table below). The names follow
     * the hierarchical property-naming convention. Also, an asterisk can be used to represent all
     * the audio permissions.
     *
     * @param permission the AudioPermission which will be checked
     * @return a list of required permissions
     * @see AudioPermission
     */
    public static List<RequiredPermission> checkAudioPermission(final AudioPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "play":
                return checkPlay(permission);
            case "record":
                return checkRecord(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: Audio playback through the audio device or devices on the system.
     * Allows the application to obtain and manipulate lines and mixers for audio playback
     * (rendering).
     * <p>
     * Risk of Allowing this permission In some cases use of this permission may affect other
     * applications because the audio from one line may be mixed with other audio being played on
     * the system, or because manipulation of a mixer affects the audio for all lines using that
     * mixer.
     *
     * @param permission The AudioPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkPlay(final AudioPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Audio recording through the audio device or devices on the
     * system. Allows the application to obtain and manipulate lines and mixers for audio recording
     * (capture).
     * <p>
     * Risk of Allowing this permission In some cases use of this permission may affect other
     * applications because manipulation of a mixer affects the audio for all lines using that
     * mixer. This permission can enable an applet or application to eavesdrop on a user.
     *
     * @param permission The AudioPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkRecord(final AudioPermission permission) {
        return new ArrayList<>();
    }
}

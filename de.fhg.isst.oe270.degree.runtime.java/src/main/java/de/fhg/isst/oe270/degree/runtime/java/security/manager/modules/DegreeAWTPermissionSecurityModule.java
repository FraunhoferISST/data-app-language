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

import java.awt.AWTPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link AWTPermission} of the D° security manager.
 */
public final class DegreeAWTPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeAWTPermissionSecurityModule() {
    }

    /**
     * A java.awt.AWTPermission is for AWT permissions.
     *
     * @param permission the AWTPermission which will be checked
     * @return a list of required permissions
     * @see AWTPermission
     */
    public static List<RequiredPermission> checkAWTPermission(final AWTPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "accessClipboard":
                return checkAccessClipboard(permission);
            case "accessEventQueue":
                return checkAccessEventQueue(permission);
            case "accessSystemTray":
                return checkAccessSystemTray(permission);
            case "createRobot":
                return checkCreateRobot(permission);
            case "fullScreenExclusive":
                return checkFullScreenExclusive(permission);
            case "listenToAllAWTEvents":
                return checkListenToAllAWTEvents(permission);
            case "readDisplayPixels":
                return checkReadDisplayPixels(permission);
            case "replaceKeyboardFocusManager":
                return checkReplaceKeyboardFocusManager(permission);
            case "setAppletStub":
                return checkSetAppletStub(permission);
            case "setWindowsAlwaysOnTop":
                return checkSetWindowsAlwaysOnTop(permission);
            case "showWindowWithoutWarningBanner":
                return checkShowWindowWithoutWarningBanner(permission);
            case "toolkitModality":
                return checkToolkitModality(permission);
            case "watchMousePointer":
                return checkWatchMousePointer(permission);
            default:
                throw new DegreeUnsupportedSecurityFeatureException();
        }
    }

    /**
     * What the permission allows: Posting and retrieval of information to and from the AWT
     * clipboard
     * <p>
     * Risk of Allowing this permission This would allow malfeasant code to share potentially
     * sensitive or confidential information.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkAccessClipboard(final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Access to the AWT event queue
     * <p>
     * Risk of Allowing this permission After retrieving the AWT event queue, malicious code may
     * peek at and even remove existing events from the system, as well as post bogus events which
     * may purposefully cause the application or applet to misbehave in an insecure manner.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkAccessEventQueue(final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Access to the AWT SystemTray instance
     * <p>
     * Risk of Allowing this permission This would allow malicious code to add tray icons to the
     * system tray. First, such an icon may look like the icon of some known application (such as a
     * firewall or anti-virus) and order a user to do something unsafe (with help of balloon
     * messages). Second, the system tray may be glutted with tray icons so that no one could add a
     * tray icon anymore.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkAccessSystemTray(final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Create java.awt.Robot objects
     * <p>
     * Risk of Allowing this permission The java.awt.Robot object allows code to generate
     * native-level mouse and keyboard events as well as read the screen. It could allow malicious
     * code to control the system, run other programs, read the display, and deny mouse and keyboard
     * access to the user.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCreateRobot(final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Enter full-screen exclusive mode
     * <p>
     * Risk of Allowing this permission Entering full-screen exclusive mode allows direct access to
     * low-level graphics card memory. This could be used to spoof the system, since the program is
     * in direct control of rendering.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkFullScreenExclusive(
            final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Listen to all AWT events, system-wide
     * <p>
     * Risk of Allowing this permission After adding an AWT event listener, malicious code may scan
     * all AWT events dispatched in the system, allowing it to read all user input (such as
     * passwords). Each AWT event listener is called from within the context of that event queue's
     * EventDispatchThread, so if the accessEventQueue permission is also enabled, malicious code
     * could modify the contents of AWT event queues system-wide, causing the application or applet
     * to misbehave in an insecure manner.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkListenToAllAWTEvents(
            final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Readback of pixels from the display screen
     * <p>
     * Risk of Allowing this permission Interfaces such as the java.awt.Composite interface which
     * allow arbitrary code to examine pixels on the display enable malicious code to snoop on the
     * activities of the user.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkReadDisplayPixels(final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Sets the KeyboardFocusManager for a particular thread.
     * <p>
     * Risk of Allowing this permission When a SecurityManager is installed, the invoking thread
     * must be granted this permission in order to replace the current KeyboardFocusManager. If
     * permission is not granted, a SecurityException will be thrown.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkReplaceKeyboardFocusManager(
            final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting the stub which implements Applet container services
     * <p>
     * Risk of Allowing this permission Malicious code could set an applet's stub and result in
     * unexpected behavior or denial of service to an applet.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetAppletStub(final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting always-on-top property of the window:
     * Window.setAlwaysOnTop(boolean)
     * <p>
     * Risk of Allowing this permission The malicious window might make itself look and behave like
     * a real full desktop, so that information entered by the unsuspecting user is captured and
     * subsequently misused.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetWindowsAlwaysOnTop(
            final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Display of a window without also displaying a banner warning that
     * the window was created by an applet
     * <p>
     * Risk of Allowing this permission Without this warning, an applet may pop up windows without
     * the user knowing that they belong to an applet. Since users may make security-sensitive
     * decisions based on whether or not the window belongs to an applet (entering a username and
     * password into a dialog box, for example), disabling this warning banner may allow applets to
     * trick the user into entering such information.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkShowWindowWithoutWarningBanner(
            final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Creating TOOLKIT_MODAL dialogs and setting the TOOLKIT_EXCLUDE
     * window property.
     * <p>
     * Risk of Allowing this permission When a toolkit-modal dialog is shown from an applet, it
     * blocks all other applets in the browser. When launching applications from Java Web Start, its
     * windows (such as the security dialog) may also be blocked by toolkit-modal dialogs, shown
     * from these applications.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkToolkitModality(final AWTPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Getting the information about the mouse pointer position at any
     * time
     * <p>
     * Risk of Allowing this permission Constantly watching the mouse pointer, an applet can make
     * guesses about what the user is doing, i.e. moving the mouse to the lower left corner of the
     * screen most likely means that the user is about to launch an application. If a virtual keypad
     * is used so that keyboard is emulated using the mouse, an applet may guess what is being
     * typed.
     *
     * @param permission The AWTPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkWatchMousePointer(final AWTPermission permission) {
        return new ArrayList<>();
    }

}

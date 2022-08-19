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

import java.security.SecurityPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link SecurityPermission} of the D° security manager.
 */
public final class DegreeSecurityPermissionSecurityModule {

    /**
     * Private default constructor.
     */
    private DegreeSecurityPermissionSecurityModule() {
    }

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * A java.security.SecurityPermission is for security permissions. A SecurityPermission contains
     * a name (also referred to as a "target name") but no actions list; you either have the named
     * permission or you don't.
     * <p>
     * The target name is the name of a security configuration parameter (see below). Currently the
     * SecurityPermission object is used to guard access to the Policy, Security, Provider, Signer,
     * and Identity objects.
     *
     * @param permission the SecurityPermission which will be checked
     * @return a list of required permissions
     * @see SecurityPermission
     */
    public static List<RequiredPermission> checkSecurityPermission(
            final SecurityPermission permission) {
        String targetName = permission.getName();

        switch (targetName) {
            case "createAccessControlContext":
                return checkCreateAccessControlContext(permission);
            case "getDomainCombiner":
                return checkGetDomainCombiner(permission);
            case "getPolicy":
                return checkGetPolicy(permission);
            case "setPolicy":
                return checkSetPolicy(permission);
            case "insertProvider":
                return checkInsertProvider(permission);
            case "setSystemScope":
                return checkSetSystemScope(permission);
            case "setIdentityPublicKey":
                return checkSetIdentityPublicKey(permission);
            case "setIdentityInfo":
                return checkSetIdentityInfo(permission);
            case "addIdentityCertificate":
                return checkAddIdentityCertificate(permission);
            case "removeIdentityCertificate":
                return checkRemoveIdentityCertificate(permission);
            case "printIdentity":
                return checkPrintIdentity(permission);
            case "getSignerPrivateKey":
                return checkGetSignerPrivateKey(permission);
            case "setSignerKeyPair":
                return checkSetSignerKeyPair(permission);
            default:
                if (targetName.startsWith("createPolicy")) {
                    return checkCreatePolicy(permission);
                } else if (targetName.startsWith("getProperty")) {
                    return checkGetProperty(permission);
                } else if (targetName.startsWith("setProperty")) {
                    return checkSetProperty(permission);
                } else if (targetName.startsWith("removeProvider")) {
                    return checkRemoveProvider(permission);
                } else if (targetName.startsWith("clearProviderProperties")) {
                    return checkClearProviderProperties(permission);
                } else if (targetName.startsWith("putProviderProperty")) {
                    return checkPutProviderProperty(permission);
                } else if (targetName.startsWith("removeProviderProperty")) {
                    return checkRemoveProviderProperty(permission);
                } else if (targetName.startsWith("insertProvider")) {
                    return checkInsertProviderNamed(permission);
                } else {
                    throw new DegreeUnsupportedSecurityFeatureException();
                }
        }
    }

    /**
     * What the permission allows: Creation of an AccessControlContext
     * <p>
     * Risk of Allowing this permission This allows someone to instantiate an AccessControlContext
     * with a DomainCombiner. Extreme care must be taken when granting this permission. Malicious
     * code could create a DomainCombiner that augments the set of permissions granted to code, and
     * even grant the code AllPermission.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCreateAccessControlContext(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieval of an AccessControlContext's DomainCombiner
     * <p>
     * Risk of Allowing this permission This allows someone to query the policy via the
     * getPermissions call, which discloses which permissions would be granted to a given
     * CodeSource. While revealing the policy does not compromise the security of the system, it
     * does provide malicious code with additional information which it may use to better aim an
     * attack. It is wise not to divulge more information than necessary.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetDomainCombiner(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieval of the system-wide security policy (specifically, of
     * the currently-installed Policy object)
     * <p>
     * Risk of Allowing this permission This allows someone to query the policy via the
     * getPermissions call, which discloses which permissions would be granted to a given
     * CodeSource. While revealing the policy does not compromise the security of the system, it
     * does provide malicious code with additional information which it may use to better aim an
     * attack. It is wise not to divulge more information than necessary.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetPolicy(final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of the system-wide security policy (specifically, the
     * Policy object)
     * <p>
     * Risk of Allowing this permission Granting this permission is extremely dangerous, as
     * malicious code may grant itself all the necessary permissions it needs to successfully mount
     * an attack on the system.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetPolicy(final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Getting an instance of a Policy via Policy.getInstance
     * <p>
     * Risk of Allowing this permission Granting this permission enables code to obtain a Policy
     * object. Malicious code may query the Policy object to determine what permissions have been
     * granted to code other than itself.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkCreatePolicy(final SecurityPermission permission) {
        String policyType = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieval of the security property with the specified key
     * <p>
     * Risk of Allowing this permission Depending on the particular key for which access has been
     * granted, the code may have access to the list of security providers, as well as the location
     * of the system-wide and user security policies. while revealing this information does not
     * compromise the security of the system, it does provide malicious code with additional
     * information which it may use to better aim an attack.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetProperty(final SecurityPermission permission) {
        String key = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of the security property with the specified key
     * <p>
     * Risk of Allowing this permission This could include setting a security provider or defining
     * the location of the system-wide security policy. Malicious code that has permission to set a
     * new security provider may set a rogue provider that steals confidential information such as
     * cryptographic private keys. In addition, malicious code with permission to set the location
     * of the system-wide security policy may point it to a security policy that grants the attacker
     * all the necessary permissions it requires to successfully mount an attack on the system.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetProperty(final SecurityPermission permission) {
        String key = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Addition of a new provider
     * <p>
     * Risk of Allowing this permission This would allow somebody to introduce a possibly malicious
     * provider (e.g., one that discloses the private keys passed to it) as the highest-priority
     * provider. This would be possible because the Security object (which manages the installed
     * providers) currently does not check the integrity or authenticity of a provider before
     * attaching it. The "insertProvider" permission subsumes the "insertProvider.{provider name}"
     * permission (see the section below for more information).
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkInsertProvider(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Removal of the specified provider
     * <p>
     * Risk of Allowing this permission This may change the behavior or disable execution of other
     * parts of the program. If a provider subsequently requested by the program has been removed,
     * execution may fail. Also, if the removed provider is not explicitly requested by the rest of
     * the program, but it would normally be the provider chosen when a cryptography service is
     * requested (due to its previous order in the list of providers), a different provider will be
     * chosen instead, or no suitable provider will be found, thereby resulting in program failure.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkRemoveProvider(
            final SecurityPermission permission) {
        String providerName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: "Clearing" of a Provider so that it no longer contains the
     * properties used to look up services implemented by the provider
     * <p>
     * Risk of Allowing this permission This disables the lookup of services implemented by the
     * provider. This may thus change the behavior or disable execution of other parts of the
     * program that would normally utilize the Provider, as described under the
     * "removeProvider.{provider name}" permission.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkClearProviderProperties(
            final SecurityPermission permission) {
        String providerName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of properties for the specified Provider
     * <p>
     * Risk of Allowing this permission The provider properties each specify the name and location
     * of a particular service implemented by the provider. By granting this permission, you let
     * code replace the service specification with another one, thereby specifying a different
     * implementation.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkPutProviderProperty(
            final SecurityPermission permission) {
        String providerName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Removal of properties from the specified Provider
     * <p>
     * Risk of Allowing this permission This disables the lookup of services implemented by the
     * provider. They are no longer accessible due to removal of the properties specifying their
     * names and locations. This may change the behavior or disable execution of other parts of the
     * program that would normally utilize the Provider, as described under the
     * "removeProvider.{provider name}" permission.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkRemoveProviderProperty(
            final SecurityPermission permission) {
        String providerName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Addition of a new provider, with the specified name
     * <p>
     * Risk of Allowing this permission This would allow somebody to introduce a possibly malicious
     * provider (e.g., one that discloses the private keys passed to it) as the highest-priority
     * provider. This would be possible because the Security object (which manages the installed
     * providers) currently does not check the integrity or authenticity of a provider before
     * attaching it.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkInsertProviderNamed(
            final SecurityPermission permission) {
        String providerName = permission.getName().split("\\.")[1];
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of the system identity scope
     * <p>
     * Risk of Allowing this permission This would allow an attacker to configure the system
     * identity scope with certificates that should not be trusted, thereby granting applet or
     * application code signed with those certificates privileges that would have been denied by the
     * system's original identity scope.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetSystemScope(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of the public key for an Identity
     * <p>
     * Risk of Allowing this permission If the identity is marked as "trusted", this allows an
     * attacker to introduce a different public key (e.g., its own) that is not trusted by the
     * system's identity scope, thereby granting applet or application code signed with that public
     * key privileges that would have been denied otherwise.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetIdentityPublicKey(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of a general information string for an Identity
     * <p>
     * Risk of Allowing this permission This allows attackers to set the general description for an
     * identity. This may trick applications into using a different identity than intended or may
     * prevent applications from finding a particular identity.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetIdentityInfo(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Addition of a certificate for an Identity
     * <p>
     * Risk of Allowing this permission This allows attackers to set a certificate for an identity's
     * public key. This is dangerous because it affects the trust relationship across the system.
     * This public key suddenly becomes trusted to a wider audience than it otherwise would be.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkAddIdentityCertificate(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Removal of a certificate for an Identity
     * <p>
     * Risk of Allowing this permission This allows attackers to remove a certificate for an
     * identity's public key. This is dangerous because it affects the trust relationship across the
     * system. This public key suddenly becomes considered less trustworthy than it otherwise would
     * be.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkRemoveIdentityCertificate(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Viewing the name of a principal and optionally the scope in which
     * it is used, and whether or not it is considered "trusted" in that scope.
     * <p>
     * Risk of Allowing this permission The scope that is printed out may be a filename, in which
     * case it may convey local system information. For example, here's a sample printout of an
     * identity named "carol", who is marked not trusted in the user's identity database:
     * carol[/home/luehe/identitydb.obj][not trusted]
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkPrintIdentity(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Retrieval of a Signer's private key
     * <p>
     * Risk of Allowing this permission It is very dangerous to allow access to a private key;
     * private keys are supposed to be kept secret. Otherwise, code can use the private key to sign
     * various files and claim the signature came from the Signer.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkGetSignerPrivateKey(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

    /**
     * What the permission allows: Setting of the key pair (public key and private key) for a
     * Signer
     * <p>
     * Risk of Allowing this permission This would allow an attacker to replace somebody else's (the
     * "target's") keypair with a possibly weaker keypair (e.g., a keypair of a smaller keysize).
     * This also would allow the attacker to listen in on encrypted communication between the target
     * and its peers. The target's peers might wrap an encryption session key under the target's
     * "new" public key, which would allow the attacker (who possesses the corresponding private
     * key) to unwrap the session key and decipher the communication data encrypted under that
     * session key.
     *
     * @param permission The SecurityPermission that will be checked
     * @return a list of required permissions
     */
    private static List<RequiredPermission> checkSetSignerKeyPair(
            final SecurityPermission permission) {
        return new ArrayList<>();
    }

}

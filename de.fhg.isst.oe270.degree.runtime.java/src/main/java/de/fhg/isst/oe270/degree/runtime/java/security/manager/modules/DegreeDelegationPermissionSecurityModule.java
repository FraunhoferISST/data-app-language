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

import de.fhg.isst.oe270.degree.runtime.java.sandbox.Sandbox;
import de.fhg.isst.oe270.degree.runtime.java.security.resolving.RequiredPermission;

import javax.security.auth.kerberos.DelegationPermission;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for {@link DelegationPermission} of the D° security manager.
 */
public final class DegreeDelegationPermissionSecurityModule {

    /**
     * The application's sandbox.
     */
    private static final Sandbox SANDBOX = Sandbox.getInstance();

    /**
     * Private default constructor.
     */
    private DegreeDelegationPermissionSecurityModule() {
    }

    /**
     * The javax.security.auth.kerberos.DelegationPermission class is used to restrict the usage of
     * the Kerberos delegation model; ie, forwardable and proxiable tickets. The target name of this
     * Permission specifies a pair of kerberos service principals. The first is the subordinate
     * service principal being entrusted to use the Ticket Granting Ticket (TGT). The second service
     * principal designates the target service the subordinate service principal is to interact with
     * on behalf of the initiating KerberosPrincipal. This latter service principal is specified to
     * restrict the use of a proxiable ticket.
     * <p>
     * For example, to specify the "host" service use of a forwardable TGT, the target permission is
     * specified as follows:
     * <p>
     * DelegationPermission("\"host/foo.example.com@EXAMPLE.COM\"
     * \"krbtgt/EXAMPLE.COM@EXAMPLE.COM\"");
     * To give the "backup" service a proxiable NFS service ticket, the target permission might be
     * specified: DelegationPermission("\"backup/bar.example.com@EXAMPLE.COM\"
     * \"nfs/home.EXAMPLE.COM@EXAMPLE.COM\"");
     *
     * @param permission the DelegationPermission which will be checked
     * @return a list of required permissions
     * @see DelegationPermission
     */
    public static List<RequiredPermission> checkDelegationPermission(
            final DelegationPermission permission) {
        return new ArrayList<>();
    }

}

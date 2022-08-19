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
package de.fhg.isst.oe270.degree.runtime.java.data.app.http;

import de.fhg.isst.oe270.degree.runtime.java.context.ContextModule;
import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext;
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadOnlyEntity;
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadWriteEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;

/**
 * This filter is used for D° application with HTTP interface to extract user information
 * from JWTs.
 */
public class JWTUserDataFilter implements Filter {

    /**
     * The used logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(
            JWTUserDataFilter.class.getSimpleName());

    /**
     * The context module which stores the extracted data.
     */
    protected static final ContextModule CONTEXT_MODULE
            = ExecutionContext.getInstance().getModule("JWTUserInformation");

    /**
     * Parse the string representation of a x509 public key and create a usable representation.
     *
     * @param key the string representation of the public key
     * @return a representation that can be used for JWT handling
     */
    public static PublicKey getKey(final String key) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
            X509EncodedKeySpec x509PublicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(x509PublicKey);
        } catch (Exception e) {
            LOGGER.error("Could not read given public key for signature validation.");
        }

        return null;
    }

    /**
     * Initialize the filter.
     *
     * @param filterConfig the filter configuration
     * @throws ServletException in case of errors
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Initialized JWTUserDataFilter.");
    }

    /**
     * Do the acual filtering. This includes decrypting the JWT of the request and extracting
     * username and roles which will be stored in the corresponding context module.
     * If no JWT is available or it cannot be parsed, special 'unknown' values are stored in the
     * context module.
     *
     * @param servletRequest  the request
     * @param servletResponse the response
     * @param filterChain     the filter chain
     * @throws IOException      in case of I/O errors
     * @throws ServletException in case of servlet errors
     */
    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String authString = httpRequest.getHeader("Authorization");
        String jwtString = authString;
        String signingKey = retrieveJwtSigningKey();
        PublicKey key = getKey(signingKey);
        boolean error = false;
        // check for empty authorization
        if (authString == null || authString.isEmpty()) {
            LOGGER.warn("Received empty/null authorization instead of JWT.");
            invalidateUserData();
            error = true;
        }

        if (signingKey == null || signingKey.isEmpty()) {
            LOGGER.warn("No signing key for validating the authorization.");
            invalidateUserData();
            error = true;
        }

        if (key == null) {
            invalidateUserData();
            error = true;
        }

        if (!error) {
            if (authString.startsWith("Bearer")) {
                jwtString = authString.split(" ")[1];
            }

            Jws<Claims> jws;
            try {
                jws = Jwts.parser()
                        .setSigningKey(key)
                        .parseClaimsJws(jwtString);

                // we can safely trust the JWT
                LOGGER.info("Received valid authorization.");
                String userName = jws.getBody().get("preferred_username", String.class);
                ArrayList<String> rolesList = (ArrayList<String>) jws.getBody()
                        .get("realm_access", LinkedHashMap.class).get("roles");
                String rolesString = "";
                for (String role : rolesList) {
                    rolesString += role + ",";
                }
                rolesString = rolesString.substring(0, rolesString.length() - 1);

                updateUserData(userName, rolesString);
                /*
                 FIXME: The code currently ignores (e.g.) time constraints regarding the
                   validity of the JWT
                   This is only tested for keycloak and may break for other software
                    since keycloak stores username not like specified in RFC7519
                 */
            } catch (JwtException ex) {
                LOGGER.warn("Could not validate authorization with known key.");
                invalidateUserData();
            }
        }
        // propagate the request to other endpoints
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Destroy the filter.
     */
    @Override
    public void destroy() {
        LOGGER.info("Destructing JWTUserDataFilter.");
    }

    /**
     * In case there is an error during JWT processing or no JWT at all, the values will be
     * reset to indicate these situation.
     */
    private void invalidateUserData() {
        ((ReadWriteEntity) CONTEXT_MODULE.getContextEntity("username"))
                .write(ContextModule.NO_VALUE);
        ((ReadWriteEntity) CONTEXT_MODULE.getContextEntity("userroles"))
                .write(ContextModule.NO_VALUE);
    }

    /**
     * Write new values for user name and user roles to the context module.
     *
     * @param userName new user name
     * @param userRoles new user roles
     */
    private void updateUserData(final String userName, final String userRoles) {
        ((ReadWriteEntity) CONTEXT_MODULE.getContextEntity("username")).write(userName);
        ((ReadWriteEntity) CONTEXT_MODULE.getContextEntity("userroles")).write(userRoles);
    }

    /**
     * Get the JWT signing key from the execution context.
     *
     * @return the used JWT signing key, in string representation
     */
    public String retrieveJwtSigningKey() {
        return (String) ((ReadOnlyEntity) ExecutionContext.getInstance()
                .getModule("JWTUserInformation")
                .getContextEntity("jwtSigningKey")).read();
    }

}

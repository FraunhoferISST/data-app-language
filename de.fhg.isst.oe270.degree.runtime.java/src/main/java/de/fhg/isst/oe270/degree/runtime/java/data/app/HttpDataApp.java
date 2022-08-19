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
package de.fhg.isst.oe270.degree.runtime.java.data.app;

import de.fhg.isst.oe270.degree.activities.execution.OutputScope;
import de.fhg.isst.oe270.degree.runtime.java.context.ExecutionContext;
import de.fhg.isst.oe270.degree.runtime.java.context.entities.ReadOnlyEntity;
import de.fhg.isst.oe270.degree.runtime.java.data.app.execution.Executor;
import de.fhg.isst.oe270.degree.runtime.java.data.app.http.JWTUserDataFilter;
import nukleus.core.Identifier;
import nukleus.core.Instance;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is a D° application which offers a HTTP-interface.
 */
@RestController
@EnableAutoConfiguration
public abstract class HttpDataApp extends TcpIpDataApp {

    /**
     * This key is used to identify the URL path item within the configuration map.
     */
    public static final String URL_KEY = "url";

    /**
     * This key is used to identify the JWT signing key item within the configuration map.
     */
    public static final String JWT_KEY = "JwtSigningKey";

    /**
     * Time in ms to wait for query results before the query is aborted.
     */
    @SuppressWarnings("unused")
    protected static final long QUERY_TIMEOUT = 10000L;

    /**
     * @see CliDataApp#isInitialized
     */
    private static boolean isHttpInitialized = false;

    /**
     * Map of all {@link Executor}s which are currently running.
     */
    private final Map<UUID, Executor> currentExecutions = new HashMap<>();

    /**
     * Shortcut to create an {@link OutputScope} with a single uuid.
     *
     * @param uuid The used UUID
     * @return An {@link OutputScope} which contains the given uuid as UUID-{@link Instance}
     */
    protected static OutputScope createUuidOutputScope(final String uuid) {
        OutputScope r = new OutputScope();

        Instance e = TYPE_TAXONOMY.create(new Identifier("core.UUID"));
        e.write(uuid);

        r.getValues().put("identifier", e);

        return r;
    }

    /**
     * Get url path of this Data App from Data App configuration.
     *
     * @return The url path of this Data App, or an empty string if unknown.
     */
    public String getPath() {
        if (!CONFIGURATION_MAP.containsKey(URL_KEY)) {
            logWarn("Could not resolve URL path for this Data App.");
            return "";
        }
        return CONFIGURATION_MAP.get(URL_KEY);
    }

    /**
     * Get url path of this Data App from Data App configuration.
     *
     * @return The url path of this Data App, or an empty string if unknown.
     */
    public String getJwtSigningKey() {
        if (!CONFIGURATION_MAP.containsKey(JWT_KEY)) {
            logInfo("There is no JWT signing key for this Data App.");
            return "";
        }
        return CONFIGURATION_MAP.get(JWT_KEY);
    }

    /**
     * Register the {@link JWTUserDataFilter} for this application.
     *
     * @return the filter registration for the JWT user data filter
     */
    @Bean
    public FilterRegistrationBean<JWTUserDataFilter> loggingFilter() {
        FilterRegistrationBean<JWTUserDataFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new JWTUserDataFilter());
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

    /**
     * Initialize the application.
     */
    @Override
    protected void init() {
        super.init();
        synchronized (HttpDataApp.class) {
            if (isHttpInitialized) {
                return;
            }
            isHttpInitialized = true;
        }
        try {
            createMappings();

            addTag("HTTP_DATA_APP");
            getExecutionContext().getModule("JWTUserInformation")
                    .addContextEntity("jwtSigningKey",
                            new ReadOnlyEntity("jwtSigningKey", getJwtSigningKey()));
        } catch (Exception e) {
            LOGGER.error("Error during initialization of HTTP data app.", e);
        }
    }

    /**
     * This function creates mappings within the execution context to ensure
     * that correct modules are used where multiple alternatives are available
     * (e.g. UserInformation).
     */
    private void createMappings() {
        ExecutionContext executionContext = ExecutionContext.getInstance();
        executionContext.changeMappingEntry("UserInformation", "JWTUserInformation");

        executionContext.getModule("JWTUserInformation").removeContextEntity("jwtSigningKey");
    }

}

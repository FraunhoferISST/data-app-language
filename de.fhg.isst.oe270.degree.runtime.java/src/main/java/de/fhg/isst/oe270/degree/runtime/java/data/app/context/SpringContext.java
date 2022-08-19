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
package de.fhg.isst.oe270.degree.runtime.java.data.app.context;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This is the global spring context, which is part of every D° application.
 * It allows to access elements from the spring context for non-spring components.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringContext implements ApplicationContextAware {

    /**
     * If this value is set to true, all getBean methods return null.
     */
    private static boolean compileMode = false;

    /**
     * The used logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("SpringContext");

    /**
     * The spring application context that is made accessible by this class.
     */
    private static ApplicationContext context;

    /**
     * Create spring context.
     */
    public SpringContext() {
        LOGGER.debug("Created SpringContext accessor util.");
    }

    /**
     * Returns the Spring managed bean instance of the given class type if it exists.
     * Returns null otherwise.
     *
     * @param beanClass the class of which a managed bean is wanted
     * @param <T> type of the expected bean
     * @return the found managed spring bean
     * @throws org.springframework.beans.BeansException in case of errors
     */
    public static <T> T getBean(final Class<T> beanClass) {
        if (compileMode) {
            return null;
        }
        LOGGER.debug("Get Bean: " + beanClass.getSimpleName());
        return context.getBean(beanClass);
    }

    /**
     * Returns the Spring managed bean instance of the given class name if it exists.
     * Returns null otherwise.
     *
     * @param name classname of the bean
     * @param <T> type of the expected bean
     * @return the found managed spring bean
     * @throws org.springframework.beans.BeansException in case of errors
     */
    public static <T> T getBean(final String name) {
        if (compileMode) {
            return null;
        }
        LOGGER.debug("Get Bean: " + name);
        return (T) context.getBean(name);
    }

    /**
     * Set the application context.
     *
     * @param ctx the spring application context
     */
    @Override
    public void setApplicationContext(@NotNull final ApplicationContext ctx) {
        LOGGER.debug("Spring context is now available.");
        context = ctx;
    }

    /**
     * Get the compile mode.
     *
     * @return the current compile mode
     * @see SpringContext#compileMode
     */
    public static boolean isCompileMode() {
        return compileMode;
    }

    /**
     * Set the compile mode.
     *
     * @param mode the new value for the compile mode
     * @see SpringContext#compileMode
     */
    public static void setCompileMode(final boolean mode) {
        SpringContext.compileMode = mode;
    }

}

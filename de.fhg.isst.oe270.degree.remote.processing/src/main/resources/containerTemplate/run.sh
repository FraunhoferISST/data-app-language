#!/bin/sh

echo "Starting Data App"
# Avoiding JVM Delays Caused by Random Number Generation
ARGS="-Djava.security.egd=file:/dev/./urandom"

APP_PARAMETERS=""

# Check for port settings
if [ -n "${app_port}" ] ; then
    APP_PARAMETERS="$--Dserver.port=${app_port}"
fi
# Start suspended in debug mode if debug port is set
if [ -n "${JAVA_DEBUG_PORT}" ] ; then
    ARGS="${ARGS} -Xdebug -Xrunjdwp:transport=dt_socket,address=${JAVA_DEBUG_PORT},server=y,suspend=y"
fi
# Add proxy args if set
if [ ! -z "$PROXY_HOST" ]; then
    ARGS="${ARGS} -Dhttp.proxyHost=${PROXY_HOST} -Dhttp.proxyPort=${PROXY_PORT}"
    if [ ! -z "$PROXY_USER" ]; then
        ARGS="${ARGS} -Dhttp.proxyUser=${PROXY_USER}"
    fi
    if [ ! -z "$PROXY_PASS" ]; then
        ARGS="${ARGS} -Dhttp.proxyPassword=${PROXY_PASS}"
    fi
fi

#echo "JVM-arguments for Data App = ${ARGS}"
echo "Command for starting the Data App = java -jar -Xms2048m -Xmx2048m -Dfile.encoding=UTF-8 ${ARGS} ${APP_PARAMETERS}" /app/dataApp.jar
# start data provider in background
java -jar -Xms2048m -Xmx2048m -Dfile.encoding=UTF-8 "${ARGS}" "${APP_PARAMETERS}" /app/dataApp.jar

echo "Data App terminated"

exit 0;

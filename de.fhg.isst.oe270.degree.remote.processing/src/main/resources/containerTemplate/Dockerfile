FROM openjdk:8-jre-alpine

# inject proxy settings as build parameters
ARG http_proxy
ARG https_proxy
ARG app_port

# Set proxy environment variables
ENV http_proxy ${http_proxy}
ENV https_proxy ${https_proxy}
ENV app_port ${app_port}
ENV no_proxy localhost, 127.0.0.1

# Copy the app & startscript
COPY dataApp.jar /app/dataApp.jar
COPY run.sh /app/run.sh

# set file permissions
RUN chmod +x /app/dataApp.jar
RUN chmod +x /app/run.sh

ENTRYPOINT ["/app/run.sh"]
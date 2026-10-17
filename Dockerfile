FROM eclipse-temurin:25-alpine

RUN apk add --no-cache curl

WORKDIR /app

ENV SERVER_PORT=8080
# Structured console log format (ecs, gelf, logstash); set to empty for the plain text pattern layout
ENV LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs

EXPOSE 8080

# Release version of the jar to copy; leave unset for a local build, where the jar has no version in its name
ARG VERSION

COPY ./build/libs/sample-coupon-service${VERSION:+-$VERSION}-exec.jar /app/sample-coupon-service.jar

ENV JAVA_OPTS_DEFAULT="\
-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-XX:+ExitOnOutOfMemoryError \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/app/heapdump"

ENV JAVA_OPTS_EXTRA=""

ENV APP_ARGS=""

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

CMD ["/bin/sh", "-c", "\
if [ -z \"$LOGGING_STRUCTURED_FORMAT_CONSOLE\" ]; then \
  unset LOGGING_STRUCTURED_FORMAT_CONSOLE; \
fi; \
exec java $JAVA_OPTS_DEFAULT $JAVA_OPTS_EXTRA -jar /app/sample-coupon-service.jar $APP_ARGS"]

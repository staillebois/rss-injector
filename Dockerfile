FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS builder

USER root
RUN mkdir /build
ADD . /build/
WORKDIR /build
RUN ./mvnw package
# RUN ./mvnw package -Dnative

FROM registry.access.redhat.com/ubi8/openjdk-21:1.20

ENV LANGUAGE='en_US:en'


# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 --from=builder build/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=builder build/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=builder build/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=builder build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]

# FROM quay.io/quarkus/quarkus-micro-image:2.0
# WORKDIR /work/
# RUN chown 1001 /work \
#     && chmod "g+rwX" /work \
#     && chown 1001:root /work
# COPY --chown=1001:root --from=builder build/target/*-runner /work/application

# EXPOSE 8080
# USER 1001

# ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
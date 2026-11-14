FROM registry.redhat.io/ubi9/openjdk-21:1.24-3.1794341133 AS build

USER root
RUN microdnf install -y git && microdnf clean all

USER 185
WORKDIR /home/default

# Changes when main moves, so the clone is not reused from an older commit.
ADD https://api.github.com/repos/yimu187/pharmkit/commits/main /tmp/pharmkit-main.json
RUN git clone --depth 1 --branch main https://github.com/yimu187/pharmkit.git source

WORKDIR /home/default/source/backend
RUN mvn -B -DskipTests package

FROM registry.redhat.io/ubi9/openjdk-21-runtime:1.24

COPY --from=build --chown=185:0 /home/default/source/backend/target/backend-*.jar /deployments/app.jar
EXPOSE 8080
ENV JAVA_APP_JAR=app.jar

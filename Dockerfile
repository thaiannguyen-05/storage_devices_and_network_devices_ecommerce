FROM eclipse-temurin:17-jdk-jammy AS glassfish-base

ARG GLASSFISH_VERSION=7.0.12
ENV GLASSFISH_HOME=/opt/glassfish7/glassfish

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/${GLASSFISH_VERSION}/glassfish-${GLASSFISH_VERSION}.zip" -o /tmp/glassfish.zip \
    && unzip /tmp/glassfish.zip -d /opt \
    && rm /tmp/glassfish.zip

FROM glassfish-base AS builder

RUN apt-get update \
    && apt-get install -y --no-install-recommends ant \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY build.xml ./
COPY nbproject ./nbproject
COPY lib ./lib
COPY src ./src
COPY web ./web

RUN ant clean dist -Dj2ee.server.home=${GLASSFISH_HOME} -Dplatforms.JDK_17.home=${JAVA_HOME}

FROM glassfish-base AS runtime

ENV DOMAIN_NAME=domain1 \
    PORT=8080

COPY --from=builder /app/dist/Ecommerce.war ${GLASSFISH_HOME}/domains/${DOMAIN_NAME}/autodeploy/Ecommerce.war

EXPOSE 8080

CMD ["/opt/glassfish7/glassfish/bin/asadmin", "start-domain", "--verbose", "domain1"]


FROM eclipse-temurin:17-jdk-jammy AS builder

ARG GLASSFISH_VERSION=6.1.0
ENV GLASSFISH_HOME=/opt/glassfish \
    PATH="/opt/apache-ant/bin:${PATH}"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.14-bin.zip" -o /tmp/ant.zip \
    && unzip /tmp/ant.zip -d /opt \
    && ln -s /opt/apache-ant-1.10.14 /opt/apache-ant \
    && rm /tmp/ant.zip

RUN curl -fsSL "https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/${GLASSFISH_VERSION}/glassfish-${GLASSFISH_VERSION}.zip" -o /tmp/glassfish.zip \
    && unzip /tmp/glassfish.zip -d /opt \
    && rm /tmp/glassfish.zip \
    && ln -s /opt/glassfish6/glassfish /opt/glassfish

WORKDIR /app
COPY . .

RUN ant -f build.xml clean dist -Dj2ee.server.home=${GLASSFISH_HOME} -Dplatforms.JDK_17.home=${JAVA_HOME}

FROM eclipse-temurin:17-jre-jammy

ARG GLASSFISH_VERSION=6.1.0
ENV GLASSFISH_HOME=/opt/glassfish \
    DOMAIN_NAME=domain1 \
    PORT=8080

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/${GLASSFISH_VERSION}/glassfish-${GLASSFISH_VERSION}.zip" -o /tmp/glassfish.zip \
    && unzip /tmp/glassfish.zip -d /opt \
    && rm /tmp/glassfish.zip \
    && ln -s /opt/glassfish6/glassfish /opt/glassfish

COPY --from=builder /app/dist/Ecommerce.war /opt/Ecommerce.war

RUN ${GLASSFISH_HOME}/bin/asadmin start-domain ${DOMAIN_NAME} \
    && ${GLASSFISH_HOME}/bin/asadmin deploy --force=true --name Ecommerce /opt/Ecommerce.war \
    && ${GLASSFISH_HOME}/bin/asadmin stop-domain ${DOMAIN_NAME}

EXPOSE 8080

CMD ["/opt/glassfish/bin/asadmin", "start-domain", "--verbose", "domain1"]

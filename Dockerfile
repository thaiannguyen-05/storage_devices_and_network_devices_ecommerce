FROM eclipse-temurin:17-jdk-jammy AS builder

ARG GLASSFISH_VERSION=7.0.12
ENV GLASSFISH_HOME=/opt/glassfish \
    PATH="/opt/apache-ant/bin:${PATH}"

ADD https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.14-bin.zip /tmp/ant.zip
RUN cd /opt \
    && jar xf /tmp/ant.zip \
    && ln -s /opt/apache-ant-1.10.14 /opt/apache-ant \
    && rm /tmp/ant.zip

ADD https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/${GLASSFISH_VERSION}/glassfish-${GLASSFISH_VERSION}.zip /tmp/glassfish.zip
RUN cd /opt \
    && jar xf /tmp/glassfish.zip \
    && rm /tmp/glassfish.zip \
    && ln -s /opt/glassfish7/glassfish /opt/glassfish

WORKDIR /app
COPY . .

RUN ant -f build.xml clean dist -Dj2ee.server.home=${GLASSFISH_HOME} -Dplatforms.JDK_17.home=${JAVA_HOME}

FROM eclipse-temurin:17-jdk-jammy

ENV GLASSFISH_HOME=/opt/glassfish7/glassfish \
    DOMAIN_NAME=domain1 \
    PORT=8080

COPY --from=builder /opt/glassfish7 /opt/glassfish7
COPY --from=builder /app/dist/Ecommerce.war ${GLASSFISH_HOME}/domains/${DOMAIN_NAME}/autodeploy/Ecommerce.war

EXPOSE 8080

CMD ["/opt/glassfish7/glassfish/bin/asadmin", "start-domain", "--verbose", "domain1"]

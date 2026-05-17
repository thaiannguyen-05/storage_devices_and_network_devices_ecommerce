FROM eclipse-temurin:17-jdk-jammy AS builder

ARG GLASSFISH_VERSION=7.0.12
ENV GLASSFISH_HOME=/opt/glassfish \
    PATH="/opt/apache-ant/bin:${PATH}"

ADD https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.14-bin.zip /tmp/ant.zip
RUN cd /opt \
    && jar xf /tmp/ant.zip \
    && find /opt/apache-ant-1.10.14/bin -type f -exec chmod +x {} \; \
    && ln -s /opt/apache-ant-1.10.14 /opt/apache-ant \
    && rm /tmp/ant.zip

ADD https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/${GLASSFISH_VERSION}/glassfish-${GLASSFISH_VERSION}.zip /tmp/glassfish.zip
RUN cd /opt \
    && jar xf /tmp/glassfish.zip \
    && find /opt/glassfish7/glassfish/bin -type f -exec chmod +x {} \; \
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


RUN cat > /tmp/gf-jvm-options.xml <<'EOF'
        <!-- VPS JVM options -->
        <jvm-options>--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED</jvm-options>
        <jvm-options>--add-opens=java.base/sun.net.www.protocol.jrt=ALL-UNNAMED</jvm-options>
        <jvm-options>--add-opens=java.base/java.lang=ALL-UNNAMED</jvm-options>
        <jvm-options>--add-opens=java.base/java.util=ALL-UNNAMED</jvm-options>
        <jvm-options>--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED</jvm-options>
        <jvm-options>-Djdk.tls.rejectClientInitiatedRenegotiation=true</jvm-options>
        <jvm-options>-Djavax.management.builder.initial=com.sun.enterprise.v3.admin.AppServerMBeanServerBuilder</jvm-options>
        <jvm-options>-Dosgi.shell.telnet.maxconn=1</jvm-options>
        <jvm-options>-Dcom.sun.enterprise.config.config_environment_factory_class=com.sun.enterprise.config.serverbeans.AppserverConfigEnvironmentFactory</jvm-options>
        <jvm-options>-DAUTH_RETRY_RANDOM_STATE=1</jvm-options>
        <jvm-options>-DAUTH_RESET_CODE_TTL_MINUTES=10</jvm-options>
        <jvm-options>-Dorg.glassfish.additionalOSGiBundlesToStart=org.apache.felix.shell,org.apache.felix.gogo.runtime,org.apache.felix.gogo.shell,org.apache.felix.gogo.command,org.apache.felix.shell.remote,org.apache.felix.fileinstall</jvm-options>
        <jvm-options>-Djava.security.auth.login.config=${com.sun.aas.instanceRoot}/config/login.conf</jvm-options>
        <jvm-options>-Dfelix.fileinstall.disableConfigSave=false</jvm-options>
        <jvm-options>-Djava.security.policy=${com.sun.aas.instanceRoot}/config/server.policy</jvm-options>
        <jvm-options>-DDB_HOST=ep-withered-meadow-apop5gmt-pooler.c-7.us-east-1.aws.neon.tech</jvm-options>
        <jvm-options>-DDB_PORT=5432</jvm-options>
        <jvm-options>-DDB_NAME=neondb</jvm-options>
        <jvm-options>-DDB_USER=neondb_owner</jvm-options>
        <jvm-options>-DDB_PASSWORD=npg_c0UrYT7QsLpI</jvm-options>
        <jvm-options>-Djavax.xml.accessExternalSchema=all</jvm-options>
        <jvm-options>-Dfelix.fileinstall.log.level=2</jvm-options>
        <jvm-options>-DAUTH_RETRY_MAX_TIME_MS=1000</jvm-options>
        <jvm-options>-Dfelix.fileinstall.poll=5000</jvm-options>
        <jvm-options>-DSMTP_USER=thaiandev05@gmail.com</jvm-options>
        <jvm-options>-Djdbc.drivers=org.apache.derby.jdbc.ClientDriver</jvm-options>
        <jvm-options>-Dfelix.fileinstall.dir=${com.sun.aas.installRoot}/modules/autostart/</jvm-options>
        <jvm-options>-Xbootclasspath/a:${com.sun.aas.installRoot}/lib/grizzly-npn-api.jar</jvm-options>
        <jvm-options>-Xmx512m</jvm-options>
        <jvm-options>-DAUTH_ARGON2_ITERATIONS=3</jvm-options>
        <jvm-options>-Dorg.glassfish.grizzly.nio.multipleUpperBoundsException=true</jvm-options>
        <jvm-options>-DJWT_REFRESH_TOKEN_DAYS=7</jvm-options>
        <jvm-options>-Dfelix.fileinstall.bundles.startTransient=true</jvm-options>
        <jvm-options>-Dcom.ctc.wstx.returnNullForDefaultNamespace=true</jvm-options>
        <jvm-options>-Dosgi.shell.telnet.ip=127.0.0.1</jvm-options>
        <jvm-options>-Djavax.net.ssl.trustStore=${com.sun.aas.instanceRoot}/config/cacerts.jks</jvm-options>
        <jvm-options>-DAUTH_ARGON2_MEMORY_KIB=65536</jvm-options>
        <jvm-options>-XX:NewRatio=2</jvm-options>
        <jvm-options>-Djdk.corba.allowOutputStreamSubclass=true</jvm-options>
        <jvm-options>-DAUTH_ARGON2_PARALLELISM=1</jvm-options>
        <jvm-options>-DAUTH_RETRY_DELAY_MS=200</jvm-options>
        <jvm-options>-Dgosh.args=--nointeractive</jvm-options>
        <jvm-options>-Dfelix.fileinstall.bundles.new.start=true</jvm-options>
        <jvm-options>-DAUTH_RETRY_MAX=3</jvm-options>
        <jvm-options>-DJWT_ACCESS_TOKEN_MINUTES=15</jvm-options>
        <jvm-options>-Dosgi.shell.telnet.port=6666</jvm-options>
        <jvm-options>-Djava.awt.headless=true</jvm-options>
        <jvm-options>-XX:+UnlockDiagnosticVMOptions</jvm-options>
        <jvm-options>--add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED</jvm-options>
        <jvm-options>-Djavax.net.ssl.keyStore=${com.sun.aas.instanceRoot}/config/keystore.jks</jvm-options>
        <jvm-options>-DANTLR_USE_DIRECT_CLASS_LOADING=true</jvm-options>
        <jvm-options>-DSMTP_PASS=gbpf hjou vvor djgf</jvm-options>
        <jvm-options>--add-opens=java.naming/javax.naming.spi=ALL-UNNAMED</jvm-options>
        <jvm-options>-Dcom.sun.enterprise.security.httpsOutboundKeyAlias=s1as</jvm-options>
        <jvm-options>-DJWT_SECRET=asjhdlkasjdlksajdlksajdlksajdlksajdlksajdlksajdas</jvm-options>
        <jvm-options>-DAUTH_VERIFICATION_CODE_LENGTH=6</jvm-options>
EOF
RUN awk 'BEGIN { while ((getline line < "/tmp/gf-jvm-options.xml") > 0) block = block line "\n" } /<\/java-config>/ { printf "%s", block } { print }' "${GLASSFISH_HOME}/domains/${DOMAIN_NAME}/config/domain.xml" > /tmp/domain.xml \
    && mv /tmp/domain.xml "${GLASSFISH_HOME}/domains/${DOMAIN_NAME}/config/domain.xml" \
    && rm /tmp/gf-jvm-options.xml

EXPOSE 8080

CMD ["/opt/glassfish7/glassfish/bin/asadmin", "start-domain", "--verbose", "domain1"]

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

ENV PATH="/opt/apache-ant/bin:${PATH}"

RUN curl -4 -fsSL "https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.14-bin.zip" -o /tmp/ant.zip \
    && unzip /tmp/ant.zip -d /opt \
    && ln -s /opt/apache-ant-1.10.14 /opt/apache-ant \
    && rm /tmp/ant.zip

WORKDIR /app

COPY build.xml ./
COPY nbproject ./nbproject
COPY lib ./lib
COPY src ./src
COPY web ./web

RUN set -eux; \
    echo "=== PaymentService markers ==="; \
    grep -n "private String safe" /app/src/java/module/bussiness/payment/PaymentService.java || true; \
    grep -n "private String signFields" /app/src/java/module/bussiness/payment/PaymentService.java || true; \
    grep -n "private Map<String, Object> body" /app/src/java/module/bussiness/payment/PaymentService.java || true; \
    grep -n "private String normalizePaymentMethod" /app/src/java/module/bussiness/payment/PaymentService.java || true; \
    echo "=== PaymentController markers ==="; \
    grep -n "private void handleSePayQuery" /app/src/java/module/bussiness/payment/PaymentController.java || true; \
    grep -n "private void handleSePayCancel" /app/src/java/module/bussiness/payment/PaymentController.java || true; \
    grep -n "private void handleSePayVoid" /app/src/java/module/bussiness/payment/PaymentController.java || true; \
    grep -n "import module.core.config.ConfigService;" /app/src/java/module/bussiness/payment/PaymentController.java || true; \
    echo "=== Payment file stats ==="; \
    wc -l /app/src/java/module/bussiness/payment/PaymentService.java /app/src/java/module/bussiness/payment/PaymentController.java; \
    head -n 40 /app/src/java/module/bussiness/payment/PaymentService.java; \
    head -n 40 /app/src/java/module/bussiness/payment/PaymentController.java

RUN ant clean dist -Dj2ee.server.home=${GLASSFISH_HOME} -Dplatforms.JDK_17.home=${JAVA_HOME}

FROM glassfish-base AS runtime

ENV DOMAIN_NAME=domain1 \
    PORT=8080

COPY --from=builder /app/dist/Ecommerce.war ${GLASSFISH_HOME}/domains/${DOMAIN_NAME}/autodeploy/Ecommerce.war

EXPOSE 8080

CMD ["/opt/glassfish7/glassfish/bin/asadmin", "start-domain", "--verbose", "domain1"]

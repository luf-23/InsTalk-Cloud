FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
ARG SERVICE

COPY pom.xml ./
COPY instalk-common/pom.xml instalk-common/pom.xml
COPY instalk-infrastructure/pom.xml instalk-infrastructure/pom.xml
COPY instalk-gateway/pom.xml instalk-gateway/pom.xml
COPY instalk-identity-service/pom.xml instalk-identity-service/pom.xml
COPY instalk-social-service/pom.xml instalk-social-service/pom.xml
COPY instalk-ai-service/pom.xml instalk-ai-service/pom.xml
COPY instalk-chat-service/pom.xml instalk-chat-service/pom.xml
RUN mvn -B -ntp dependency:go-offline

COPY . .
RUN mvn -B -ntp -pl ${SERVICE} -am package -DskipTests && \
    mkdir -p /app && \
    cp $(ls ${SERVICE}/target/*.jar | grep -v original | head -n 1) /app/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /app/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

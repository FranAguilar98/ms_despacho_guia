FROM eclipse-temurin:17-jdk AS buildstage
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pom.xml .
COPY src /app/src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=buildstage /app/target/*.jar /app/app.jar
COPY wallet /app/wallet
EXPOSE 8081
CMD ["java", "-jar", "/app/app.jar"]

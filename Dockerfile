# build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy only pom first to cache dependencies
COPY pom.xml .
# download dependencies
RUN mvn -B -q dependency:go-offline

# copy source and build
COPY src ./src
RUN mvn -B -q -DskipTests package

# runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
# adjust jar name if your artifactId/version differ
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

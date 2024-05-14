# 依赖的基础镜像(maven 和 java)
FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/sync_trajectory_backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]
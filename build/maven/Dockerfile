
# Multi stage build Docker file

#Build-Stage
FROM maven:3.8.4-openjdk-17-slim AS build
ARG WORK_DIR
WORKDIR /app

# copy the project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh
COPY ${WORK_DIR}/src ./src

RUN mvn -B -f /app/pom.xml package

#Run-time stage
FROM openjdk:17-jdk-alpine
WORKDIR /opt/egov
COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/
RUN chmod +x /opt/egov/start.sh
CMD ["/opt/egov/start.sh"]
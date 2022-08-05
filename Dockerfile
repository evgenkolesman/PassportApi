FROM openjdk:17-jdk-slim
#ENV M2_HOME=/opt/maven
#ENV export MAVEN_HOME=/opt/maven
#ENV export PATH=/opt/maven/bin:${PATH}
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
#EXPOSE 54333
#VOLUME /home/euhenios/Загрузки/IdeaProjects/PassportApi/docker
ENTRYPOINT ["java", "-jar", "app.jar"]

#
##
## Build stage
##
#FROM maven:3.8-openjdk-17-slim AS build
#COPY src /home/app/src
#COPY pom.xml /home/app
#RUN mvn -f /home/app/pom.xml clean package -DskipTests
#
##
## Package stage
##
#FROM openjdk:17-jdk-slim
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]

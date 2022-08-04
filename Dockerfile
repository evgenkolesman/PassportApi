FROM openjdk:17-jdk-slim
ENV M2_HOME=/opt/maven
ENV export MAVEN_HOME=/opt/maven
ENV export PATH=/opt/maven/bin:${PATH}
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
#EXPOSE 54333
#VOLUME /home/euhenios/Загрузки/IdeaProjects/PassportApi/docker
ENTRYPOINT ["java", "-jar", "app.jar"]

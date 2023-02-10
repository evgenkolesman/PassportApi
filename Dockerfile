FROM maven:3.8.6-openjdk-18
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml package -DskipTests
#COPY --chown=1000:1000 /home/app/target/PassportApi-0.0.1-SNAPSHOT.jar
#ARG JAR_FILE=/home/app/target/PassportApi-0.0.1-SNAPSHOT.jar
#COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/home/app/target/PassportApi-0.0.1-SNAPSHOT.jar"]





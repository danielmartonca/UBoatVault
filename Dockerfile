FROM openjdk:17
COPY target/*.jar UBoatVault.jar
ENTRYPOINT ["java","-jar","\"-Dspring.profiles.active=heroku\"","/UBoatVault.jar"]
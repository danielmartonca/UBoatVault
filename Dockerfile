FROM openjdk:17
COPY target/*.jar UBoatVault.jar
ENTRYPOINT ["java","-jar","/UBoatVault.jar"]
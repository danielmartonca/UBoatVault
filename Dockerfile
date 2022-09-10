FROM openjdk:17
ARG jarFile=/*.jar
COPY target/${jarFile}.jar UBoatVault.jar
ENTRYPOINT ["java","-jar","/UBoatVault.jar"]
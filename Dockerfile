FROM openjdk:11
COPY target/Hyperloader.war Hyperloader.war
ENTRYPOINT ["java", "-jar", "/Hyperloader.war"]
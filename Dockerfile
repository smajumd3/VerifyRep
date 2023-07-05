FROM openjdk:11
LABEL maintainer="javaguides.net"
ADD target/Hyperloader.jar hyperloader.jar
ENTRYPOINT ["java","-jar","hyperloader.jar"]
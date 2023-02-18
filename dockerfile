FROM openjdk:11
COPY target/finance-0.0.1-SNAPSHOT.jar finance.jar
ENTRYPOINT ["java","-jar","finance.jar"]
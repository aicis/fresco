FROM ubuntu:18.04
RUN apt-get update -y
RUN apt-get upgrade -y
RUN apt-get install -y openjdk-8-jdk maven make
WORKDIR /home/fresco
ADD . /home/fresco
RUN mvn clean install -DskipTests

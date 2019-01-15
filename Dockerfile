FROM ubuntu:18.04
RUN apt-get update && apt-get install -y \
 openjdk-11-jdk \
 maven \
 make
WORKDIR /home/fresco
ADD . /home/fresco
RUN mvn clean install -DskipTests

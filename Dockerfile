FROM openjdk:11-jdk
RUN apt-get update && apt-get install -y \
 maven \
 make
WORKDIR /home/fresco
ADD . /home/fresco
RUN mvn clean install -DskipTests

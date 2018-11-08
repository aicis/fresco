#!/bin/sh
JAR=./demos/distance/target/fresco-demo-distance.jar

java -jar $JAR -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz -Dspdz.preprocessingStrategy=DUMMY -x 10 -y 10 > log.txt 2>&1

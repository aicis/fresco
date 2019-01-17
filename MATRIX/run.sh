#!/usr/bin/env bash
# MATRIX assumes the run.sh script is in a dir called "MATRIX" in the root dir  
MAIN_DIR=$(dirname "$0")"/.."
# Point to the jar to run
JAR=$MAIN_DIR/demos/distance/target/fresco-demo-distance.jar 
# MATRIX gives the party id of this party as the first argument, but indexes from 0
PARTY_ID=`expr $1 + 1`
# MATRIX seems to put the party configurarion here
PARTIES_FILE=$MAIN_DIR"/parties.conf"
# Unwrap the MATRIX parties configuration file
PARTIES_STR="-i $1 "
NUM_LINES=$(wc -l < $PARTIES_FILE)
NUM_PARTIES=`expr $NUM_LINES / 2`
for i in $(seq $NUM_PARTIES)
do
    IDX=`expr $i - 1`
    IP_PREFIX="party_"$IDX"_ip="
    PORT_PREFIX="party_"$IDX"_port="
    IP=$(grep $IP_PREFIX $PARTIES_FILE | sed "s/$IP_PREFIX//")
    PORT=$(grep $PORT_PREFIX $PARTIES_FILE | sed "s/$PORT_PREFIX//")
    PARTIES_STR=$PARTIES_STR"-p $i:$IP:$PORT "
done
shift
PARAMS=$@
java -jar $JAR -e SEQUENTIAL_BATCHED $PARTIES_STR -s spdz -Dspdz.preprocessingStrategy=DUMMY $PARAMS > log.txt 2>&1

#java -jar $JAR -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz -Dspdz.preprocessingStrategy=DUMMY > log.txt 2>&1

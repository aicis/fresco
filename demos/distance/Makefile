build: install move

install:
	cd ../../core && mvn clean install -DskipTests
	cd ../../suite/spdz && mvn clean install -DskipTests
	cd ../common && mvn clean install -DskipTests
	mvn clean install -DskipTests

move:
	mkdir -p server1;
	mkdir -p server2;
	cp target/fresco-demo-distance.jar server1;
	cp target/fresco-demo-distance.jar server2;

runSpdz:
	cd server1 && java -jar fresco-demo-distance.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz -Dspdz.preprocessingStrategy=DUMMY -x 10 -y 10 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-distance.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz -Dspdz.preprocessingStrategy=DUMMY -x 20 -y 15 > log.txt 2>&1 &

runDummy:
	cd server1 && java -jar fresco-demo-distance.jar -l -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s dummyArithmetic -x 10 -y 10 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-distance.jar -l -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s dummyArithmetic -x 20 -y 15 > log.txt 2>&1 &

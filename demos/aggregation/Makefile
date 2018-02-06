build: install move

install:
	cd ../../core && mvn clean install -DskipTests
	cd ../../suite/spdz && mvn clean install -DskipTests
	cd ../common && mvn clean install -DskipTests
	mvn clean install -DskipTests

move:
	mkdir -p server1;
	mkdir -p server2;
	cp target/fresco-demo-aggregation.jar server1;
	cp target/fresco-demo-aggregation.jar server2;

runSpdz:
	cd server1 && java -jar fresco-demo-aggregation.jar 1 -e SEQUENTIAL_BATCHED -i 1 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -s spdz -Dspdz.preprocessingStrategy=DUMMY > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-aggregation.jar 2 -e SEQUENTIAL_BATCHED -i 2 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -s spdz -Dspdz.preprocessingStrategy=DUMMY > log.txt 2>&1 &

runDummy:
	cd server1 && java -jar fresco-demo-aggregation.jar 1 -e SEQUENTIAL_BATCHED -i 1 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -s dummyArithmetic > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-aggregation.jar 2 -e SEQUENTIAL_BATCHED -i 2 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -s dummyArithmetic > log.txt 2>&1 &

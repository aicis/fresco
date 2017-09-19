build: install move

install:
	cd ../../core && mvn clean install -DskipTests
	cd ../../suite/spdz && mvn clean install -DskipTests
	cd ../common && mvn clean install -DskipTests
	mvn clean install -DskipTests

move:
	mkdir -p server1;
	mkdir -p server2;
	mkdir -p server3;
	cp target/fresco-demo-sum.jar server1;
	cp target/fresco-demo-sum.jar server2;
	cp target/fresco-demo-sum.jar server3;

runSPDZ:
	cd server1 && java -jar fresco-demo-sum.jar -e SEQUENTIAL_BATCHED -i 1 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -p 3:localhost:8083 -s spdz -Dspdz.preprocessingStrategy=DUMMY > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-sum.jar -e SEQUENTIAL_BATCHED -i 2 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -p 3:localhost:8083 -s spdz -Dspdz.preprocessingStrategy=DUMMY > log.txt 2>&1 &
	cd server3 && java -jar fresco-demo-sum.jar -e SEQUENTIAL_BATCHED -i 3 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -p 3:localhost:8083 -s spdz -Dspdz.preprocessingStrategy=DUMMY > log.txt 2>&1 &

runDummy:
	cd server1 && java -jar fresco-demo-sum.jar -e SEQUENTIAL_BATCHED -i 1 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -p 3:localhost:8083 -s dummyarithmetic > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-sum.jar -e SEQUENTIAL_BATCHED -i 2 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -p 3:localhost:8083 -s dummyarithmetic > log.txt 2>&1 &
	cd server3 && java -jar fresco-demo-sum.jar -e SEQUENTIAL_BATCHED -i 3 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -p 3:localhost:8083 -s dummyarithmetic > log.txt 2>&1 &

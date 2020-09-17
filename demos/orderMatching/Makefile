build: install move

install:
	cd ../../core && mvn clean install -DskipTests
	cd ../../suite/spdz2k && mvn clean install -DskipTests
	cd ../common && mvn clean install -DskipTests
	mvn clean install -DskipTests

move:
	mkdir -p server1;
	mkdir -p server2;
	cp target/fresco-demo-matching.jar server1;
	cp target/fresco-demo-matching.jar server2;

run:
	cd server1 && java -jar fresco-demo-matching.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k -Dspdz2k.preprocessingStrategy=DUMMY &
	cd server2 && java -jar fresco-demo-matching.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k -Dspdz2k.preprocessingStrategy=DUMMY &


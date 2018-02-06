build: install move

install:
	cd ../../core && mvn clean install -DskipTests
	cd ../../suite/tinytables && mvn clean install -DskipTests
	cd ../common && mvn clean install -DskipTests
	mvn clean install -DskipTests

move:
	mkdir -p server1;
	mkdir -p server2;
	cp target/fresco-demo-aes.jar server1;
	cp target/fresco-demo-aes.jar server2;

runPrePro:
	cd server1 && java -jar fresco-demo-aes.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s tinytablesprepro -in 000102030405060708090a0b0c0d0e0f > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-aes.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s tinytablesprepro -in 00112233445566778899aabbccddeeff > log.txt 2>&1 &


run:
	cd server1 && java -jar fresco-demo-aes.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s tinytables -in 000102030405060708090a0b0c0d0e0f > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-aes.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s tinytables -in 00112233445566778899aabbccddeeff > log.txt 2>&1 &

runDummy:
	cd server1 && java -jar fresco-demo-aes.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s dummyBool -in 000102030405060708090a0b0c0d0e0f > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-aes.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s dummyBool -in 00112233445566778899aabbccddeeff > log.txt 2>&1 &

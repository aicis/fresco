build:
	cd .. && mvn clean package -DskipTests -P setintersection && cd -;
	mkdir -p server1;
	mkdir -p server2;
	cp ../target/fresco-demo-setIntersection.jar server1;
	cp ../target/fresco-demo-setIntersection.jar server2;

runPrePro:
	cd server1 && java -jar fresco-demo-setIntersection.jar -e SEQUENTIAL_BATCHED -i 1 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -s tinytablesprepro -vt 1 -in 2,3,4,5,8,9,14 -key abc123abc123abc123abc123abc123ab > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-setIntersection.jar -e SEQUENTIAL_BATCHED -i 2 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -s tinytablesprepro -vt 1 -in 2,3,4,6,7,12,14 -key abc123abc123abc123abc123abc123ab > log.txt 2>&1 &

run:
	cd server1 && java -jar fresco-demo-setIntersection.jar -e SEQUENTIAL_BATCHED -i 1 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -s tinytables -vt 1 -in 2,3,4,5,8,9,14 -key abc123abc123abc123abc123abc123ab > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-setIntersection.jar -e SEQUENTIAL_BATCHED -i 2 -l INFO -p 1:localhost:8081 -p 2:localhost:8082 -s tinytables -vt 1 -in 2,3,4,5,8,9,14 -key abc123abc123abc123abc123abc123ab > log.txt 2>&1 &

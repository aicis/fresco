
To make SPDZ tests work, execute this from project root:

TODO: Tests should automatically generate these as part of test fixture.

mkdir -p ./triples/spdz2-byte0
mvn compile
mvn exec:java -Dexec.mainClass="dk.alexandra.fresco.suite.spdz.storage.FakeTripGen" -Dexec.args="-t=100000 -i=1000 -b=1000 -p=2 -e=100 -m=6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329 -d=triples/spdz2-byte0"

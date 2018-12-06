package dk.alexandra.fresco.suite.spdz;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
    TestLookUpProtocol.class,
    TestSpdzBasicArithmetic2Parties.class,
    TestSpdzCollections.class,
    TestMaliciousBehaviour.class,
})

public class JunitTestSuite {
}
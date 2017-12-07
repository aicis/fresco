package dk.alexandra.fresco.tools.mascot.mock;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MascotContext;

public class MockMascotContext extends MascotContext {

  public MockMascotContext(Integer myId, List<Integer> partyIds, BigInteger modulus, int kBitLength,
      int lambdaSecurityParameter, Network network, Random rand) {
    super(myId, partyIds, modulus, kBitLength, lambdaSecurityParameter, network, rand);
  }

  public MockMascotContext() {
    this(1, Arrays.asList(1), new BigInteger("1"), 8, 8, new MockNetwork(), new Random());
  }

}

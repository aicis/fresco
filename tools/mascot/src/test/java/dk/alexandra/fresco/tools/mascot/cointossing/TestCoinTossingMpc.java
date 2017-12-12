package dk.alexandra.fresco.tools.mascot.cointossing;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;

public class TestCoinTossingMpc extends NetworkedTest {

  public StrictBitVector singlePartyCoinTossing(MascotContext ctx, StrictBitVector ownSeed) {
    CoinTossingMpc coinTosser = new CoinTossingMpc(ctx);
    return coinTosser.generateJointSeed(ownSeed);
  }

  @Test
  public void twoPartyCoinTossing() {
    initContexts(Arrays.asList(1, 2));

    StrictBitVector seedOne =
        new StrictBitVector(new byte[] {(byte) 0x00, (byte) 0x02, (byte) 0xFF}, 24);
    StrictBitVector seedTwo =
        new StrictBitVector(new byte[] {(byte) 0xF0, (byte) 0x02, (byte) 0xF0}, 24);

    Callable<StrictBitVector> partyOneTask = () -> singlePartyCoinTossing(contexts.get(1), seedOne);
    Callable<StrictBitVector> partyTwoTask = () -> singlePartyCoinTossing(contexts.get(2), seedTwo);

    List<StrictBitVector> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    StrictBitVector left = results.get(0);
    StrictBitVector right = results.get(1);
    assertEquals(left, right);

    StrictBitVector expected =
        new StrictBitVector(new byte[] {(byte) 0xF0, (byte) 0x00, (byte) 0x0F}, 24);
    assertEquals(expected, left);
  }

}
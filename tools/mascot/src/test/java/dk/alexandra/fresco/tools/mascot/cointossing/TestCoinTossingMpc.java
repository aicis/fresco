package dk.alexandra.fresco.tools.mascot.cointossing;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

public class TestCoinTossingMpc extends NetworkedTest {

  public StrictBitVector singlePartyCoinTossing(MascotTestContext ctx, StrictBitVector ownSeed) {
    CoinTossingMpc coinTosser = new CoinTossingMpc(ctx.getResourcePool(), ctx.getNetwork());
    return coinTosser.generateJointSeed(ownSeed);
  }

  @Test
  public void twoPartyCoinTossing() {
    initContexts(Arrays.asList(1, 2));

    StrictBitVector seedOne =
        new StrictBitVector(new byte[]{(byte) 0x00, (byte) 0x02, (byte) 0xFF});
    StrictBitVector seedTwo =
        new StrictBitVector(new byte[]{(byte) 0xF0, (byte) 0x02, (byte) 0xF0});

    Callable<StrictBitVector> partyOneTask = () -> singlePartyCoinTossing(contexts.get(1), seedOne);
    Callable<StrictBitVector> partyTwoTask = () -> singlePartyCoinTossing(contexts.get(2), seedTwo);

    List<StrictBitVector> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    StrictBitVector left = results.get(0);
    StrictBitVector right = results.get(1);
    assertEquals(left, right);

    StrictBitVector expected =
        new StrictBitVector(new byte[]{(byte) 0xF0, (byte) 0x00,
            (byte) 0x0F});
    assertEquals(expected, left);
  }

  @Test
  public void threePartyCoinTossing() {
    initContexts(Arrays.asList(1, 2, 3));

    StrictBitVector seedOne =
        new StrictBitVector(new byte[]{(byte) 0x00, (byte) 0x02, (byte) 0xFF});
    StrictBitVector seedTwo =
        new StrictBitVector(new byte[]{(byte) 0xF0, (byte) 0x02, (byte) 0xF0});
    StrictBitVector seedThree =
        new StrictBitVector(new byte[]{(byte) 0xF0, (byte) 0x02, (byte) 0xF0});

    Callable<StrictBitVector> partyOneTask = () -> singlePartyCoinTossing(contexts.get(1), seedOne);
    Callable<StrictBitVector> partyTwoTask = () -> singlePartyCoinTossing(contexts.get(2), seedTwo);
    Callable<StrictBitVector> partyThreeTask = () -> singlePartyCoinTossing(contexts.get(3),
        seedThree);

    List<StrictBitVector> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));

    StrictBitVector partyOneOut = results.get(0);
    StrictBitVector partyTwoOut = results.get(1);
    StrictBitVector partyThreeOut = results.get(2);
    assertEquals(partyOneOut, partyTwoOut);
    assertEquals(partyTwoOut, partyThreeOut);

    StrictBitVector expected =
        new StrictBitVector(new byte[]{(byte) 0x00, (byte) 0x02, (byte) 0xFF});
    assertEquals(expected, partyOneOut);
  }

}

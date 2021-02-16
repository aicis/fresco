package dk.alexandra.fresco.tools.bitTriples.bracket;

import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTriplesTestContext;
import dk.alexandra.fresco.tools.bitTriples.NetworkedTest;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrgImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.Test;

public class BracketTest extends NetworkedTest {


  private List<StrictBitVector> runBracket(BitTriplesTestContext bitTriplesTestContext) {
    BytePrgImpl jointSampler = new BytePrgImpl(new StrictBitVector(bitTriplesTestContext.getPrgSeedLength()));
    Bracket bracket =
        new Bracket(bitTriplesTestContext.getResourcePool(), bitTriplesTestContext.getNetwork(),jointSampler);
    return bracket.input(getParameters().getComputationalSecurityBitParameter());
  }

  public List<StrictBitVector> runShare(BitTriplesTestContext bitTriplesTestContext, StrictBitVector mac) {
    BytePrgImpl jointSampler = new BytePrgImpl(new StrictBitVector(bitTriplesTestContext.getPrgSeedLength()));
    Bracket bracket =
        new Bracket(bitTriplesTestContext.getResourcePool(), bitTriplesTestContext.getNetwork(), mac,jointSampler);
    List<StrictBitVector> result;
    StrictBitVector randomShares =
        new StrictBitVector(
            getParameters().getComputationalSecurityBitParameter(),
            bitTriplesTestContext.getResourcePool().getRandomGenerator());
    result = bracket.share(1,randomShares);
    return result;
  }

  public List<StrictBitVector> runNShare(BitTriplesTestContext bitTriplesTestContext, StrictBitVector mac) {
    BytePrgImpl jointSampler = new BytePrgImpl(new StrictBitVector(bitTriplesTestContext.getPrgSeedLength()));
    Bracket bracket =
        new Bracket(bitTriplesTestContext.getResourcePool(), bitTriplesTestContext.getNetwork(), mac,jointSampler);
    List<StrictBitVector> result;
    StrictBitVector randomShares =
        new StrictBitVector(
            getParameters().getComputationalSecurityBitParameter(),
            bitTriplesTestContext.getResourcePool().getRandomGenerator());
    result = bracket.nShare(randomShares);
    return result;
  }

  @Test
  public void bracketWithTwoParties() {
    initContexts(2);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    tasks.add(() -> runBracket(contexts.get(1)));
    tasks.add(() -> runBracket(contexts.get(2)));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void bracketWithThreeParties() {
    initContexts(3);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    tasks.add(() -> runBracket(contexts.get(1)));
    tasks.add(() -> runBracket(contexts.get(2)));
    tasks.add(() -> runBracket(contexts.get(3)));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void bracketWithFourParties() {
    initContexts(4);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    tasks.add(() -> runBracket(contexts.get(1)));
    tasks.add(() -> runBracket(contexts.get(2)));
    tasks.add(() -> runBracket(contexts.get(3)));
    tasks.add(() -> runBracket(contexts.get(4)));

    testRuntime.runPerPartyTasks(tasks);
  }


  @Test
  public void nShareProduceCorrectSharesTwoParties() {
    initContexts(2);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    StrictBitVector macP1 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP2 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    tasks.add(() -> runNShare(contexts.get(1),macP1));
    tasks.add(() -> runNShare(contexts.get(2),macP2));

    List<List<StrictBitVector>> results = testRuntime.runPerPartyTasks(tasks);
    macP1.xor(macP2);
    Assert.assertTrue(checkResultOfShare(results,results.get(0).size(),macP1));
  }

  @Test
  public void nShareProduceCorrectSharesThreeParties() {
    initContexts(3);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    StrictBitVector macP1 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP2 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP3 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    tasks.add(() -> runNShare(contexts.get(1),macP1));
    tasks.add(() -> runNShare(contexts.get(2),macP2));
    tasks.add(() -> runNShare(contexts.get(3),macP3));

    List<List<StrictBitVector>> results = testRuntime.runPerPartyTasks(tasks);
    macP1.xor(macP2);
    macP1.xor(macP3);
    Assert.assertTrue(checkResultOfShare(results,results.get(0).size(),macP1));
  }

  @Test
  public void nShareProduceCorrectSharesFourParties() {
    initContexts(4);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    StrictBitVector macP1 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP2 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP3 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP4 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    tasks.add(() -> runNShare(contexts.get(1),macP1));
    tasks.add(() -> runNShare(contexts.get(2),macP2));
    tasks.add(() -> runNShare(contexts.get(3),macP3));
    tasks.add(() -> runNShare(contexts.get(4),macP4));

    List<List<StrictBitVector>> results = testRuntime.runPerPartyTasks(tasks);
    macP1.xor(macP2);
    macP1.xor(macP3);
    macP1.xor(macP4);
    Assert.assertTrue(checkResultOfShare(results,results.get(0).size(),macP1));
  }

  @Test
  public void shareProduceCorrectSharesTwoParties() {
    initContexts(2);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    StrictBitVector macP1 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP2 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    tasks.add(() -> runShare(contexts.get(1),macP1));
    tasks.add(() -> runShare(contexts.get(2),macP2));

    List<List<StrictBitVector>> results = testRuntime.runPerPartyTasks(tasks);
    macP1.xor(macP2);
    Assert.assertTrue(checkResultOfShare(results,results.get(0).size(),macP1));
  }

  @Test
  public void shareProduceCorrectSharesThreeParties() {
    initContexts(3);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    StrictBitVector macP1 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP2 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP3 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    tasks.add(() -> runShare(contexts.get(1),macP1));
    tasks.add(() -> runShare(contexts.get(2),macP2));
    tasks.add(() -> runShare(contexts.get(3),macP3));

    List<List<StrictBitVector>> results = testRuntime.runPerPartyTasks(tasks);
    macP1.xor(macP2);
    macP1.xor(macP3);
    Assert.assertTrue(checkResultOfShare(results,results.get(0).size(),macP1));
  }

  @Test
  public void shareProduceCorrectSharesFourParties() {
    initContexts(4);

    List<Callable<List<StrictBitVector>>> tasks = new ArrayList<>();
    StrictBitVector macP1 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP2 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP3 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    StrictBitVector macP4 =
        constructRandomMac(getParameters().getComputationalSecurityBitParameter());
    tasks.add(() -> runShare(contexts.get(1),macP1));
    tasks.add(() -> runShare(contexts.get(2),macP2));
    tasks.add(() -> runShare(contexts.get(3),macP3));
    tasks.add(() -> runShare(contexts.get(4),macP4));

    List<List<StrictBitVector>> results = testRuntime.runPerPartyTasks(tasks);
    macP1.xor(macP2);
    macP1.xor(macP3);
    macP1.xor(macP4);
    Assert.assertTrue(checkResultOfShare(results,results.get(0).size(),macP1));
  }

  private boolean checkResultOfShare(List<List<StrictBitVector>> result, int length, StrictBitVector sharedMac){
    for (int i = 0; i < length; i++){
      StrictBitVector compare = new StrictBitVector(result.get(0).get(i).toByteArray());
      for (int j = 1; j < result.size(); j++){
        compare.xor(result.get(j).get(i));
      }
      if(!(compare.equals(sharedMac) || compare.equals(new StrictBitVector(sharedMac.getSize())))){
        return false;
      }
    }
    return true;
  }

  private StrictBitVector constructRandomMac(int size) {
    byte[] drbgSeed = new byte[getParameters().getComputationalSecurityBitParameter()];
    Drbg drbg = AesCtrDrbgFactory.fromDerivedSeed(drbgSeed);
    return new StrictBitVector(size,drbg);
  }
}

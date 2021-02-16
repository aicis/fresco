package dk.alexandra.fresco.tools.bitTriples.bracket;

import dk.alexandra.fresco.framework.MaliciousException;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestMacCheckSharesNetworked extends NetworkedTest {

  private StrictBitVector publicValues;
  private List<StrictBitVector> privateMacs;
  private List<List<StrictBitVector>> macShares;


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private void initiateParameters(int noOfParties) {
    int h = parameters.getPrgSeedBitLength();
    byte[] drbgSeed = new byte[h];
    Drbg drbg = AesCtrDrbgFactory.fromDerivedSeed(drbgSeed);

    publicValues = new StrictBitVector(h ,drbg);

    privateMacs = new ArrayList<>();
    StrictBitVector mac = new StrictBitVector(h);
    for (int i = 0; i < noOfParties; i++) {
      StrictBitVector partyMac = new StrictBitVector(h, drbg);
      if (i == 0) {
        mac = new StrictBitVector(partyMac.toByteArray().clone());
      } else {
        mac.xor(partyMac);
      }
      privateMacs.add(partyMac);
    }

    macShares = new ArrayList<>();
    for (int i = 0; i < noOfParties; i++) {
      macShares.add(new ArrayList<>());
    }

    for (int i = 0; i < publicValues.getSize(); i++) {
      StrictBitVector macToShare;
      if (publicValues.getBit(i,false)) {
        macToShare = new StrictBitVector(mac.toByteArray().clone());
      } else {
        macToShare = new StrictBitVector(h);
      }

      for (int j = 0; j < noOfParties - 1; j++) {
        StrictBitVector partyMac = new StrictBitVector(h, drbg);
        macToShare.xor(partyMac);
        macShares.get(j).add(partyMac);
      }
      macShares.get(noOfParties - 1).add(macToShare);
    }
  }

  private List<StrictBitVector> randomMacShares(int noOfVectors) {
    byte[] drbgSeed = new byte[] {10};
    Drbg drbg = AesCtrDrbgFactory.fromDerivedSeed(drbgSeed);
    List<StrictBitVector> macShare = new ArrayList<>();
    for(int i = 0; i<noOfVectors; i++) {
      macShare.add(new StrictBitVector(parameters.getPrgSeedBitLength(),drbg));
    }
    return macShare;
  }

  private boolean runMacCheckShares(BitTriplesTestContext ctx, int partyId) {
    BytePrgImpl jointSampler = new BytePrgImpl(new StrictBitVector(ctx.getPrgSeedLength()));
    MacCheckShares macCheckShares = new MacCheckShares(ctx.getResourcePool(), ctx.getNetwork(), jointSampler);
    return macCheckShares.check(
        publicValues, macShares.get(partyId - 1), privateMacs.get(partyId - 1));
  }

  @Test
  public void checkTwoParties() {
    initContexts(2);
    initiateParameters(2);

    List<Callable<Boolean>> tasks = new ArrayList<>();
    tasks.add(() -> runMacCheckShares(contexts.get(1), 1));
    tasks.add(() -> runMacCheckShares(contexts.get(2), 2));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void checkTwoPartiesMalicious() {
    initContexts(2);
    initiateParameters(2);

    privateMacs.remove(0);
    privateMacs.add(new StrictBitVector(parameters.getPrgSeedBitLength()));

    List<Callable<Boolean>> tasks = new ArrayList<>();
    tasks.add(() -> runMacCheckShares(contexts.get(1), 1));
    tasks.add(() -> runMacCheckShares(contexts.get(2), 2));
    try {
      testRuntime.runPerPartyTasks(tasks);
    } catch (Exception e){
      Assert.assertTrue(e.getCause().getCause() instanceof MaliciousException);
    }
  }

  @Test
  public void checkThreeParties() {
    initContexts(3);
    initiateParameters(3);

    List<Callable<Boolean>> tasks = new ArrayList<>();
    tasks.add(() -> runMacCheckShares(contexts.get(1), 1));
    tasks.add(() -> runMacCheckShares(contexts.get(2), 2));
    tasks.add(() -> runMacCheckShares(contexts.get(3), 3));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void checkFiveParties() {
    initContexts(5);
    initiateParameters(5);

    List<Callable<Boolean>> tasks = new ArrayList<>();
    tasks.add(() -> runMacCheckShares(contexts.get(1), 1));
    tasks.add(() -> runMacCheckShares(contexts.get(2), 2));
    tasks.add(() -> runMacCheckShares(contexts.get(3), 3));
    tasks.add(() -> runMacCheckShares(contexts.get(4), 4));
    tasks.add(() -> runMacCheckShares(contexts.get(5), 5));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void checkWrongShares() {
    initContexts(2);
    initiateParameters(2);

    macShares.remove(1);
    macShares.add(randomMacShares(macShares.get(0).size()));

    List<Callable<Boolean>> tasks = new ArrayList<>();
    tasks.add(() -> runMacCheckShares(contexts.get(1), 1));
    tasks.add(() -> runMacCheckShares(contexts.get(2), 2));
    try {
      testRuntime.runPerPartyTasks(tasks);
    } catch (Exception e){
      Assert.assertTrue(e.getCause().getCause() instanceof MaliciousException);
    }

  }

  @Test
  public void checkWrongLengthOfVector() {
    initContexts(2);
    initiateParameters(2);

    publicValues = new StrictBitVector(parameters.getPrgSeedBitLength()+8);

    List<Callable<Boolean>> tasks = new ArrayList<>();
    tasks.add(() -> runMacCheckShares(contexts.get(1), 1));
    tasks.add(() -> runMacCheckShares(contexts.get(2), 2));
    try {
      testRuntime.runPerPartyTasks(tasks);
    } catch (Exception e){
      Assert.assertTrue(e.getCause().getCause() instanceof IllegalStateException);
    }
  }
}

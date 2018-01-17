package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.suite.spdz.KryoNetManager;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePoolImpl;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.NaorPinkasOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestParallelMascots {

  private int modBitLength;
  private ExecutorService executorService;
  private int prgSeedLength;
  private int numLeftFactors;
  private List<Integer> ports;
  private int noOfParties;
  private BigInteger modulus;
  private List<Integer> partyIds;
  private int iterations;
  private Logger logger = LoggerFactory.getLogger(TestParallelMascots.class);

  @Before
  public void setUp() {
    noOfParties = 2;
    ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(10000 + i * (noOfParties - 1));
    }
    modBitLength = 128;
    executorService = Executors.newCachedThreadPool();
    numLeftFactors = 3;
    prgSeedLength = 256;
    modulus = ModulusFinder.findSuitableModulus(modBitLength);
    partyIds = IntStream.range(1, noOfParties + 1).boxed().collect(Collectors.toList());
    iterations = 3;
  }

  @After
  public void tearDown() {
    executorService.shutdownNow();
  }

  @Test
  public void testConstructorWithOtherModulus() throws Exception {
    modulus = new BigInteger("340282366920938463463374607431768211297");
    constructMascot();
  }

  @Test
  public void testConstructor() throws Exception {
    constructMascot();
  }

  private Map<Integer, RotList> perPartySingleSeedOtSetup(int myId, Drbg drbg, Network network) {
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (Integer otherId : partyIds) {
      if (!otherId.equals(myId)) {
        Ot ot = new DummyOt(otherId, network);
        RotList currentSeedOts = new RotList(drbg, prgSeedLength);
        if (myId < otherId) {
          currentSeedOts.send(ot);
          currentSeedOts.receive(ot);
        } else {
          currentSeedOts.receive(ot);
          currentSeedOts.send(ot);
        }
        seedOts.put(otherId, currentSeedOts);
      }
    }
    return seedOts;
  }

  private List<Map<Integer, RotList>> setupOts() {
    List<Callable<Map<Integer, RotList>>> seedOtTasks = new ArrayList<>();
    KryoNetManager otManager = new KryoNetManager(ports);
    for (int myId = 1; myId <= noOfParties; myId++) {
      int myFinalId = myId;
      Callable<Map<Integer, RotList>> task = () -> perPartySingleSeedOtSetup(myFinalId,
          getDrbg(),
          otManager.createExtraNetwork(myFinalId));
      seedOtTasks.add(task);
    }
    return invokeAndReturn(seedOtTasks);
  }

  private Map<Integer, FieldElement> setupMacKeyShares() {
    Map<Integer, FieldElement> macKeyShares = new HashMap<>();
    for (int myId = 1; myId <= noOfParties; myId++) {
      FieldElement ssk = SpdzMascotDataSupplier
          .createRandomSsk(modulus, modBitLength, prgSeedLength);
      macKeyShares.put(myId, ssk);
    }
    return macKeyShares;
  }

  private Drbg getDrbg() {
    byte[] drbgSeed = new byte[prgSeedLength / 8];
    new SecureRandom().nextBytes(drbgSeed);
    return new PaddingAesCtrDrbg(drbgSeed);
  }

  private void constructMascot() throws Exception {
    List<Map<Integer, RotList>> seedOts = setupOts();
    Map<Integer, FieldElement> perPartyMacKeyShares = setupMacKeyShares();
    List<Callable<Mascot>> mascotCreators = new ArrayList<>();
    for (int i = 0; i < iterations; i++) {
      @SuppressWarnings("resource")
      KryoNetManager normalManager = new KryoNetManager(ports);
      for (int myId = 1; myId <= noOfParties; myId++) {
        FieldElement randomSsk = perPartyMacKeyShares.get(myId);
        int finalMyId = myId;
        int finalInstanceId = i;
        Map<Integer, RotList> seedOt = seedOts.get(finalMyId - 1);
        mascotCreators.add(() -> {
          int lambdaParam = this.modBitLength;
          return new Mascot(new MascotResourcePoolImpl(finalMyId, partyIds, finalInstanceId,
              getDrbg(), seedOt, modulus, modBitLength, lambdaParam, prgSeedLength, numLeftFactors),
              normalManager.createExtraNetwork(finalMyId), randomSsk);
        });
      }
    }
    invoke(mascotCreators);
  }

  @Test
  public void testFirstTriples() throws Exception {
    List<Map<Integer, RotList>> seedOts = setupOts();
    Map<Integer, FieldElement> perPartyMacKeyShares = setupMacKeyShares();
    List<Callable<List<MultTriple>>> mascotCreators = new ArrayList<>();
    for (int i = 0; i < iterations; i++) {
      @SuppressWarnings("resource")
      KryoNetManager normalManager = new KryoNetManager(ports);
      for (int myId = 1; myId <= noOfParties; myId++) {
        int finalMyId = myId;
        int finalInstanceId = i;
        FieldElement randomSsk = perPartyMacKeyShares.get(finalMyId);
        Map<Integer, RotList> seedOt = seedOts.get(finalMyId - 1);
        mascotCreators.add(() -> {
          Mascot mascot = new Mascot(
              new MascotResourcePoolImpl(finalMyId, partyIds, finalInstanceId,
                  getDrbg(), seedOt, modulus, modBitLength, 256, prgSeedLength,
                  numLeftFactors),
              normalManager.createExtraNetwork(finalMyId), randomSsk);
          return mascot.getTriples(16);
        });
      }
    }
    invoke(mascotCreators);
  }

  private <T> List<T> invokeAndReturn(List<Callable<T>> tasks) {
    List<Future<T>> futures = ExceptionConverter
        .safe(() -> executorService.invokeAll(tasks), "Error invoking tasks");
    return futures.stream().map(
        future -> ExceptionConverter.safe(future::get, "Error getting future result"))
        .collect(Collectors.toList());
  }

  private <T> void invoke(List<Callable<T>> mascotCreators) throws Exception {
    List<Future<T>> futures =
        mascotCreators.stream().map((task) -> executorService.submit(task))
            .collect(Collectors.toList());

    while (!futures.isEmpty()) {
      for (Iterator<Future<T>> iterator = futures.iterator(); iterator.hasNext(); ) {
        Future<T> future = iterator.next();
        if (future.isDone()) {
          T result = future.get();
          Assert.assertThat(result, IsNull.notNullValue());
          iterator.remove();
        }
      }
      Thread.sleep(1000);
      logger.info("Testing the remaining " + futures.size() + " futures");
    }

  }
}

package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.suite.spdz.KryoNetManager;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePoolImpl;
import dk.alexandra.fresco.tools.mascot.MascotSecurityParameters;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestParallelMascots {

  private ExecutorService executorService;
  private MascotSecurityParameters mascotSecurityParameters;
  private List<Integer> ports;
  private int noOfParties;
  private BigInteger modulus;
  private int iterations;
  private Logger logger = LoggerFactory.getLogger(TestParallelMascots.class);

  @Before
  public void setUp() {
    noOfParties = 2;
    ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(10000 + i * (noOfParties - 1));
    }
    executorService = Executors.newCachedThreadPool();
    mascotSecurityParameters = new MascotSecurityParameters();
    modulus = ModulusFinder.findSuitableModulus(mascotSecurityParameters.getModBitLength());
    iterations = 3;
  }

  @After
  public void tearDown() {
    executorService.shutdownNow();
  }

  @Test
  public void testConstructor() throws Exception {
    constructMascot();
  }

  private Map<Integer, RotList> perPartySingleSeedOtSetup(int myId, Drbg drbg, Network network) {
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (int otherId = 1; otherId <= noOfParties; otherId++) {
      if (otherId != myId) {
        Ot ot = new DummyOt(otherId, network);
        RotList currentSeedOts = new RotList(drbg, mascotSecurityParameters.getPrgSeedLength());
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
          .createRandomSsk(modulus, mascotSecurityParameters.getPrgSeedLength());
      macKeyShares.put(myId, ssk);
    }
    return macKeyShares;
  }

  private Drbg getDrbg() {
    byte[] drbgSeed = new byte[mascotSecurityParameters.getPrgSeedLength() / 8];
    new SecureRandom().nextBytes(drbgSeed);
    return new PaddingAesCtrDrbg(drbgSeed);
  }

  private void constructMascot() throws Exception {
    List<Map<Integer, RotList>> seedOts = setupOts();
    Map<Integer, FieldElement> perPartyMacKeyShares = setupMacKeyShares();
    final MascotSecurityParameters finalParams = mascotSecurityParameters;
    List<Callable<Mascot>> mascotCreators = new ArrayList<>();
    for (int i = 0; i < iterations; i++) {
      @SuppressWarnings("resource")
      KryoNetManager normalManager = new KryoNetManager(ports);
      for (int myId = 1; myId <= noOfParties; myId++) {
        FieldElement randomSsk = perPartyMacKeyShares.get(myId);
        int finalMyId = myId;
        int finalInstanceId = i;
        Map<Integer, RotList> seedOt = seedOts.get(finalMyId - 1);
        mascotCreators.add(() -> new Mascot(
            new MascotResourcePoolImpl(finalMyId, noOfParties,
              finalInstanceId, getDrbg(), seedOt, mascotSecurityParameters),
            normalManager.createExtraNetwork(finalMyId),
            randomSsk));
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
              new MascotResourcePoolImpl(finalMyId, noOfParties, finalInstanceId,
                  getDrbg(), seedOt, mascotSecurityParameters),
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

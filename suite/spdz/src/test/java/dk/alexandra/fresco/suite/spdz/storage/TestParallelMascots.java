package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.suite.spdz.KryoNetManager;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePoolImpl;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    partyIds = IntStream.range(1, 3).boxed().collect(Collectors.toList());
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

  public void constructMascot() throws Exception {
    List<Callable<Mascot>> mascotCreators = new ArrayList<>();

    for (int i = 0; i < iterations; i++) {
      KryoNetManager normalManager = new KryoNetManager(ports);
      for (int myId = 1; myId <= noOfParties; myId++) {

        FieldElement randomSsk = SpdzMascotDataSupplier
            .createRandomSsk(myId, modulus, modBitLength, prgSeedLength);
        int finalMyId = myId;
        mascotCreators.add(() -> {
          int lambdaParam = this.modBitLength;
          return new Mascot(new MascotResourcePoolImpl(finalMyId, partyIds,
              new PaddingAesCtrDrbg(new byte[]{12}, prgSeedLength), modulus,
              modBitLength, lambdaParam, prgSeedLength, numLeftFactors),
              normalManager.createExtraNetwork(finalMyId), randomSsk);
        });
      }
    }
    invoke(mascotCreators);
  }

  @Test
  public void testFirstTriples() throws Exception {
    List<Callable<List<MultTriple>>> mascotCreators = new ArrayList<>();

    for (int i = 0; i < iterations; i++) {
      KryoNetManager normalManager = new KryoNetManager(ports);
      for (int myId = 1; myId <= noOfParties; myId++) {

        FieldElement randomSsk = SpdzMascotDataSupplier
            .createRandomSsk(myId, modulus, modBitLength, prgSeedLength);
        int finalMyId = myId;
        mascotCreators.add(() -> {
          Mascot mascot = new Mascot(new MascotResourcePoolImpl(finalMyId, partyIds,
              new PaddingAesCtrDrbg(new byte[]{7, 127, -1}, prgSeedLength), modulus,
              modBitLength, 256, prgSeedLength, numLeftFactors),
              normalManager.createExtraNetwork(finalMyId), randomSsk);
          return mascot.getTriples(128);
        });
      }
    }
    invoke(mascotCreators);
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

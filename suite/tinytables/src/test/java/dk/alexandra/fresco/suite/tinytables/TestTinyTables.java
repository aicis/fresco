package dk.alexandra.fresco.suite.tinytables;

import dk.alexandra.fresco.IntegrationTest;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestFrameworkException;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.lib.bool.ComparisonBooleanTests;
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import dk.alexandra.fresco.lib.field.bool.generic.FieldBoolTests;
import dk.alexandra.fresco.lib.math.bool.add.AddTests;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.ot.TinyTablesDummyOt;
import dk.alexandra.fresco.suite.tinytables.ot.TinyTablesOt;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproResourcePool;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class TestTinyTables {
  private static final int COMPUTATIONAL_SECURITY = 128;
  private static final int STATISTICAL_SECURITY = 40;

  private void runTest(TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy, boolean preprocessing, String name) {
    int noPlayers = 2;
    List<Integer> ports = Network.getFreePorts(noPlayers);
    Map<Integer, NetworkConfiguration> netConf = Network.getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();

    for (int playerId : netConf.keySet()) {
      File tinyTablesFile = new File(getFilenameForTest(playerId, name));
      Supplier<ResourcePoolImpl> resourcePoolSupplier;
      SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> computationEngine;
      if (preprocessing) {
        BatchEvaluationStrategy<TinyTablesPreproResourcePool> batchStrategy =
            evalStrategy.getStrategy();
        TinyTablesPreproProtocolSuite suite = new TinyTablesPreproProtocolSuite();
        TinyTablesOt baseOt = new TinyTablesDummyOt(Util.otherPlayerId(playerId));
        Drbg random = new AesCtrDrbg(new byte[32]);
        resourcePoolSupplier =
            () -> new TinyTablesPreproResourcePool(playerId, noPlayers, baseOt, random,
                COMPUTATIONAL_SECURITY, STATISTICAL_SECURITY, tinyTablesFile);
        ProtocolEvaluator<TinyTablesPreproResourcePool> evaluator =
            new BatchedProtocolEvaluator<>(batchStrategy, suite);
        computationEngine =
            (SecureComputationEngine) new SecureComputationEngineImpl<>(suite, evaluator);
      } else {
        BatchEvaluationStrategy<ResourcePoolImpl> batchStrategy = evalStrategy.getStrategy();
        TinyTablesProtocolSuite suite = new TinyTablesProtocolSuite(playerId, tinyTablesFile);
        resourcePoolSupplier = () -> new ResourcePoolImpl(playerId, noPlayers);
        ProtocolEvaluator<ResourcePoolImpl> evaluator =
            new BatchedProtocolEvaluator<>(batchStrategy, suite);
        computationEngine = new SecureComputationEngineImpl<>(suite, evaluator);
      }
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> configuration =
          new TestThreadConfiguration<>(computationEngine, resourcePoolSupplier,
              () -> new AsyncNetwork(netConf.get(playerId)));
      conf.put(playerId, configuration);
    }
    TestThreadRunner.run(f, conf);

  }

  /*
   * Helper methods
   */

  private String getFilenameForTest(int playerId, String name) {
    return "tinytables/TinyTables_" + name + "_" + playerId;
  }

  private static void deleteFileOrFolder(final Path path) throws IOException {
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
          throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(final Path file, final IOException e) {
        return handleException(e);
      }

      private FileVisitResult handleException(final IOException e) {
        e.printStackTrace(); // replace with more robust error handling
        return FileVisitResult.TERMINATE;
      }

      @Override
      public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
          throws IOException {
        if (e != null) {
          return handleException(e);
        }
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /*
   * Basic tests
   */


  /**
   * Creates a directory in which to store preprocessing material for TinyTables. If the directory
   * already exists the old directory is deleted and a new is created.
   *
   * @throws IOException if an exception occurs handling the directory.
   */
  @Before
  public void checkFolderExists() throws IOException {
    File f = new File("tinytables");
    if (f.exists()) {
      deleteFileOrFolder(f.toPath());
      f.mkdir();
    } else {
      f.mkdir();
    }
  }

  /**
   * Removes the directory storing data for the tests.
   *
   * @throws IOException if an exception occurs while deleting the directory
   */
  @After
  public void removeFolder() throws IOException {
    File f = new File("tinytables");
    if (f.exists()) {
      deleteFileOrFolder(f.toPath());
    }
  }

  @Test
  public void testInput() {
    runTest(new BasicBooleanTests.TestInput<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testInput");
    runTest(new BasicBooleanTests.TestInput<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testInput");
  }

  @Test
  public void testXor() {
    runTest(new BasicBooleanTests.TestXOR<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testXOR");
    runTest(new BasicBooleanTests.TestXOR<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testXOR");
  }

  @Test
  public void testAnd() {
    runTest(new BasicBooleanTests.TestAND<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testAND");
    runTest(new BasicBooleanTests.TestAND<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testAND");
  }

  @Test
  public void testManyAnd() {
    final int numAnds = 2000;
    runTest(new BasicBooleanTests.TestMultipleAnds<>(false, numAnds),
        EvaluationStrategy.SEQUENTIAL_BATCHED, true, "testAND");
    runTest(new BasicBooleanTests.TestMultipleAnds<>(true, numAnds),
        EvaluationStrategy.SEQUENTIAL_BATCHED, false, "testAND");
  }

  @Test
  public void testNot() {
    runTest(new BasicBooleanTests.TestNOT<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testNOT");
    runTest(new BasicBooleanTests.TestNOT<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testNOT");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTooManyPlayers() throws Throwable {
    new TinyTablesPreproResourcePool(1, 3, null, new AesCtrDrbg(), COMPUTATIONAL_SECURITY,
        STATISTICAL_SECURITY, null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRandomBitOffline() throws Throwable {
    try {
      runTest(new BasicBooleanTests.TestRandomBit<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED,
          true, "testRandomBit");
    } catch (TestFrameworkException tfe) {
      if (tfe.getCause() instanceof RuntimeException) {
        throw tfe.getCause().getCause();
      } else {
        throw tfe;
      }
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRandomBitOnline() throws Throwable {
    try {
      final String name = "testRandomBit";
      // Run preprocessing for something, just to generate the required files.
      runTest(new BasicBooleanTests.TestXOR<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
          name);
      runTest(new BasicBooleanTests.TestRandomBit<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED,
          false, name);
    } catch (TestFrameworkException tfe) {
      if (tfe.getCause() instanceof RuntimeException) {
        throw tfe.getCause().getCause();
      } else {
        throw tfe;
      }
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testOpenOffline() throws Throwable {
    try {
      runTest(new FieldBoolTests.TestOpen<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
          "testOpenOffline");
    } catch (TestFrameworkException tfe) {
      if (tfe.getCause() instanceof RuntimeException) {
        throw tfe.getCause().getCause();
      } else {
        throw tfe;
      }
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testOpenOnline() throws Throwable {
    try {
      final String name = "testOpenOnline";
      // Run preprocessing for something, just to generate the required files.
      runTest(new BasicBooleanTests.TestXOR<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
          name);
      runTest(new FieldBoolTests.TestOpen<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, false, name);
    } catch (TestFrameworkException tfe) {
      if (tfe.getCause() instanceof RuntimeException) {
        throw tfe.getCause().getCause();
      } else {
        throw tfe;
      }
    }
  }

  @Test
  public void testBasicProtocols() {
    runTest(new BasicBooleanTests.TestBasicProtocols<>(false),
        EvaluationStrategy.SEQUENTIAL_BATCHED, true, "testBasicProtocols");
    runTest(new BasicBooleanTests.TestBasicProtocols<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED,
        false, "testBasicProtocols");
  }

  /* Bristol tests */

  @Category(IntegrationTest.class)
  @Test
  public void testMult() {
    runTest(new BristolCryptoTests.Mult32x32Test<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED,
        true, "testMult32x32");
    runTest(new BristolCryptoTests.Mult32x32Test<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED,
        false, "testMult32x32");
  }

  @Category(IntegrationTest.class)
  @Test
  public void testAes() {
    runTest(new BristolCryptoTests.AesTest<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testAES");
    runTest(new BristolCryptoTests.AesTest<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testAES");
  }

  @Category(IntegrationTest.class)
  @Test
  public void test_Des() {
    runTest(new BristolCryptoTests.DesTest<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testDES");
    runTest(new BristolCryptoTests.DesTest<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testDES");
  }

  @Category(IntegrationTest.class)
  @Test
  public void test_Sha1() {
    runTest(new BristolCryptoTests.Sha1Test<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testSHA1");
    runTest(new BristolCryptoTests.Sha1Test<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testSHA1");
  }

  @Category(IntegrationTest.class)
  @Test
  public void test_Sha256() {
    runTest(new BristolCryptoTests.Sha256Test<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testSHA256");
    runTest(new BristolCryptoTests.Sha256Test<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testSHA256");
  }

  /* Advanced functionality */

  @Test
  public void test_Binary_Adder() {
    runTest(new AddTests.TestFullAdder<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED, true,
        "testAdder");
    runTest(new AddTests.TestFullAdder<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, false,
        "testAdder");
  }

  @Test
  public void test_comparison() {
    runTest(new ComparisonBooleanTests.TestGreaterThan<>(false),
        EvaluationStrategy.SEQUENTIAL_BATCHED, true, "testGT");
    runTest(new ComparisonBooleanTests.TestGreaterThan<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED, false, "testGT");
  }

  @Test
  public void test_equality() {
    runTest(new ComparisonBooleanTests.TestEquality<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED,
        true, "testEQ");
    runTest(new ComparisonBooleanTests.TestEquality<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED,
        false, "testEQ");
  }

  @Test
  public void testNaorPinkasBaseOtDes() {
    int noPlayers = 2;
    List<Integer> ports = Network.getFreePorts(noPlayers);
    Map<Integer, NetworkConfiguration> netConf = Network.getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf = new HashMap<>();

    for (int playerId : netConf.keySet()) {
      File tinyTablesFile = new File(getFilenameForTest(playerId, "TestNaorPinkasBaseOtDes"));
      Supplier<ResourcePoolImpl> resourcePoolSupplier;
      SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> computationEngine;
      TinyTablesPreproProtocolSuite suite = new TinyTablesPreproProtocolSuite();
      TinyTablesOt baseOt = new TinyTablesDummyOt(Util.otherPlayerId(playerId));
      Drbg random = new AesCtrDrbg(new byte[32]);
      resourcePoolSupplier = () -> new TinyTablesPreproResourcePool(playerId, noPlayers, baseOt,
          random, COMPUTATIONAL_SECURITY, STATISTICAL_SECURITY, tinyTablesFile);
      ProtocolEvaluator<TinyTablesPreproResourcePool> evaluator = new BatchedProtocolEvaluator<>(
          EvaluationStrategy.SEQUENTIAL_BATCHED.getStrategy(), suite);
      computationEngine = (SecureComputationEngine) new SecureComputationEngineImpl<>(suite,
          evaluator);
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> configuration = new TestThreadConfiguration<>(
          computationEngine, resourcePoolSupplier, () -> new AsyncNetwork(netConf.get(playerId)));
      conf.put(playerId, configuration);
    }
    TestThreadRunner.run(new BristolCryptoTests.DesTest<>(false), conf);

  }
}

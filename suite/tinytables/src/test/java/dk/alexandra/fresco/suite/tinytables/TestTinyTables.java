package dk.alexandra.fresco.suite.tinytables;

import dk.alexandra.fresco.IntegrationTest;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.lib.bool.ComparisonBooleanTests;
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import dk.alexandra.fresco.lib.math.bool.add.AddTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class TestTinyTables {

  private void runTest(TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy, boolean preprocessing, String name) {
    int noPlayers = 2;
    // Since SCAPI currently does not work with ports > 9999 we use fixed
    // ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(noPlayers);
    for (int i = 1; i <= noPlayers; i++) {
      ports.add(9000 + i);
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();

    for (int playerId : netConf.keySet()) {
      ProtocolEvaluator<ResourcePoolImpl> evaluator;

      ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> suite;
      File tinyTablesFile = new File(getFilenameForTest(playerId, name));
      if (preprocessing) {
        suite = new TinyTablesPreproProtocolSuite(playerId, tinyTablesFile);
      } else {
        suite = new TinyTablesProtocolSuite(playerId, tinyTablesFile);
      }
      BatchEvaluationStrategy<ResourcePoolImpl> batchStrat = evalStrategy.getStrategy();
      evaluator = new BatchedProtocolEvaluator<>(batchStrat, suite);
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadConfiguration<>(
              new SecureComputationEngineImpl<>(suite, evaluator),
              () -> new ResourcePoolImpl(playerId, noPlayers),
              () -> new KryoNetNetwork(netConf.get(playerId))
          );
      conf.put(playerId, ttc);
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

  // ensure that the tinytables folder is new for each test and is deleted upon exiting each test.
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

  @After
  public void removeFolder() throws IOException {
    File f = new File("tinytables");
    if (f.exists()) {
      deleteFileOrFolder(f.toPath());
    }
  }

  @Test
  public void testInput() {
    runTest(new BasicBooleanTests.TestInput<>(false), EvaluationStrategy.SEQUENTIAL,
        true, "testInput");
    runTest(new BasicBooleanTests.TestInput<>(true), EvaluationStrategy.SEQUENTIAL,
        false, "testInput");
  }

  @Test
  public void testXOR() {
    runTest(new BasicBooleanTests.TestXOR<>(false), EvaluationStrategy.SEQUENTIAL,
        true, "testXOR");
    runTest(new BasicBooleanTests.TestXOR<>(true), EvaluationStrategy.SEQUENTIAL,
        false, "testXOR");
  }

  @Test
  public void testAND() {
    runTest(new BasicBooleanTests.TestAND<>(false), EvaluationStrategy.SEQUENTIAL,
        true, "testAND");
    runTest(new BasicBooleanTests.TestAND<>(true), EvaluationStrategy.SEQUENTIAL,
        false, "testAND");
  }

  @Test
  public void testNOT() {
    runTest(new BasicBooleanTests.TestNOT<>(false), EvaluationStrategy.SEQUENTIAL,
        true, "testNOT");
    runTest(new BasicBooleanTests.TestNOT<>(true), EvaluationStrategy.SEQUENTIAL,
        false, "testNOT");
  }

  @Test
  public void testBasicProtocols() {
    runTest(new BasicBooleanTests.TestBasicProtocols<>(false),
        EvaluationStrategy.SEQUENTIAL, true, "testBasicProtocols");
    runTest(new BasicBooleanTests.TestBasicProtocols<>(true),
        EvaluationStrategy.SEQUENTIAL, false, "testBasicProtocols");
  }

  /* Bristol tests */

  @Category(IntegrationTest.class)
  @Test
  public void testMult() {
    runTest(new BristolCryptoTests.Mult32x32Test<>(false),
        EvaluationStrategy.SEQUENTIAL, true, "testMult32x32");
    runTest(new BristolCryptoTests.Mult32x32Test<>(true),
        EvaluationStrategy.SEQUENTIAL, false, "testMult32x32");
  }

  @Category(IntegrationTest.class)
  @Test
  public void testAES() {
    runTest(new BristolCryptoTests.AesTest<>(false), EvaluationStrategy.SEQUENTIAL,
        true, "testAES");
    runTest(new BristolCryptoTests.AesTest<>(true), EvaluationStrategy.SEQUENTIAL,
        false, "testAES");
  }

  @Category(IntegrationTest.class)
  @Test
  public void test_DES() {
    runTest(new BristolCryptoTests.DesTest<>(false), EvaluationStrategy.SEQUENTIAL,
        true, "testDES");
    runTest(new BristolCryptoTests.DesTest<>(true), EvaluationStrategy.SEQUENTIAL,
        false, "testDES");
  }

  @Category(IntegrationTest.class)
  @Test
  public void test_SHA1() {
    runTest(new BristolCryptoTests.Sha1Test<>(false), EvaluationStrategy.SEQUENTIAL,
        true, "testSHA1");
    runTest(new BristolCryptoTests.Sha1Test<>(true), EvaluationStrategy.SEQUENTIAL,
        false, "testSHA1");
  }

  @Category(IntegrationTest.class)
  @Test
  public void test_SHA256() {
    runTest(new BristolCryptoTests.Sha256Test<>(false),
        EvaluationStrategy.SEQUENTIAL, true, "testSHA256");
    runTest(new BristolCryptoTests.Sha256Test<>(true),
        EvaluationStrategy.SEQUENTIAL, false, "testSHA256");
  }

  /* Advanced functionality */

  @Test
  public void test_Binary_Adder() {
    runTest(new AddTests.TestFullAdder<>(false),
        EvaluationStrategy.SEQUENTIAL_BATCHED, true, "testAdder");
    runTest(new AddTests.TestFullAdder<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED, false, "testAdder");
  }

  @Test
  public void test_comparison() {
    runTest(new ComparisonBooleanTests.TestGreaterThan<>(false),
        EvaluationStrategy.SEQUENTIAL, true, "testGT");
    runTest(new ComparisonBooleanTests.TestGreaterThan<>(true),
        EvaluationStrategy.SEQUENTIAL, false, "testGT");
  }

  @Test
  public void test_equality() {
    runTest(new ComparisonBooleanTests.TestEquality<>(false),
        EvaluationStrategy.SEQUENTIAL, true, "testEQ");
    runTest(new ComparisonBooleanTests.TestEquality<>(true),
        EvaluationStrategy.SEQUENTIAL, false, "testEQ");
  }
}

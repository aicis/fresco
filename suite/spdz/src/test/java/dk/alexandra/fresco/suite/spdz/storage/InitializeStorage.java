package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class which is useful in tests where we use a file-based approach for fetching the pre-processed
 * material. It works by utilizing the {@link FakeTripGen} to generate correct, but not secure
 * pre-processed values.
 */
public class InitializeStorage {

  /**
   * Removes all preprocessed material previously produced by this class' init*Storage methods.
   */
  public static void cleanup() throws IOException {
    String folder = SpdzStorageDataSupplier.STORAGE_FOLDER;
    if (!new File(folder).exists()) {
      System.out.println(
          "The folder '" + folder + "' does not exist. Continuing without removing anything");
      return;
    }
    System.out.println("Removing any preprocessed material from the folder " + folder);
    deleteFileOrFolder(Paths.get(folder));
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
        if (e != null)
          return handleException(e);
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  };

  /**
   * Generates on runtime the necessary preprocessed data for SPDZ tests and stores it in the
   * different stores that were given as argument.
   *
   * @param stores
   * @param noOfPlayers
   * @param noOfTriples
   * @param noOfInputMasks
   * @param noOfBits
   */
  public static void initStorage(Storage[] stores, int noOfPlayers, int noOfTriples,
      int noOfInputMasks, int noOfBits, int noOfExpPipes) {

    List<Storage> tmpStores = new ArrayList<Storage>();
    for (Storage s : stores) {
      if (s.getObject(SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + 1,
          SpdzStorageDataSupplier.MODULUS_KEY) == null) {
        tmpStores.add(s);
      }
    }
    Storage[] storages = tmpStores.toArray(new Storage[0]);

    BigInteger p = new BigInteger(
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");

    List<BigInteger> alphaShares = FakeTripGen.generateAlphaShares(noOfPlayers, p);
    BigInteger alpha = BigInteger.ZERO;
    for (BigInteger share : alphaShares) {
      alpha = alpha.add(share);
    }
    alpha = alpha.mod(p);

    List<SpdzTriple[]> triples = FakeTripGen.generateTriples(noOfTriples, noOfPlayers, p, alpha);
    List<List<SpdzInputMask[]>> inputMasks =
        FakeTripGen.generateInputMasks(noOfInputMasks, noOfPlayers, p, alpha);
    List<SpdzSInt[]> bits = FakeTripGen.generateBits(noOfBits, noOfPlayers, p, alpha);
    List<SpdzSInt[][]> expPipes = FakeTripGen.generateExpPipes(noOfExpPipes, noOfPlayers, p, alpha);

    for (Storage store : storages) {
      for (int i = 1; i < noOfPlayers + 1; i++) {
        String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + i;
        store.putObject(storageName, SpdzStorageDataSupplier.MODULUS_KEY, p);
        store.putObject(storageName, SpdzStorageDataSupplier.SSK_KEY, alphaShares.get(i - 1));
      }
      // triples
      int tripleCounter = 0;
      for (SpdzTriple[] triple : triples) {
        for (int i = 0; i < noOfPlayers; i++) {
          String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + (i + 1);
          store.putObject(storageName, SpdzStorageDataSupplier.TRIPLE_KEY_PREFIX + tripleCounter,
              triple[i]);
        }
        tripleCounter++;
      }
      // inputs
      // towards player
      for (int towardsPlayer = 1; towardsPlayer < inputMasks.size() + 1; towardsPlayer++) {
        int[] inputCounters = new int[noOfPlayers];
        // number of inputs towards that player
        for (SpdzInputMask[] masks : inputMasks.get(towardsPlayer - 1)) {
          // single shares of that input
          for (int i = 0; i < noOfPlayers; i++) {
            String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + (i + 1);
            String key =
                SpdzStorageDataSupplier.INPUT_KEY_PREFIX + towardsPlayer + "_" + inputCounters[i];
            store.putObject(storageName, key, masks[i]);
            inputCounters[i]++;
          }
        }
      }

      // bits
      int bitCounter = 0;
      for (SpdzSInt[] bit : bits) {
        for (int i = 0; i < noOfPlayers; i++) {
          String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + (i + 1);
          String key = SpdzStorageDataSupplier.BIT_KEY_PREFIX + bitCounter;
          store.putObject(storageName, key, bit[i]);
        }
        bitCounter++;
      }

      // exp pipes
      int expCounter = 0;
      for (SpdzSInt[][] expPipe : expPipes) {
        for (int i = 0; i < noOfPlayers; i++) {
          String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + (i + 1);
          String key = SpdzStorageDataSupplier.EXP_PIPE_KEY_PREFIX + expCounter;
          store.putObject(storageName, key, expPipe[i]);
        }
        expCounter++;
      }
    }
  }

  /**
   * Initializes the storage
   *
   * @param storage The storages to initialize (multiple storages are used when using a
   *        strategy with multiple threads)
   * @param noOfPlayers The number of players
   * @param noOfThreads The number of threads used
   * @param noOfTriples The number of triples to generate
   * @param noOfInputMasks The number of masks for input to generate.
   * @param noOfBits The number of random bits to generate
   * @param noOfExpPipes The number of exponentiation pipes to generate.
   * @param p The modulus to use.
   */
  public static void initStreamedStorage(StreamedStorage storage, int noOfPlayers,
      int noOfThreads, int noOfTriples, int noOfInputMasks, int noOfBits, int noOfExpPipes,
      BigInteger p) {
    try {
      // Try get the last thread file. If that fails, we need to
      // generate the files
      Serializable next = storage
          .getNext(SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreads + "_" + 1 + "_" + 0
              + "_" + SpdzStorageDataSupplier.MODULUS_KEY);
      if (next != null) {
        return;
      }
    } catch (Exception e) {
      // Likely we could not find the file, so we generate new ones
    }

    System.out.println("Generating preprocessed data!");
    File f = new File("spdz");
    if (!f.exists()) {
      f.mkdirs();
    }

    List<BigInteger> alphaShares = FakeTripGen.generateAlphaShares(noOfPlayers, p);
    BigInteger alpha = BigInteger.ZERO;
    for (BigInteger share : alphaShares) {
      alpha = alpha.add(share);
    }
    alpha = alpha.mod(p);

    FakeTripGen generator = new FakeTripGen();

    for (int i = 1; i < noOfPlayers + 1; i++) {
      for (int threadId = 0; threadId < noOfThreads; threadId++) {
        String storageName =
            SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreads + "_" + i + "_" + threadId + "_";
        storage.putNext(storageName + SpdzStorageDataSupplier.MODULUS_KEY, p);
        storage.putNext(storageName + SpdzStorageDataSupplier.SSK_KEY, alphaShares.get(i - 1));
      }
    }
    System.out.println("Set modulus and alpha. Now generating triples");
    // triples
    List<List<ObjectOutputStream>> streams = new ArrayList<>();
    for (int threadId = 0; threadId < noOfThreads; threadId++) {
      List<ObjectOutputStream> ooss = new ArrayList<ObjectOutputStream>();
      for (int i = 0; i < noOfPlayers; i++) {
        String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreads + "_" + (i + 1)
            + "_" + threadId + "_" + SpdzStorageDataSupplier.TRIPLE_STORAGE;
        try {
          ObjectOutputStream oos =
              new ObjectOutputStream(new FileOutputStream(new File(storageName)));
          ooss.add(oos);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          throw new RuntimeException("Could not open the file " + storageName, e);
        } catch (IOException e) {
          e.printStackTrace();
          throw new RuntimeException("Could not write to the file " + storageName, e);
        }
      }
      streams.add(ooss);
    }
    // }
    try {
      generator.generateTripleStream(noOfTriples, noOfPlayers, p, alpha, new Random(), streams);
      for (List<ObjectOutputStream> s : streams) {
        for (ObjectOutputStream o : s) {
          o.flush();
          o.close();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not write the triple stream", e);
    }
    System.out.println("Done generating triples, now generating input masks");

    List<List<List<ObjectOutputStream>>> oosss = new ArrayList<>();
    for (int towardsPlayer = 1; towardsPlayer < noOfPlayers + 1; towardsPlayer++) {
      streams = new ArrayList<>();
      for (int threadId = 0; threadId < noOfThreads; threadId++) {
        List<ObjectOutputStream> ooss = new ArrayList<>();
        for (int i = 0; i < noOfPlayers; i++) {
          String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreads + "_"
              + (i + 1) + "_" + threadId + "_" + SpdzStorageDataSupplier.INPUT_STORAGE + towardsPlayer;
          try {
            ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(new File(storageName)));
            ooss.add(oos);
          } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open the file " + storageName, e);
          } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not write to the file " + storageName, e);
          }
        }
        streams.add(ooss);
      }
      oosss.add(streams);
    }
    try {
      for (int towardsPlayer = 0; towardsPlayer < noOfPlayers; towardsPlayer++) {
        generator.generateInputMaskStream(noOfInputMasks, noOfPlayers, towardsPlayer, p, alpha,
            new Random(), oosss.get(towardsPlayer));
      }
      for (List<List<ObjectOutputStream>> ooss : oosss) {
        for (List<ObjectOutputStream> s : ooss) {
          for (ObjectOutputStream o : s) {
            o.flush();
            o.close();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not write the Input mask stream", e);
    }

    System.out.println("Done generating input masks, now generating bits");

    streams = new ArrayList<>();
    // bits
    for (int threadId = 0; threadId < noOfThreads; threadId++) {
      List<ObjectOutputStream> ooss = new ArrayList<>();
      for (int i = 0; i < noOfPlayers; i++) {

        String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreads + "_" + (i + 1)
            + "_" + threadId + "_" + SpdzStorageDataSupplier.BIT_STORAGE;
        try {
          ObjectOutputStream oos =
              new ObjectOutputStream(new FileOutputStream(new File(storageName)));
          ooss.add(oos);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          throw new RuntimeException("Could not open the file " + storageName, e);
        } catch (IOException e) {
          e.printStackTrace();
          throw new RuntimeException("Could not write to the file " + storageName, e);
        }
      }
      streams.add(ooss);
    }
    try {
      generator.generateBitStream(noOfBits, noOfPlayers, p, alpha, new Random(), streams);
      for (List<ObjectOutputStream> s : streams) {
        for (ObjectOutputStream o : s) {
          o.flush();
          o.close();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not write the bit stream", e);
    }

    System.out.println("Done generating bits, now generating exponentiation pipes");

    streams = new ArrayList<>();
    // exp pipes
    for (int threadId = 0; threadId < noOfThreads; threadId++) {
      List<ObjectOutputStream> ooss = new ArrayList<>();
      for (int i = 0; i < noOfPlayers; i++) {
        String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreads + "_" + (i + 1)
            + "_" + threadId + "_" + SpdzStorageDataSupplier.EXP_PIPE_STORAGE;
        try {
          ObjectOutputStream oos =
              new ObjectOutputStream(new FileOutputStream(new File(storageName)));
          ooss.add(oos);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          throw new RuntimeException("Could not open the file " + storageName, e);
        } catch (IOException e) {
          e.printStackTrace();
          throw new RuntimeException("Could not write to the file " + storageName, e);
        }
      }
      streams.add(ooss);
    }
    try {
      generator.generateExpPipeStream(noOfExpPipes, noOfPlayers, p, alpha, new Random(), streams);
      for (List<ObjectOutputStream> s : streams) {
        for (ObjectOutputStream o : s) {
          o.flush();
          o.close();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not write the exp pipe stream", e);
    }
    System.out.println("Done generating preprocessed data for the SPDZ protocol suite");
  }

  /**
   * Does the same as
   * {@link #initStreamedStorage(StreamedStorage, int, int, int, int, int, int, BigInteger)} but
   * where the chosen modulus is chosen for you, and is the same as the one found in:
   * {@link SpdzDummyDataSupplier}
   */
  public static void initStreamedStorage(StreamedStorage streamedStorage,
      int noOfPlayers, int noOfThreads, int noOfTriples, int noOfInputMasks, int noOfBits,
      int noOfExpPipes) {
    BigInteger p = new BigInteger(
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
    InitializeStorage.initStreamedStorage(streamedStorage, noOfPlayers, noOfThreads, noOfTriples,
        noOfInputMasks, noOfBits, noOfExpPipes, p);
  }
}

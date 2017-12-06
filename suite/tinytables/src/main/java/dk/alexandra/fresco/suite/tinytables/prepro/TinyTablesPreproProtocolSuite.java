package dk.alexandra.fresco.suite.tinytables.prepro;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.BitVector;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElementVector;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.ot.base.BaseOTFactory;
import dk.alexandra.fresco.suite.tinytables.ot.extension.SemiHonestOTExtensionFactory;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproProtocol;
import dk.alexandra.fresco.suite.tinytables.storage.BatchTinyTablesTripleProvider;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorageImpl;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesTripleProvider;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This protocol suite is intended for the preprocessing phase of the TinyTables protocol as created
 * by Ivan Damgård, Jesper Buus Nielsen and Michael Nielsen from the Department of Computer Science
 * at Aarhus University.
 * </p>
 *
 * <p>
 * The TinyTables protocol has to phases - a <i>preprocessing</i> and an <i>online</i> phase. In the
 * preprocessing phase, each of the two players picks his additive share of a mask for each input
 * wire of a protocol. Furthermore, for each AND protocol each of the two players must also
 * calculate a so-called <i>TinyTable</i> which is used in the online phase (see
 * {@link TinyTablesProtocolSuite}). This is done using oblivious transfer. To enhance performance,
 * all oblivious transfers are done at the end of the preprocessing (see
 * {@link #createRoundSynchronization()).
 * </p>
 *
 * <p>
 * The masking values and TinyTables are stored in a {@link TinyTablesStorage} which can be stored
 * for later use in the online phase. In order to avoid leaks, you should not reuse the values from
 * a preprocessing in multiple evaluations of a protocol, but should instead preprocess once per
 * evaluation. Note that all the values calculated during the preprocessing phase is saved with a
 * protocols ID as key, which is simply incremented on each created protocol, it is important that
 * the protocols are created in exactly the same order in the preprocessing and online phases.
 * </p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesPreproProtocolSuite
    implements ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> {

  private final static Logger logger = LoggerFactory.getLogger(TinyTablesPreproProtocolSuite.class);

  private TinyTablesStorage storage;
  private File tinyTablesFile;
  private TinyTablesTripleProvider tinyTablesTripleProvider;
  private List<TinyTablesPreproANDProtocol> unprocessedAndGates;
  private final SecureRandom secRand;
  private static volatile Map<Integer, TinyTablesPreproProtocolSuite> instances =
      Collections.synchronizedMap(new HashMap<>());

  public static TinyTablesPreproProtocolSuite getInstance(int id) {
    return instances.get(id);
  }

  public TinyTablesPreproProtocolSuite(int id, File tinyTablesFile) {
    this.secRand = new SecureRandom();
    this.storage = TinyTablesStorageImpl.getInstance(id);
    this.tinyTablesFile = tinyTablesFile;
    instances.put(id, this);
  }

  public SecureRandom getSecureRandom() {
    return secRand;
  }
  
  @Override
  public BuilderFactory<ProtocolBuilderBinary> init(
      ResourcePoolImpl resourcePool, Network network) {
    OTFactory otFactory = new SemiHonestOTExtensionFactory(network,
        resourcePool.getMyId(), 128, new BaseOTFactory(network,
        resourcePool.getMyId(), secRand),
        secRand);

    this.tinyTablesTripleProvider =
        new BatchTinyTablesTripleProvider(new TinyTablesTripleGenerator(resourcePool.getMyId(),
            secRand, otFactory), 1500);

    this.unprocessedAndGates =
        Collections.synchronizedList(new ArrayList<TinyTablesPreproANDProtocol>());

    return new TinyTablesPreproBuilderFactory();
  }

  public TinyTablesStorage getStorage() {
    return this.storage;
  }

  public void addANDGate(TinyTablesPreproANDProtocol gate) {
    this.unprocessedAndGates.add(gate);
  }

  @Override
  public RoundSynchronization<ResourcePoolImpl> createRoundSynchronization() {
    return new DummyRoundSynchronization<ResourcePoolImpl>() {
      @Override
      public void finishedBatch(int gatesEvaluated, ResourcePoolImpl resourcePool,
          Network network) {
        /*
         * When 1000 AND gates needs to be processed, we do it.
         */
        if (TinyTablesPreproProtocolSuite.this.unprocessedAndGates.size() > 1000) {
          calculateTinyTablesForUnprocessedANDGates(resourcePool, network);
        }
      }

      @Override
      public void finishedEval(ResourcePoolImpl resourcePool, Network network) {
        calculateTinyTablesForUnprocessedANDGates(resourcePool, network);
        tinyTablesTripleProvider.close();
        /*
         * Store the TinyTables to a file.
         */
        try {
          storeTinyTables(storage, tinyTablesFile);
          logger.info("TinyTables stored to " + tinyTablesFile);
        } catch (IOException e) {
          logger.error("Failed to save TinyTables: " + e.getMessage());
        }

      }

    };
  }

  private void calculateTinyTablesForUnprocessedANDGates(
      ResourcePool resourcePool, Network network) {
    int unprocessedGates = this.unprocessedAndGates.size();

      /*
       * Sort the unprocessed gates to make sure that the players process them in the same order.
       */
    this.unprocessedAndGates.sort(Comparator.comparingInt(TinyTablesPreproProtocol::getId));

    // Two bits per gate
    TinyTablesElementVector shares = new TinyTablesElementVector(unprocessedGates * 2);
    List<TinyTablesTriple> usedTriples = new ArrayList<>();
    for (int i = 0; i < unprocessedGates; i++) {
      TinyTablesPreproANDProtocol gate = this.unprocessedAndGates.get(i);
      TinyTablesTriple triple = this.tinyTablesTripleProvider.getNextTriple();
      usedTriples.add(triple);

        /*
         * Calculate temp values e, d for multiplication. These should be opened before calling
         * finalize.
         */
      Pair<TinyTablesElement, TinyTablesElement> msg =
          gate.getInRight().getValue().multiply(gate.getInLeft().getValue(), triple);

      shares.setShare(2 * i, msg.getFirst().getShare());
      shares.setShare(2 * i + 1, msg.getSecond().getShare());
    }

    byte[] size = ByteBuffer.allocate(2).putShort((short) shares.getSize()).array();
    // send
    network.send(Util.otherPlayerId(resourcePool.getMyId()), size);
    network.send(Util.otherPlayerId(resourcePool.getMyId()),
        shares.payload());

    // receive
    size = network.receive(Util.otherPlayerId(resourcePool.getMyId()));
    short length = ByteBuffer.wrap(size).getShort();
    byte[] data =
        network.receive(Util.otherPlayerId(resourcePool.getMyId()));
    TinyTablesElementVector otherShares = new TinyTablesElementVector(data, length);

    BitVector open = TinyTablesElementVector.open(shares, otherShares);

    for (int i = 0; i < unprocessedGates; i++) {
      TinyTablesPreproANDProtocol gate = this.unprocessedAndGates.get(i);
      boolean e = open.get(2 * i);
      boolean d = open.get(2 * i + 1);

      TinyTablesElement product = TinyTablesElement.finalizeMultiplication(e, d,
          usedTriples.get(i), resourcePool.getMyId());

      TinyTable tinyTable = gate.calculateTinyTable(resourcePool.getMyId(), product);

      this.storage.storeTinyTable(gate.getId(), tinyTable);
    }

    this.unprocessedAndGates.clear();
  }

  private void storeTinyTables(TinyTablesStorage tinyTablesStorage, File file) throws IOException {
    file.createNewFile();
    FileOutputStream fout = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(tinyTablesStorage);
    oos.close();
  }

}

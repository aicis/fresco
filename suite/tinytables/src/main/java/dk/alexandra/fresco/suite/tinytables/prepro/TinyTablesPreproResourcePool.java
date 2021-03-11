package dk.alexandra.fresco.suite.tinytables.prepro;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.RegularBitVector;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElementVector;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.ot.TinyTablesOt;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproProtocol;
import dk.alexandra.fresco.suite.tinytables.storage.BatchTinyTablesTripleProvider;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorageImpl;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesTripleProvider;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.ot.otextension.BristolOtFactory;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;
import dk.alexandra.fresco.tools.ot.otextension.RotFactory;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TinyTablesPreproResourcePool extends ResourcePoolImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(TinyTablesPreproResourcePool.class);
  private static final int TRIP_BATCH_SIZE = 8192;

  private final Drng drng;
  private final List<TinyTablesPreproANDProtocol> unprocessedAnds;
  private final TinyTablesStorage storage;
  private final File tinyTablesFile;
  private final Supplier<TinyTablesTripleProvider> supplier;
  private TinyTablesTripleProvider tinyTablesTripleProvider;

  /**
   * Creates an instance of the default implementation of a resource pool. This contains the basic
   * resources needed within FRESCO.
   *
   * @param myId The ID of the MPC party.
   * @param baseOt OT functionality for the base OTs
   * @param drbg Secure bit randomness generator
   * @param otBatchSize The amount of OTs to preprocess in a batch
   * @param tinyTablesFile file for data
   */
  public TinyTablesPreproResourcePool(int myId, TinyTablesOt baseOt, Drbg drbg,
      int computationalSecurity, int statisticalSecurity,
      int otBatchSize, File tinyTablesFile, Supplier<Network> network) {
    super(myId, 2);
    this.unprocessedAnds = Collections.synchronizedList(new ArrayList<>());
    this.storage = new TinyTablesStorageImpl();
    this.tinyTablesFile = tinyTablesFile;
    this.drng = new DrngImpl(drbg);
    this.supplier = () -> {
      RotList rotList = new RotList(drbg, computationalSecurity);
      CoinTossing ct = new CoinTossing(myId, Util.otherPlayerId(myId), drbg);
      OtExtensionResourcePool otExtRes = new OtExtensionResourcePoolImpl(myId,
          Util.otherPlayerId(myId),
          computationalSecurity, statisticalSecurity, 1, drbg, ct, rotList);
      baseOt.init(network.get());
      int otherId = Util.otherPlayerId(getMyId());
      // Execute random seed OTs
      if (getMyId() < otherId) {
        rotList.send(baseOt);
        rotList.receive(baseOt);
      } else {
        rotList.receive(baseOt);
        rotList.send(baseOt);
      }
      ct.initialize(network.get());
      // Setup the OT extension
      RotFactory rotFactory = new RotFactory(otExtRes, network.get());
      BristolOtFactory otFactory = new BristolOtFactory(rotFactory, otExtRes, network.get(),
          otBatchSize);
      TinyTablesTripleGenerator generator =
          new TinyTablesTripleGenerator(getMyId(), getDrng(), otFactory);
      return new BatchTinyTablesTripleProvider(generator, TRIP_BATCH_SIZE);
    };
  }

  public Drng getDrng() {
    return drng;
  }

  public void addAndProtocol(TinyTablesPreproANDProtocol protocol) {
    this.unprocessedAnds.add(protocol);
  }

  public List<TinyTablesPreproANDProtocol> getUnprocessedAnds() {
    return unprocessedAnds;
  }

  void calculateTinyTables(Network network) {
    int unprocessedGates = this.unprocessedAnds.size();
    /*
     * Sort the unprocessed gates to make sure that the players process them in the same order.
     */
    this.unprocessedAnds.sort(Comparator.comparingInt(TinyTablesPreproProtocol::getId));

    // Two bits per gate
    TinyTablesElementVector shares = new TinyTablesElementVector(unprocessedGates * 2);
    List<TinyTablesTriple> usedTriples = new ArrayList<>();
    for (int i = 0; i < unprocessedGates; i++) {
      TinyTablesPreproANDProtocol gate = this.unprocessedAnds.get(i);
      if (tinyTablesTripleProvider == null) {
        tinyTablesTripleProvider = supplier.get();
      }
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

    byte[] size = ByteBuffer.allocate(Integer.BYTES).putInt(shares.getSize()).array();
    // send
    network.send(Util.otherPlayerId(getMyId()), size);
    network.send(Util.otherPlayerId(this.getMyId()), shares.payload());

    // receive
    size = network.receive(Util.otherPlayerId(this.getMyId()));
    int length = ByteBuffer.wrap(size).getInt();
    byte[] data = network.receive(Util.otherPlayerId(this.getMyId()));
    TinyTablesElementVector otherShares = new TinyTablesElementVector(data, length);

    RegularBitVector open = TinyTablesElementVector.open(shares, otherShares);

    for (int i = 0; i < unprocessedGates; i++) {
      TinyTablesPreproANDProtocol gate = this.unprocessedAnds.get(i);
      boolean e = open.getBit(2 * i);
      boolean d = open.getBit(2 * i + 1);

      TinyTablesElement product = TinyTablesElement.finalizeMultiplication(e, d, usedTriples.get(i),
          this.getMyId());

      TinyTable tinyTable = gate.calculateTinyTable(getMyId(), product);

      this.storage.storeTinyTable(gate.getId(), tinyTable);
    }

    this.unprocessedAnds.clear();
  }

  public void closeEvaluation() {
    if (tinyTablesTripleProvider != null) {
      tinyTablesTripleProvider.close();
    }
    /*
     * Store the TinyTables to a file.
     */
    ExceptionConverter.safe(() -> {
      storeTinyTables(storage, tinyTablesFile);
      LOGGER.info("TinyTables stored to " + tinyTablesFile);
      return null;
    }, "Failed to store TinyTables");
  }

  private void storeTinyTables(TinyTablesStorage tinyTablesStorage, File file) throws IOException {
    file.createNewFile();
    FileOutputStream fout = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(tinyTablesStorage);
    oos.close();
  }

  public TinyTablesStorage getStorage() {
    return storage;
  }
}

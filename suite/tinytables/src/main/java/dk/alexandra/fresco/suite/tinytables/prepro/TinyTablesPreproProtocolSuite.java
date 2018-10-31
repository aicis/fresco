package dk.alexandra.fresco.suite.tinytables.prepro;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.ot.TinyTablesOt;
import dk.alexandra.fresco.suite.tinytables.storage.BatchTinyTablesTripleProvider;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.ot.otextension.BristolOtFactory;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;
import dk.alexandra.fresco.tools.ot.otextension.RotFactory;
import dk.alexandra.fresco.tools.ot.otextension.RotList;

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
 * calculate a so-called <i>TinyTable</i> which is used in the online phase (see {@link
 * TinyTablesProtocolSuite}). This is done using oblivious transfer. To enhance performance, all
 * oblivious transfers are done at the end of the preprocessing (see {@link
 * #createRoundSynchronization}).
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
 */
public class TinyTablesPreproProtocolSuite
    implements ProtocolSuite<TinyTablesPreproResourcePool, ProtocolBuilderBinary> {

  private static final int OT_BATCH_SIZE = 16000;

  public TinyTablesPreproProtocolSuite() {
  }

  @Override
  public BuilderFactory<ProtocolBuilderBinary> init(
      TinyTablesPreproResourcePool resourcePool,
      Network network) {
    int computationalSecurity = resourcePool.getComputationalSecurity();
    int statisticalSecurity = resourcePool.getStatisticalSecurity();
    int myId = resourcePool.getMyId();
    int otherId = Util.otherPlayerId(myId);

    Drbg random = resourcePool.getSecureRandom();
    TinyTablesOt baseOt = resourcePool.getBaseOt();
    baseOt.init(network);
    CoinTossing ct = new CoinTossing(myId, otherId, random);
    ct.initialize(network);
    // Execute random seed OTs
    RotList currentSeedOts = new RotList(random, computationalSecurity);
    if (myId < otherId) {
      currentSeedOts.send(baseOt);
      currentSeedOts.receive(baseOt);
    } else {
      currentSeedOts.receive(baseOt);
      currentSeedOts.send(baseOt);
    }
    // Setup the OT extension
    OtExtensionResourcePool resources = new OtExtensionResourcePoolImpl(myId, otherId,
        computationalSecurity, statisticalSecurity, 1, random, ct, currentSeedOts);
    BristolOtFactory otFactory = new BristolOtFactory(new RotFactory(resources, network), resources,
        network, OT_BATCH_SIZE);

    resourcePool.setTripleGenerator(
        new BatchTinyTablesTripleProvider(
            new TinyTablesTripleGenerator(myId, random, otFactory),
            OT_BATCH_SIZE));

    return new TinyTablesPreproBuilderFactory();
  }

  @Override
  public RoundSynchronization<TinyTablesPreproResourcePool> createRoundSynchronization() {
    return new PreProRoundSync();
  }

  private class PreProRoundSync implements
      RoundSynchronization<TinyTablesPreproResourcePool> {

    /**
     * The number of unprocessed AND protocols to collect before we compute their TinyTables.
     */
    private static final int UNPROCESSED_BUFFER_SIZE = 1000;

    @Override
    public void beforeBatch(
        ProtocolCollection<TinyTablesPreproResourcePool> protocols,
        TinyTablesPreproResourcePool resourcePool,
        Network network) {
      // Ignore
    }

    @Override
    public void finishedBatch(int gatesEvaluated,
        TinyTablesPreproResourcePool resourcePool, Network network) {
      if (resourcePool.getUnprocessedAnds().size() > UNPROCESSED_BUFFER_SIZE) {
        resourcePool.calculateTinyTables(network);
      }
    }

    @Override
    public void finishedEval(TinyTablesPreproResourcePool resourcePool, Network network) {
      resourcePool.calculateTinyTables(network);
      resourcePool.closeEvaluation();
    }
  }


}

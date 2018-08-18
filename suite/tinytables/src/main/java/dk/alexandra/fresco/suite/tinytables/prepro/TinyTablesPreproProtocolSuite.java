package dk.alexandra.fresco.suite.tinytables.prepro;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.ot.base.BaseOTFactory;
import dk.alexandra.fresco.suite.tinytables.ot.extension.SemiHonestOTExtensionFactory;
import dk.alexandra.fresco.suite.tinytables.storage.BatchTinyTablesTripleProvider;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;
import java.security.SecureRandom;

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


  private final SecureRandom secRand;

  public TinyTablesPreproProtocolSuite() {
    this.secRand = new SecureRandom();
  }

  public SecureRandom getSecureRandom() {
    return secRand;
  }

  @Override
  public BuilderFactory<ProtocolBuilderBinary> init(
      TinyTablesPreproResourcePool resourcePool,
      Network network) {
    OTFactory otFactory = new SemiHonestOTExtensionFactory(network, resourcePool.getMyId(), 128,
        new BaseOTFactory(network, resourcePool.getMyId(), secRand), secRand);

    resourcePool.setTripleGenerator(
        new BatchTinyTablesTripleProvider(
            new TinyTablesTripleGenerator(resourcePool.getMyId(), secRand, otFactory), 8192));

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

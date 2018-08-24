package dk.alexandra.fresco.suite.tinytables.util;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.ot.datatypes.OTInput;
import dk.alexandra.fresco.suite.tinytables.ot.datatypes.OTSigma;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class TinyTablesTripleGenerator {

  private int playerId;
  private OTFactory otFactory;
  private SecureRandom random;

  /**
   * Creates a new triple generator.
   *
   * @param playerId the id of the player to generate triples for
   * @param random a source of randomness
   * @param otFactory a factory for OT's
   */
  public TinyTablesTripleGenerator(int playerId, SecureRandom random, OTFactory otFactory) {
    this.playerId = playerId;
    this.random = random;
    this.otFactory = otFactory;
  }

  /**
   * Generate new multiplication triples (a,b,c). The two players need to call this method at the
   * same time and with the same amount parameter.
   */
  public List<TinyTablesTriple> generate(int amount) {

    List<TinyTablesTriple> triples = new ArrayList<>();

    if (playerId == 1) {
      List<OTInput> otInputs = new ArrayList<>();
      for (int i = 0; i < amount; i++) {
        // Pick random shares of a and b
        boolean a = random.nextBoolean();
        boolean b = random.nextBoolean();
        // Masks for the OTs
        boolean x = random.nextBoolean();
        boolean y = random.nextBoolean();
        otInputs.add(new OTInput(x, x ^ a));
        otInputs.add(new OTInput(y, y ^ b));
        boolean c = a & b ^ x ^ y;
        triples.add(TinyTablesTriple.fromShares(a, b, c));
      }

      OTSender sender = otFactory.createOTSender();
      sender.send(otInputs);
    }
    if (playerId == 2) {
      List<OTSigma> otSigmas = new ArrayList<>();
      for (int i = 0; i < amount; i++) {
        /*
         * Pick random shares of a and b and use them for sigmas in the OT's:
         */
        boolean a = random.nextBoolean();
        boolean b = random.nextBoolean();
        otSigmas.add(new OTSigma(b));
        otSigmas.add(new OTSigma(a));
        // We don't know c until after we have done the OT's
        triples.add(TinyTablesTriple.fromShares(a, b, false));
      }
      OTReceiver receiver = otFactory.createOTReceiver();
      List<BitSet> results = receiver.receive(otSigmas, 1);

      for (int i = 0; i < amount; i++) {
        TinyTablesTriple trip = triples.get(i);
        boolean c = results.get(2 * i).get(0) ^ results.get(2 * i + 1).get(0)
            ^ trip.getA().getShare() & trip.getB().getShare();
        triples.set(i,
            TinyTablesTriple.fromShares(trip.getA().getShare(), trip.getB().getShare(), c));
      }
    }
    return triples;
  }

}

package dk.alexandra.fresco.suite.tinytables.util;

import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.tools.ot.base.Ot;
import java.util.ArrayList;
import java.util.List;

public class TinyTablesTripleGenerator {

  private final int playerId;
  private final Ot ot;
  private final Drng random;

  /**
   * Creates a new triple generator.
   *
   * @param playerId the id of the player to generate triples for
   * @param random a source of randomness
   * @param ot class for executing OTs
   */
  public TinyTablesTripleGenerator(int playerId, Drbg random, Ot ot) {
    this.playerId = playerId;
    this.random = new DrngImpl(random);
    this.ot = ot;
  }

  /**
   * Generate new multiplication triples (a,b,c). The two players need to call this method at the
   * same time and with the same amount parameter.
   */
  public List<TinyTablesTriple> generate(int amount) {

    List<TinyTablesTriple> triples = new ArrayList<>();

    if (playerId == 1) {
      // StrictBitVector only supports bitvectors of and 8 multiple
      StrictBitVector zeroMessage = new StrictBitVector(8);
      StrictBitVector oneMessage = new StrictBitVector(8);
      for (int i = 0; i < amount; i++) {
        // Pick random shares of a and b
        boolean a = random.nextBit();
        boolean b = random.nextBit();
        // Masks for the OTs
        boolean x = random.nextBit();
        boolean y = random.nextBit();

        zeroMessage.setBit(0, x);
        oneMessage.setBit(0, x ^ a);
        ot.send(zeroMessage, oneMessage);
        zeroMessage.setBit(0, y);
        oneMessage.setBit(0, y ^ b);
        ot.send(zeroMessage, oneMessage);
        boolean c = a & b ^ x ^ y;
        triples.add(TinyTablesTriple.fromShares(a, b, c));

      }
    }
    if (playerId == 2) {
      for (int i = 0; i < amount; i++) {
        /*
         * Pick random shares of a and b and use them for sigmas in the OT's:
         */
        boolean a = random.nextBit();
        boolean b = random.nextBit();
        StrictBitVector bMessage = ot.receive(b);
        StrictBitVector aMessage = ot.receive(a);

        // We don't know c until after we have done the OT's
        TinyTablesTriple trip = TinyTablesTriple.fromShares(a, b, false);
        boolean c = aMessage.getBit(0) ^ bMessage.getBit(0) ^ trip.getA().getShare() & trip.getB()
            .getShare();
        trip = TinyTablesTriple.fromShares(trip.getA().getShare(), trip.getB().getShare(), c);
        triples.add(trip);
      }
    }
    return triples;
  }
}

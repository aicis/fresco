package dk.alexandra.fresco.tools.mascot.ot;

import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.List;

public interface OTBatch<T> {

  public void send(List<Pair<T, T>> messagePairs);

  public List<T> receive(BigInteger choiceBits, int numBits);

}

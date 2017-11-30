package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.framework.util.Pair;

public interface OtBatch<T> {

  public void send(List<Pair<T, T>> messagePairs)
      throws MaliciousOtException, FailedOtException;;

  public List<T> receive(BigInteger choiceBits, int numBits)
      throws MaliciousOtException, FailedOtException;;

}

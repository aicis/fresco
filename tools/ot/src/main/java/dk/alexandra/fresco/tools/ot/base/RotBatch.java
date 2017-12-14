package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.List;

public interface RotBatch<T> {

  List<Pair<T, T>> send(int numMessages, int messageSize);

  List<T> receive(StrictBitVector choiceBits, int messageSize);
  
}

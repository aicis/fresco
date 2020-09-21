package dk.alexandra.fresco.lib.common.util;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import java.util.List;

public class RowPair<T, S> extends Pair<DRes<List<T>>, DRes<List<S>>> {
  public RowPair(DRes<List<T>> first, DRes<List<S>> second) {
    super(first, second);
  }
}

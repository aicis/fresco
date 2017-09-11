package dk.alexandra.fresco.framework.util;

import java.util.List;

import dk.alexandra.fresco.framework.DRes;

public class RowPair<T, S> extends Pair<DRes<List<T>>, DRes<List<S>>> {
  public RowPair(DRes<List<T>> first, DRes<List<S>> second) {
    super(first, second);
  }
}

package dk.alexandra.fresco.framework.util;

import java.util.List;

import dk.alexandra.fresco.framework.DRes;

public class RowPairD<T, S> extends RowPair<DRes<T>, DRes<S>> {
  public RowPairD(DRes<List<DRes<T>>> first, DRes<List<DRes<S>>> second) {
    super(first, second);
  }
}

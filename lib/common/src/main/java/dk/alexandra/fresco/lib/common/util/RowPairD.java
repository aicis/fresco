package dk.alexandra.fresco.lib.common.util;

import dk.alexandra.fresco.framework.DRes;
import java.util.List;

public class RowPairD<T, S> extends RowPair<DRes<T>, DRes<S>> {
  public RowPairD(DRes<List<DRes<T>>> first, DRes<List<DRes<S>>> second) {
    super(first, second);
  }
}

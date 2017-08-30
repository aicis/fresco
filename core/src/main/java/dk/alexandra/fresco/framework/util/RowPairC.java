package dk.alexandra.fresco.framework.util;
import java.util.ArrayList;

import dk.alexandra.fresco.framework.Computation;

public class RowPairC<T, S> extends RowPair<Computation<T>, Computation<S>> {
  public RowPairC(ArrayList<Computation<T>> first, ArrayList<Computation<S>> second) {
    super(first, second);
  }
}
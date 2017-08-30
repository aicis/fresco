package dk.alexandra.fresco.framework.util;

import java.util.ArrayList;

public class RowPair<T, S> extends Pair<ArrayList<T>, ArrayList<S>> {
  public RowPair(ArrayList<T> first, ArrayList<S> second) {
    super(first, second);
  }
}

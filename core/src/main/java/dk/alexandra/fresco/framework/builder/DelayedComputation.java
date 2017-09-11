package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.DRes;

public class DelayedComputation<R> implements DRes<R> {

  private DRes<R> closure;

  public void setComputation(DRes<R> computation) {
    if (this.closure != null) {
      throw new IllegalStateException("Only allowed once");
    }
    this.closure = computation;
  }

  @Override
  public R out() {
    return closure.out();
  }
}

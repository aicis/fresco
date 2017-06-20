package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;

public class DelayedComputation<R> implements Computation<R> {

  private Computation<R> closure;

  public void setComputation(Computation<R> computation) {
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

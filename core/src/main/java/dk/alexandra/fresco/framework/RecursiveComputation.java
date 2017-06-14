package dk.alexandra.fresco.framework;

import java.util.Objects;

/**
 * Generic super class for building recursive computations (replacing the old
 * round based protocol producer interface. Enables propagating input through layers of
 * recusive computations
 *
 * @param <OutputT> the output type
 */
public class RecursiveComputation<OutputT> implements Computation<OutputT> {

  private Result<OutputT> result;

  /**
   * Initialization constructor - only use once and first.
   */
  protected RecursiveComputation() {
    this.result = new Result<>();
  }

  /**
   * Sub sequent constructor - never use first.
   */
  protected RecursiveComputation(RecursiveComputation<OutputT> previousComputation) {
    this.result = previousComputation.result;
  }

  @Override
  public OutputT out() {
    return Objects.requireNonNull(result.value);
  }

  protected void setResult(OutputT result) {
    if (this.result.value != null) {
      throw new IllegalStateException("Cannot set output more than once");
    }
    this.result.value = result;
  }

  private static class Result<OutputT> {

    private OutputT value;
  }

}

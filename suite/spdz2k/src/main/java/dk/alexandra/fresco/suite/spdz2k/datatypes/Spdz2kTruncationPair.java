package dk.alexandra.fresco.suite.spdz2k.datatypes;

/**
 * Sdpz2k representation of a truncation pair. <p> A truncation pair is pre-processing material
 * used for probabilistic truncation. A truncation pair consists of a value r and r^{prime} such
 * that r^{prime} is a random element and r = r^{prime} / 2^{d}, i.e., r right-shifted by d.</p>
 */
public class Spdz2kTruncationPair<PlainT extends CompUInt<?, ?, PlainT>> {
  private final Spdz2kSIntArithmetic<PlainT> rPrime;
  private final Spdz2kSIntArithmetic<PlainT> r;

  public Spdz2kTruncationPair(
      Spdz2kSIntArithmetic<PlainT> rPrime,
      Spdz2kSIntArithmetic<PlainT> r) {
    this.rPrime = rPrime;
    this.r = r;
  }

  public Spdz2kSIntArithmetic<PlainT> getR() {
    return r;
  }

  public Spdz2kSIntArithmetic<PlainT> getRPrime() {
    return rPrime;
  }
}

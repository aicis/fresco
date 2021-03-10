package dk.alexandra.fresco.tools.bitTriples;

/**
 * The security parameters used throughout the MASCOT protocol MASCOT protocol (<a
 * href="https://eprint.iacr.org/2016/505.pdf">https://eprint.iacr.org/2016/505.pdf</a>).
 */
public class BitTripleSecurityParameters {

  private final int computationalSecurityBitParameter;
  private final int statisticalSecurityParameterBytes;
  private final int prgSeedLength;

  /**
   * Creates new {@link BitTripleSecurityParameters}.
   *
   * @param computationalSecurityBitParameter OT security parameter in bits (lambda in Mascot paper)
   * @param prgSeedBitLength bit length of seed used to underlying prg
   *     (tau in Mascot paper) For each triple we generate, we will generate and
   *     numCandidatesPerTriple - 1 triples for a single right factor and sacrifice these to
   *     authenticate the triple.
   */
  public BitTripleSecurityParameters(
      int computationalSecurityBitParameter, int statisticalSecurityByteParameter, int prgSeedBitLength) {
    this.computationalSecurityBitParameter = computationalSecurityBitParameter;
    this.statisticalSecurityParameterBytes = statisticalSecurityByteParameter;
    this.prgSeedLength = prgSeedBitLength;
  }

  /**
   * Creates new {@link BitTripleSecurityParameters} with realistic parameters (based on paper
   * recommendations).
   */
  public BitTripleSecurityParameters() {
    this(256, 40, 256);
  }

  /**
   * Gets OT security parameter num bits (lambda in Mascot paper).
   *
   * @return lambda security parameter
   */
  public int getComputationalSecurityBitParameter() {
    return computationalSecurityBitParameter;
  }

  /**
   * Gets OT security parameter num bits (lambda in Mascot paper).
   *
   * @return lambda security parameter
   */
  public int getStatisticalSecurityByteParameter() {
    return statisticalSecurityParameterBytes;
  }

  /**
   * Gets bit length of seed used to underlying prg.
   *
   * @return prg seed bit length
   */
  public int getPrgSeedBitLength() {
    return prgSeedLength;
  }
}

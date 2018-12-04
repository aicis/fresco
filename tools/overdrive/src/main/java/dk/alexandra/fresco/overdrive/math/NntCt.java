package dk.alexandra.fresco.overdrive.math;

import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the number theoretic transform using a simple recursive Cooley Tukey radix-2
 * approach.
 *
 * <p>
 * Note: While this seem fine for now, there is a lot of FFT optimizations that could be applied.
 * </p>
 */
final class NntCt implements NumberTheoreticTransform {

  private static Map<Pair<BigInteger, BigInteger>, NntCt> instanceMap = new HashMap<>();
  private ArrayList<BigInteger> powerTable;
  private ArrayList<BigInteger> powerTableInverse;
  private BigInteger root;
  private BigInteger rootInverse;
  private BigInteger modulus;

  private NntCt(BigInteger root, BigInteger modulus) {
    this.root = root;
    this.modulus = modulus;
    this.rootInverse = root.modInverse(modulus);
  }

  static NntCt getInstance(BigInteger root, BigInteger modulus) {
    Pair<BigInteger, BigInteger> key = new Pair<>(root, modulus);
    return instanceMap.computeIfAbsent(key, k -> new NntCt(k.getFirst(), k.getSecond()));
  }

  @Override
  public List<BigInteger> nnt(List<BigInteger> coefficients) {
    if (coefficients.isEmpty()) {
      throw new IllegalArgumentException("Coefficient list is empty.");
    }
    isTwoPower(coefficients.size());
    ensurePowerTables(coefficients.size());
    return nntCtRecursive(coefficients, 0, 1, false);
  }

  @Override
  public List<BigInteger> nntInverse(List<BigInteger> evaluations) {
    if (evaluations.isEmpty()) {
      throw new IllegalArgumentException("Evaluations list is empty.");
    }
    isTwoPower(evaluations.size());
    ensurePowerTables(evaluations.size() * 2);
    List<BigInteger> coefficients = nntCtRecursive(evaluations, 0, 1, true);
    BigInteger inv = BigInteger.valueOf(evaluations.size()).modInverse(modulus);
    for (int i = 0; i < evaluations.size(); i++) {
      BigInteger scaled = coefficients.get(i).multiply(inv).mod(modulus);
      coefficients.set(i, scaled);
    }
    return coefficients;
  }

  private List<BigInteger> nntCtRecursive(List<BigInteger> coefficients, int offset, int stride,
      boolean isInverse) {
    if (coefficients.size() == stride) {
      return Collections.singletonList(coefficients.get(offset));
    }
    List<BigInteger> even = nntCtRecursive(coefficients, offset, stride * 2, isInverse);
    List<BigInteger> odd = nntCtRecursive(coefficients, offset + stride, stride * 2, isInverse);
    List<BigInteger> merged = new ArrayList<>(Collections.nCopies(even.size() + odd.size(), null));
    for (int i = 0; i < even.size(); i++) {
      BigInteger e = even.get(i);
      BigInteger o = odd.get(i);
      int pwr = i * stride;
      merged.set(i, e.add(o.multiply(getPowerOf(isInverse, pwr))).mod(modulus));
      merged.set(i + even.size(), e.subtract(o.multiply(getPowerOf(isInverse, pwr))).mod(modulus));
    }
    return merged;
  }

  private BigInteger getPowerOf(boolean isInverse, int i) {
    return isInverse ? powerTableInverse.get(i) : powerTable.get(i);
  }

  private void ensurePowerTables(int length) {
    if (this.powerTable == null) {
      this.powerTable = new ArrayList<>(length);
    }
    if (this.powerTableInverse == null) {
      this.powerTableInverse = new ArrayList<>(length);
    }
    ensurePowerTables(length, root, powerTable);
    ensurePowerTables(length, rootInverse, powerTableInverse);
  }

  private void ensurePowerTables(int length, BigInteger base, ArrayList<BigInteger> table) {
    int size = table.size();
    if (size < length) {
      BigInteger tmp = table.isEmpty() ? BigInteger.ONE : table.get(size - 1);
      table.ensureCapacity(length);
      for (int i = size; i < length - size; i++) {
        table.add(tmp);
        tmp = tmp.multiply(base).mod(modulus);
      }
    }
  }

  private static void isTwoPower(int size) {
    boolean isTwoPower = size > 1 && (size & (size - 1)) == 0;
    if (!isTwoPower) {
      throw new IllegalArgumentException(
          "Number of coefficients must be larger than 1 and a power of two, but was " + size);
    }
  }

}

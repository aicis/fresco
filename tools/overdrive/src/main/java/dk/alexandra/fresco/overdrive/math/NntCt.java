package dk.alexandra.fresco.overdrive.math;

import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NntCt implements NumberTheoreticTransform {

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

  public static NntCt getInstance(BigInteger root, BigInteger modulus) {
    Pair<BigInteger, BigInteger> key = new Pair<>(root, modulus);
    return instanceMap.computeIfAbsent(key, k -> new NntCt(k.getFirst(), k.getSecond()));
  }

  @Override
  public List<BigInteger> nnt(List<BigInteger> coefficients) {
    ensurePowerTables(coefficients.size());
    return nntCtRecursive(coefficients, 0, 1, false);
  }

  @Override
  public List<BigInteger> nntInverse(List<BigInteger> coefficients) {
    ensurePowerTables(coefficients.size());
    return nntCtRecursive(coefficients, 0, 1, true);
  }

  List<BigInteger> nntNaive(List<BigInteger> coefficients) {
    CoefficientRingPoly poly = new CoefficientRingPoly(coefficients, modulus);
    List<BigInteger> evaluations = new ArrayList<>(coefficients.size());
    BigInteger evaluationPoint = BigInteger.ONE;
    for (int i = 0; i < coefficients.size(); i++) {
      evaluations.add(poly.eval(evaluationPoint));
      evaluationPoint = evaluationPoint.multiply(root).mod(modulus);
    }
    return evaluations;
  }

  List<BigInteger> nntCtRecursive(List<BigInteger> coefficients, int offset, int stride,
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
      merged.set(i + even.size(),e.subtract(o.multiply(getPowerOf(isInverse, pwr))).mod(modulus));
    }
    return merged;
  }

  private BigInteger getPowerOf(boolean isInverse, int i) {
    return isInverse ? powerTableInverse.get(i) : powerTable.get(i);
  }

  private void ensurePowerTables(int length) {
    if (this.powerTable == null) {
      this.powerTable = new ArrayList<>(length < 0 ? 0 : length);
    }
    if (this.powerTableInverse == null) {
      this.powerTableInverse = new ArrayList<>(length < 0 ? 0 : length);
    }
    ensurePowerTables(length, root, powerTable);
    ensurePowerTables(length, rootInverse, powerTableInverse);
  }

  private void ensurePowerTables(int length, BigInteger base, ArrayList<BigInteger> table) {
    int size = table.size() * 2;
    if (size < length) {
      BigInteger tmp = table.isEmpty() ? BigInteger.ONE : table.get(size - 1);
      table.ensureCapacity(length);
      for (int i = size; i < length; i++) {
        table.add(tmp);
        tmp = tmp.multiply(base).mod(modulus);
      }
    }
  }


}

/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Misc computation on OInts -- results are cached
 *
 * @author ttoft
 */
public class MiscOIntGenerators {

  private BasicNumericFactory factory;

  Map<Integer, OInt[]> coefficientsOfPolynomiums;
  OInt[] twoPowers;
  LinkedList<OInt> twoPowersList;
  // should twoPowers be a List?


  public MiscOIntGenerators(BasicNumericFactory factory) {
    this.factory = factory;
    coefficientsOfPolynomiums = new HashMap<Integer, OInt[]>();

    twoPowers = new OInt[1];
    twoPowers[0] = factory.getOInt();
    twoPowers[0].setValue(BigInteger.ONE);
    twoPowersList = new LinkedList<>();
    twoPowersList.add(twoPowers[0]);
  }


  /**
   * Generate a degree l polynomium P such that P(1) = 1 and P(i) = 0 for i in {2,3,...,l+1}
   *
   * @param factory source of OInt's
   * @param l degree of polynomium
   * @return coefficients of P
   */
  public OInt[] getPoly(int l, BigInteger modulus) {
    // check that l is positive
    Integer lInt = new Integer(l);
    OInt[] result = coefficientsOfPolynomiums.get(lInt);
    if (result == null) {
      // Generate a new set of OInts and store them...
      result = new OInt[l + 1];

      BigInteger[] coefficients = constructPolynomial(l, 1, modulus);
      for (int i = 0; i <= l; i++) {
        result[i] = factory.getOInt();
        result[i].setValue(coefficients[coefficients.length - 1 - i]);
      }

      coefficientsOfPolynomiums.put(lInt, result);
    }
    return result;
  }

  /**
   * Returns the coefficients of a polynomial of degree <i>l</i> such that
   * <i>f(m) = 1</i> and <i>f(n) = 0</i> for <i>1 &le; n &le; l+1</i> and <i>n
   * &ne; m</i> in <i>Z<sub>p</sub></i> (<i>p</i> should be set in
   * {@link #setModulus(BigInteger)}). The first element in the array is the
   * coefficient of the term with the highest degree, eg. degree <i>l</i>.
   *
   * @param l The desired degree of <i>f</i>
   * @param m The only non-zero integer point for <i>f</i> in the range <i>1,2,...,l+1</i>.
   */
  public static BigInteger[] constructPolynomial(int l, int m, BigInteger modulus) {

		/*
     * Let f_i be the polynoimial which is the product of the first i of
		 * (x-1), (x-2), ..., (x-(m-1)), (x-(m+1)), ..., (x-(l+1)). Then f_0 = 1
		 * and f_i = (x-k) f_{i-1} where k = i if i < m and k = i+1 if i >= m.
		 * Note that we are interested in calculating f(x) = f_l(x) / f_l(m).
		 *
		 * If we let f_ij denote the j'th coefficient of f_i we have the
		 * recurrence relations:
		 *
		 * f_i0 = 1 for all i (highest degree coefficient)
		 *
		 * f_ij = f_{i-1, j} - f_{i-1, j-1} * k for j = 1,...,i
		 *
		 * f_ij = 0 for j > i
		 */
    BigInteger[] f = new BigInteger[l + 1];

    // Initial value: f_0 = 1
    f[0] = BigInteger.valueOf(1);

		/*
		 * We also calculate f_i(m) in order to be able to normalize f such that
		 * f(m) = 1. Note that f_i(m) = f_{i-1}(m)(m - k) with the above notation.
		 */
    BigInteger fm = BigInteger.ONE;

    for (int i = 1; i <= l; i++) {
      int k = i;
      if (i >= m) {
        k++;
      }

      // Apply recurrence relation
      f[i] = f[i - 1].multiply(BigInteger.valueOf(-k)).mod(modulus);
      for (int j = i - 1; j > 0; j--) {
        f[j] = f[j].subtract(BigInteger.valueOf(k).multiply(f[j - 1]).mod(modulus)).mod(modulus);
      }

      fm = fm.multiply(BigInteger.valueOf(m - k)).mod(modulus);
    }

    // Scale all coefficients of f_l by f_l(m)^{-1}.
    fm = fm.modInverse(modulus);
    for (int i = 0; i < f.length; i++) {
      f[i] = f[i].multiply(fm).mod(modulus);
    }

    return f;
  }

  /**
   * Generate all two-powers 2^i for i<l
   *
   * @param length array length
   * @return Array of length l with result[i] == 2^i
   */
  public OInt[] getTwoPowers(int length) {
    if (length > twoPowers.length) {
      OInt[] newArray = new OInt[length];
      System.arraycopy(twoPowers, 0, newArray, 0, twoPowers.length);
      BigInteger currentValue = twoPowers[twoPowers.length - 1].getValue();
      for (int i = twoPowers.length; i < newArray.length; i++) {
        newArray[i] = factory.getOInt();
        currentValue = currentValue.shiftLeft(1); // multiply previous value by two
        newArray[i].setValue(currentValue);
      }
      twoPowers = newArray;
    }
    // TODO: avoid copying.... also; since OInts are mutable, perhaps we should clone.
    OInt[] result = new OInt[length];
    System.arraycopy(twoPowers, 0, result, 0, length);
    return result;
  }

  public List<OInt> getTwoPowersList(OIntFactory factory, int length) {
    if (length > twoPowersList.size()) {
      BigInteger currentValue = twoPowersList.getLast().getValue();
      while (length > twoPowersList.size()) {
        currentValue = currentValue.shiftLeft(1);
        twoPowersList.add(factory.getOInt(currentValue));
      }
    }
    return twoPowersList.subList(0, length);
  }
}

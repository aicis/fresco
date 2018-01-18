package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.arithm.ArithmeticCollectionUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FieldElementUtils extends ArithmeticCollectionUtils<FieldElement> {

  private final BigInteger modulus;
  private final int modBitLength;
  private final List<FieldElement> generators;

  /**
   * Creates new {@link FieldElementUtils}.
   *
   * @param modulus modulus for underlying field element operations
   */
  public FieldElementUtils(BigInteger modulus) {
    super();
    this.modulus = modulus;
    this.modBitLength = modulus.bitLength();
    this.generators = precomputeGenerators();
  }

  private List<FieldElement> precomputeGenerators() {
    List<FieldElement> generators = new ArrayList<>(modBitLength);
    BigInteger two = new BigInteger("2");
    BigInteger current = BigInteger.ONE;
    for (int i = 0; i < modBitLength; i++) {
      generators.add(new FieldElement(current, modulus));
      current = current.multiply(two).mod(modulus);
    }
    return generators;
  }

  /**
   * Multiplies two lists of field elements, pair-wise.
   *
   * @param leftFactors left factors
   * @param rightFactors right factors
   * @return list of products
   */
  public List<FieldElement> pairWiseMultiply(List<FieldElement> leftFactors,
      List<FieldElement> rightFactors) {
    if (leftFactors.size() != rightFactors.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    return pairWiseMultiplyStream(leftFactors, rightFactors).collect(Collectors.toList());
  }

  /**
   * Multiplies two lists of field elements, pair-wise.
   *
   * @param leftFactors left factors
   * @param rightFactors right factors
   * @return stream of products
   */
  public Stream<FieldElement> pairWiseMultiplyStream(List<FieldElement> leftFactors,
      List<FieldElement> rightFactors) {
    return IntStream.range(0, leftFactors.size()).mapToObj(idx -> {
      FieldElement l = leftFactors.get(idx);
      FieldElement r = rightFactors.get(idx);
      return l.multiply(r);
    });
  }

  /**
   * Computes inner product of two lists of field elements.
   *
   * @param left left factors
   * @param right right factors
   * @return inner product
   */
  public FieldElement innerProduct(List<FieldElement> left, List<FieldElement> right) {
    if (left.size() != right.size()) {
      throw new IllegalArgumentException("Lists must have same size");
    }
    return sum(pairWiseMultiplyStream(left, right));
  }

  /**
   * Multiplies each value in list by scalar.
   *
   * @param values list of factors
   * @param scalar scalar factor
   * @return list of products
   */
  public List<FieldElement> scalarMultiply(List<FieldElement> values, FieldElement scalar) {
    return values.stream().map(value -> value.multiply(scalar)).collect(Collectors.toList());
  }

  /**
   * Computes inner product of elements and powers of twos.<br> e0 * 2**0 + e1 * 2**1 + ... + e(n -
   * 1) * 2**(n - 1) Elements must have same modulus, otherwise we get undefined behaviour.
   *
   * @param elements elements to recombine
   * @return recombined elements
   */
  public FieldElement recombine(List<FieldElement> elements) {
    if (elements.size() > modBitLength) {
      throw new IllegalArgumentException("Number of elements cannot exceed bit-length");
    }
    BigInteger elementModulus = elements.get(0).getModulus();
    if (!elementModulus.equals(modulus)) {
      throw new IllegalArgumentException("Wrong modulus " + elementModulus);
    }
    return innerProduct(elements, generators.subList(0, elements.size()));
  }

  /**
   * Duplicates each element stretchBy times. <br> For instance, stretching [e0, e1, e2] by 2
   * results in [e0, e0, e1, e1, e3, e3].
   *
   * @param elements elements to be stretched
   * @param stretchBy number of duplications per element
   * @return stretched list
   */
  public List<FieldElement> stretch(List<FieldElement> elements, int stretchBy) {
    List<FieldElement> stretched = new ArrayList<>(elements.size() * stretchBy);
    for (FieldElement element : elements) {
      for (int c = 0; c < stretchBy; c++) {
        stretched.add(new FieldElement(element));
      }
    }
    return stretched;
  }

  /**
   * Appends padding elements to end of list numPads times.
   *
   * @param elements elements to pad
   * @param padElement element to pad with
   * @param numPads number of times to pad
   * @return padded list
   */
  public List<FieldElement> padWith(List<FieldElement> elements, FieldElement padElement,
      int numPads) {
    List<FieldElement> copy = new ArrayList<>(elements);
    copy.addAll(Collections.nCopies(numPads, padElement));
    return copy;
  }

  /**
   * Converts field elements to bit vectors and concatenates the result.
   *
   * @param elements field elements to pack
   * @param reverse indicator whether to reverse the order of bytes
   * @return concatenated field elements in bit representation
   */
  public StrictBitVector pack(List<FieldElement> elements, boolean reverse) {
    StrictBitVector[] bitVecs =
        elements.stream().map(FieldElement::toBitVector).toArray(StrictBitVector[]::new);
    return StrictBitVector.concat(reverse, bitVecs);
  }

  public StrictBitVector pack(List<FieldElement> elements) {
    return pack(elements, true);
  }

  /**
   * Unpacks a bit string into a list of field elements.
   *
   * @param packed concatenated bits representing field elements
   * @return field elements
   */
  public List<FieldElement> unpack(byte[] packed) {
    int packedBitLength = packed.length * 8;
    if ((packedBitLength % modBitLength) != 0) {
      throw new IllegalArgumentException(
          "Packed bit length must be multiple of single element bit length");
    }
    int numElements = packedBitLength / modBitLength;
    int byteLength = modBitLength / 8;
    List<FieldElement> unpacked = new ArrayList<>(numElements);
    for (int i = 0; i < numElements; i++) {
      byte[] b = Arrays.copyOfRange(packed, i * byteLength, (i + 1) * byteLength);
      FieldElement el = new FieldElement(b, modulus);
      unpacked.add(el);
    }
    return unpacked;
  }

}

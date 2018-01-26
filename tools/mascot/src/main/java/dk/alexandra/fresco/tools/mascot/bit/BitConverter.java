package dk.alexandra.fresco.tools.mascot.bit;

import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.online.OnlinePhase;
import java.util.ArrayList;
import java.util.List;

/**
 * Protocol for getting random authenticated bits from random authenticated field elements.
 */
public class BitConverter {

  private final OnlinePhase onlinePhase;
  private final FieldElement macKeyShare;
  private final MascotResourcePool resourcePool;

  /**
   * Creates new {@link BitConverter}.
   */
  public BitConverter(MascotResourcePool resourcePool, OnlinePhase onlinePhase,
      FieldElement macKeyShare) {
    this.resourcePool = resourcePool;
    this.onlinePhase = onlinePhase;
    this.macKeyShare = macKeyShare;
  }

  /**
   * Converts random authenticated elements to random authenticated bits. <p> Given random element
   * <i>[r]</i>, applies the following protocol: <ol> <li>Compute <i>[r<sup>2</sup>]</i>. <li>Open
   * to <i>r<sup>2</sup></i>. <li>Compute <i>s = &Sqrt;(r<sup>2</sup>)</i>. <li>Compute <i>[r] / s
   * </i>. This is guaranteed to be either <i>-1</i> or <i>1</i>. <li>Compute <i>(1 + [r] / s) /
   * 2</i> to convert <i>-1</i> to <i>0</i> and <i>1</i> to <i>1</i>. </ol> </p>
   *
   * @param randomElements random elements to convert
   * @return random bits
   */
  public List<AuthenticatedElement> convertToBits(List<AuthenticatedElement> randomElements) {
    List<AuthenticatedElement> squares = onlinePhase.multiply(randomElements, randomElements);
    List<FieldElement> openSquares = onlinePhase.open(squares);
    onlinePhase.triggerMacCheck();
    List<AuthenticatedElement> bits = new ArrayList<>(randomElements.size());
    for (int b = 0; b < randomElements.size(); b++) {
      FieldElement square = openSquares.get(b);
      FieldElement root = square.sqrt();
      AuthenticatedElement randomElement = randomElements.get(b);
      AuthenticatedElement oneOrNegativeOne =
          randomElement.multiply(root.modInverse()); // division
      FieldElement two =
          new FieldElement(2, resourcePool.getModulus());
      FieldElement one = new FieldElement(1, resourcePool.getModulus());
      AuthenticatedElement bit = oneOrNegativeOne.add(one, resourcePool.getMyId(), macKeyShare)
          .multiply(two.modInverse());
      bits.add(bit);
    }
    return bits;
  }

}

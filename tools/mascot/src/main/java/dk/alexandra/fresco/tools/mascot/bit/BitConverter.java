package dk.alexandra.fresco.tools.mascot.bit;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.online.OnlinePhase;
import java.util.ArrayList;
import java.util.List;

/**
 * Protocol for getting random authenticated bits from random authenticated field elements.
 */
public class BitConverter extends BaseProtocol {

  private final OnlinePhase onlinePhase;
  private final FieldElement macKeyShare;

  /**
   * Creates new {@link BitConverter}.
   */
  public BitConverter(MascotResourcePool resourcePool, Network network, OnlinePhase onlinePhase,
      FieldElement macKeyShare) {
    super(resourcePool, network);
    this.onlinePhase = onlinePhase;
    this.macKeyShare = macKeyShare;
  }

  /**
   * Converts random authenticated elements to random authenticated bits. <br> Given random element
   * [r], applies the following protocol: <br> Compute [r^2]. <br> Open to r^2. <br> Compute
   * sqrt(r^2). <br> Compute [r] / sqrt(r^2). This is guaranteed to be either -1 or 1. <br> Compute
   * (1 + [r] / sqrt(r^2)) / 2 to convert -1 to 0 and 1 to 1.
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
          new FieldElement(2, getModulus(), getModBitLength());
      FieldElement one = new FieldElement(1, getModulus(), getModBitLength());
      AuthenticatedElement bit = oneOrNegativeOne.add(one, getMyId(), macKeyShare)
          .multiply(two.modInverse());
      bits.add(bit);
    }
    return bits;
  }

}

package dk.alexandra.fresco.tools.mascot.bit;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.online.OnlinePhase;
import java.util.ArrayList;
import java.util.List;

/**
 * Protocol for getting random authenticated bits from random authenticated field elements.
 */
public class BitConverter extends BaseProtocol {

  private final ElementGeneration elementGeneration;
  private final OnlinePhase onlinePhase;
  private final FieldElement macKeyShare;

  /**
   * Creates new {@link BitConverter}.
   */
  public BitConverter(MascotResourcePool resourcePool,
      Network network, ElementGeneration elementGeneration, OnlinePhase onlinePhase,
      FieldElement macKeyShare) {
    super(resourcePool, network);
    this.elementGeneration = elementGeneration;
    this.onlinePhase = onlinePhase;
    this.macKeyShare = macKeyShare;
  }

  /**
   * Converts random authenticated elements to random bits.
   *
   * @param randomElements random elements to convert
   * @return random bits
   */
  public List<AuthenticatedElement> convertToBits(List<AuthenticatedElement> randomElements) {
    List<AuthenticatedElement> squares = onlinePhase.multiply(randomElements, randomElements);
    List<FieldElement> openSquares = elementGeneration.open(squares);
    List<AuthenticatedElement> bits = new ArrayList<>(randomElements.size());
    for (int b = 0; b < randomElements.size(); b++) {
      FieldElement square = openSquares.get(b);
      FieldElement root = square.sqrt();
      FieldElement inverted = root.modInverse();
      AuthenticatedElement randomElement = randomElements.get(b);
      AuthenticatedElement oneOrNegativeOne = randomElement.multiply(inverted);
      FieldElement twoInverted = new FieldElement(2, getModulus(), getModBitLength());
      FieldElement one = new FieldElement(1, getModulus(), getModBitLength());
      AuthenticatedElement bit = oneOrNegativeOne.add(one, getMyId(), macKeyShare)
          .multiply(twoInverted);
      bits.add(bit);
    }
    return bits;
  }

}

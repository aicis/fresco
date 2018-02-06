package dk.alexandra.fresco.suite.spdz.preprocessing;

import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import java.math.BigInteger;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.InputMask;

public class MascotFormatConverter {

  private MascotFormatConverter() {
  }

  /**
   * Converts single {@link AuthenticatedElement} to {@link SpdzElement}.
   * 
   * @param element authenticated element
   * @return spdz element
   */
  public static SpdzElement toSpdzElement(AuthenticatedElement element) {
    BigInteger share = element.getShare().toBigInteger();
    BigInteger mac = element.getMac().toBigInteger();
    return new SpdzElement(share, mac, element.getModulus());
  }

  /**
   * Converts single {@link MultiplicationTriple} to {@link SpdzTriple}.
   * 
   * @param triple triple to convert
   * @return converted triple
   */
  public static SpdzTriple toSpdzTriple(MultiplicationTriple triple) {
    SpdzElement a = toSpdzElement(triple.getLeft());
    SpdzElement b = toSpdzElement(triple.getRight());
    SpdzElement c = toSpdzElement(triple.getProduct());
    return new SpdzTriple(a, b, c);
  }

  /**
   * Converts single {@link InputMask} to {@link SpdzInputMask}.
   * 
   * @param mask to convert
   * @return converted mask
   */
  public static SpdzInputMask toSpdzInputMask(InputMask mask) {
    FieldElement openMask = mask.getOpenValue();
    if (openMask == null) {
      return new SpdzInputMask(toSpdzElement(mask.getMaskShare()));
    } else {
      return new SpdzInputMask(toSpdzElement(mask.getMaskShare()), openMask.toBigInteger());
    }
  }

}

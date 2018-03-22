package dk.alexandra.fresco.suite.spdz.preprocessing;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import java.math.BigInteger;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.InputMask;

public class MascotFormatConverter {

  private MascotFormatConverter() {
  }

  /**
   * Converts single {@link AuthenticatedElement} to {@link SpdzSInt}.
   * 
   * @param element authenticated element
   * @return spdz element
   */
  public static SpdzSInt toSpdzElement(AuthenticatedElement element) {
    BigInteger share = element.getShare().toBigInteger();
    BigInteger mac = element.getMac().toBigInteger();
    return new SpdzSInt(share, mac, element.getModulus());
  }

  /**
   * Converts single {@link MultiplicationTriple} to {@link SpdzTriple}.
   * 
   * @param triple triple to convert
   * @return converted triple
   */
  public static SpdzTriple toSpdzTriple(MultiplicationTriple triple) {
    SpdzSInt a = toSpdzElement(triple.getLeft());
    SpdzSInt b = toSpdzElement(triple.getRight());
    SpdzSInt c = toSpdzElement(triple.getProduct());
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

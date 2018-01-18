package dk.alexandra.fresco.suite.spdz;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.preprocessing.MascotFormatConverter;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import java.math.BigInteger;
import org.junit.Test;

public class TestMascotFormatConverter {

  private AuthenticatedElement getAuthElement(int shareVal, int macVal, BigInteger modulus) {
    FieldElement share = new FieldElement(shareVal, modulus);
    FieldElement mac = new FieldElement(macVal, modulus);
    return new AuthenticatedElement(share, mac, modulus);
  }

  private SpdzElement getSpdzElement(int shareVal, int macVal, BigInteger modulus) {
    return new SpdzElement(BigInteger.valueOf(shareVal), BigInteger.valueOf(macVal), modulus);
  }

  @Test
  public void convertSingleElement() {
    BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    AuthenticatedElement element = getAuthElement(100, 123, modulus);
    SpdzElement expected = getSpdzElement(100, 123, modulus);
    SpdzElement actual = MascotFormatConverter.toSpdzElement(element);
    assertEquals(expected, actual);
  }

  @Test
  public void convertTriple() {
    BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    AuthenticatedElement left = getAuthElement(1, 2, modulus);
    AuthenticatedElement right = getAuthElement(3, 4, modulus);
    AuthenticatedElement product = getAuthElement(5, 6, modulus);
    MultTriple triple = new MultTriple(left, right, product);
    SpdzTriple expected = new SpdzTriple(getSpdzElement(1, 2, modulus),
        getSpdzElement(3, 4, modulus), getSpdzElement(5, 6, modulus));
    SpdzTriple actual = MascotFormatConverter.toSpdzTriple(triple);
    assertEquals(expected, actual);
  }

}

package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticBuilderFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestPolynomial {

  @Test(expected = IndexOutOfBoundsException.class)
  public void testPolynomial() throws NoSuchAlgorithmException {
    BasicNumericContext dummyFact =
        new BasicNumericContext(8, BigInteger.valueOf(1001), new DummyArithmeticResourcePoolImpl(1,
            1, new HmacDrbg(), BigInteger.ONE));
    BuilderFactoryNumeric builderFactory = new DummyArithmeticBuilderFactory(dummyFact);
    Numeric numeric = builderFactory.createNumeric(builderFactory.createSequential());

    int[] coefficients = new int[] {1, 2, 3, 4};

    List<DRes<SInt>> secretCoefficients = Arrays.stream(coefficients).mapToObj(BigInteger::valueOf)
        .map(numeric::known).collect(Collectors.toList());

    Polynomial p = new PolynomialImpl(secretCoefficients);

    Assert.assertThat(p.getMaxDegree(), Is.is(4));
    // for(int i = 0; i< 4; i++) {
    // p.setCoefficient(i, new DummyArithmeticSInt(i));
    // }

    for (int i = 0; i < 4; i++) {
      Assert.assertThat(p.getCoefficient(i), Is.is(secretCoefficients.get(i)));
    }

    p.getCoefficient(5);
  }

}


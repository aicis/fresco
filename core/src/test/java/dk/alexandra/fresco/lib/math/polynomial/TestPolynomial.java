package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticBuilderFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestPolynomial {

  @Test(expected = IndexOutOfBoundsException.class)
  public void testPolynomial() {
    BasicNumericContext dummyFact = new BasicNumericContext(8, BigInteger.valueOf(1001),
        new DummyArithmeticResourcePoolImpl(1, 1, null, new Random(), new SecureRandom(),
            BigInteger.ONE));
    BuilderFactoryNumeric builderFactory = new DummyArithmeticBuilderFactory(dummyFact);
    NumericBuilder numeric = builderFactory.createNumericBuilder(builderFactory.createSequential());

    int[] coefficients = new int[]{1, 2, 3, 4};

    List<Computation<SInt>> secretCoefficients =
        Arrays.stream(coefficients)
            .mapToObj(BigInteger::valueOf)
            .map(numeric::known)
            .collect(Collectors.toList());

    Polynomial p = new PolynomialImpl(secretCoefficients);

    Assert.assertThat(p.getMaxDegree(), Is.is(4));
    // for(int i = 0; i< 4; i++) {
    //  p.setCoefficient(i, new DummyArithmeticSInt(i));  
    // }

    for (int i = 0; i < 4; i++) {
      Assert.assertThat(p.getCoefficient(i), Is.is(secretCoefficients.get(i)));
    }

    p.getCoefficient(5);
  }

}


package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import org.junit.Assert;

class RealTestUtils {

  private RealTestUtils() {
    // Should not be instantiated
  }

  static void assertEqual(
      FieldDefinition definition, BigDecimal a,
      BigDecimal b, int precision) {
    BigDecimal decimalModulo =
        new BigDecimal(definition.getModulus())
            .setScale(precision, RoundingMode.HALF_UP)
            .divide(new BigDecimal(BigInteger.valueOf(2).pow(precision)), RoundingMode.HALF_UP);
    if (b.compareTo(decimalModulo.divide(BigDecimal.valueOf(2))) > 0) {
      b = b.subtract(decimalModulo);
    }
    BigDecimal bound = BigDecimal.ONE.setScale(precision, RoundingMode.HALF_UP)
        .divide(BigDecimal.valueOf(2.0).pow(precision), RoundingMode.HALF_UP);
    BigDecimal d = a.subtract(b).abs();
    Assert.assertTrue(a + " == " + b + " +/- 2^"
        + ceilLog2(d) + " but expected precision " + precision, d.compareTo(bound) <= 0);
  }

  static void assertEqual(
      FieldDefinition definition,
      List<BigDecimal> a, List<BigDecimal> b, int precision) {
    Assert.assertTrue("Lists must be of same size", a.size() == b.size());
    for (int i = 0; i < a.size(); i++) {
      assertEqual(definition, a.get(i), b.get(i), precision);
    }
  }

  static int floorLog2(BigDecimal value) {
    return (int) Math.floor(Math.log(value.doubleValue()) / Math.log(2.0));
  }

  static int ceilLog2(BigDecimal value) {
    return (int) Math.ceil(Math.log(value.doubleValue()) / Math.log(2.0));
  }
}

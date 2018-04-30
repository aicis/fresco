package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdz2kSIntArithmetic {
  @Test
  public void testToString() {
    Spdz2kSIntArithmetic<CompUInt128> sint = new Spdz2kSIntArithmetic<>(
        new CompUInt128(BigInteger.ONE),
        new CompUInt128(BigInteger.ONE)
    );
    Assert.assertEquals("Spdz2kSIntArithmetic{share=1, macShare=1}", sint.toString());
  }
}

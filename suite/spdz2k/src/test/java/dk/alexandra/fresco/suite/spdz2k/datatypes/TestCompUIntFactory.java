package dk.alexandra.fresco.suite.spdz2k.datatypes;

import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.junit.Test;

public final class TestCompUIntFactory {

  @Test
  public void zero() {
    CompUIntFactory factory = new CompUInt128Factory();
    CompUInt result = factory.zero();
    assertThat(result.toString(), Is.is("0"));
  }

  @Test
  public void createFromBigInteger() {
    CompUIntFactory factory = new CompUInt128Factory();
    CompUInt result = factory.createFromBigInteger(BigInteger.ZERO);
    assertThat(result.toString(), Is.is("0"));
    result = factory.createFromBigInteger(BigInteger.valueOf(113));
    assertThat(result.toString(), Is.is("113"));
  }
}

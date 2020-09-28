package dk.alexandra.fresco.logging.arithmetic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NumericLoggingDecoratorTest {

  private Numeric delegate;
  private NumericLoggingDecorator decorator;

  @Before
  public void setup() {
    delegate = mock(Numeric.class);
    decorator = new NumericLoggingDecorator(delegate);
  }

  @Test
  public void add() {
    DRes<SInt> firstArgument = mock(DRes.class);
    DRes<SInt> secondArgument = mock(DRes.class);
    decorator.add(firstArgument, secondArgument);
    verify(delegate, times(1)).add(firstArgument, secondArgument);
  }

  @Test
  public void testAdd() {
    BigInteger firstArgument = mock(BigInteger.class);
    DRes<SInt> secondArgument = mock(DRes.class);
    decorator.add(firstArgument, secondArgument);
    verify(delegate, times(1)).add(firstArgument, secondArgument);
  }

  @Test
  public void sub() {
    DRes<SInt> firstArgument = mock(DRes.class);
    DRes<SInt> secondArgument = mock(DRes.class);
    decorator.sub(firstArgument, secondArgument);
    verify(delegate, times(1)).sub(firstArgument, secondArgument);
  }

  @Test
  public void testSub() {
    BigInteger firstArgument = mock(BigInteger.class);
    DRes<SInt> secondArgument = mock(DRes.class);
    decorator.sub(firstArgument, secondArgument);
    verify(delegate, times(1)).sub(firstArgument, secondArgument);
  }

  @Test
  public void testSub1() {
    DRes<SInt> firstArgument = mock(DRes.class);
    BigInteger secondArgument = mock(BigInteger.class);
    decorator.sub(firstArgument, secondArgument);
    verify(delegate, times(1)).sub(firstArgument, secondArgument);
  }

  @Test
  public void mult() {
    DRes<SInt> firstArgument = mock(DRes.class);
    DRes<SInt> secondArgument = mock(DRes.class);
    decorator.mult(firstArgument, secondArgument);
    verify(delegate, times(1)).mult(firstArgument, secondArgument);
  }

  @Test
  public void testMult() {
    BigInteger firstArgument = mock(BigInteger.class);
    DRes<SInt> secondArgument = mock(DRes.class);
    decorator.mult(firstArgument, secondArgument);
    verify(delegate, times(1)).mult(firstArgument, secondArgument);
  }

  @Test
  public void randomBit() {
    decorator.randomBit();
    verify(delegate, times(1)).randomBit();
  }

  @Test
  public void randomElement() {
    decorator.randomElement();
    verify(delegate, times(1)).randomElement();
  }

  @Test
  public void known() {
    BigInteger firstArgument = mock(BigInteger.class);
    decorator.known(firstArgument);
    verify(delegate, times(1)).known(firstArgument);
  }

  @Test
  public void input() {
    BigInteger firstArgument = mock(BigInteger.class);
    int secondArgument = 4;
    decorator.input(firstArgument, secondArgument);
    verify(delegate, times(1)).input(firstArgument, secondArgument);
  }

  @Test
  public void open() {
    DRes<SInt> firstArgument = mock(DRes.class);
    decorator.open(firstArgument);
    verify(delegate, times(1)).open(firstArgument);
  }

  @Test
  public void testOpen() {
    DRes<SInt> firstArgument = mock(DRes.class);
    int secondArgument = 4;
    decorator.open(firstArgument, secondArgument);
    verify(delegate, times(1)).open(firstArgument, secondArgument);
  }

  @Test
  public void reset() {
    DRes<SInt> firstArgument = mock(DRes.class);
    DRes<SInt> secondArgument = mock(DRes.class);
    decorator.mult(firstArgument, secondArgument);
    decorator.add(firstArgument, secondArgument);
    decorator.sub(firstArgument, secondArgument);
    decorator.randomBit();
    decorator.randomElement();

    Assert.assertEquals(decorator.getLoggedValues().get("MULT_COUNT").intValue(), 1);
    Assert.assertEquals(decorator.getLoggedValues().get("ADD_COUNT").intValue(), 1);
    Assert.assertEquals(decorator.getLoggedValues().get("SUB_COUNT").intValue(), 1);
    Assert.assertEquals(decorator.getLoggedValues().get("BIT_COUNT").intValue(), 1);
    Assert.assertEquals(decorator.getLoggedValues().get("RANDOM_ELEMENT_COUNT").intValue(), 1);

    decorator.reset();

    Assert.assertEquals(decorator.getLoggedValues().get("MULT_COUNT").intValue(), 0);
    Assert.assertEquals(decorator.getLoggedValues().get("ADD_COUNT").intValue(), 0);
    Assert.assertEquals(decorator.getLoggedValues().get("SUB_COUNT").intValue(), 0);
    Assert.assertEquals(decorator.getLoggedValues().get("BIT_COUNT").intValue(), 0);
    Assert.assertEquals(decorator.getLoggedValues().get("RANDOM_ELEMENT_COUNT").intValue(), 0);
  }
}

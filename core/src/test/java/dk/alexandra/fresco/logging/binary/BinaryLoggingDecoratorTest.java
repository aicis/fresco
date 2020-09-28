package dk.alexandra.fresco.logging.binary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.value.SBool;
import org.junit.Assert;
import org.junit.Test;

public class BinaryLoggingDecoratorTest {

  private DRes<SBool> firstArgument;
  private int secondArgument;

  @Test
  public void open() {
    Binary binary = mock(Binary.class);
    when(binary.open(any(DRes.class), any(int.class)))
        .then(
            invocationOnMock -> {
              firstArgument = invocationOnMock.getArgument(0);
              secondArgument = invocationOnMock.getArgument(1);
              return null;
            });

    BinaryLoggingDecorator decorator = new BinaryLoggingDecorator(binary);
    DRes<SBool> toOpen = mock(DRes.class);
    decorator.open(toOpen, 2);

    Assert.assertEquals(toOpen, firstArgument);
    Assert.assertEquals(2, secondArgument);
  }

  @Test
  public void reset() {
    Binary binary = mock(Binary.class);
    when(binary.xor(any(DRes.class), any(DRes.class))).thenReturn(mock(DRes.class));
    when(binary.and(any(DRes.class), any(DRes.class))).thenReturn(mock(DRes.class));

    BinaryLoggingDecorator decorator = new BinaryLoggingDecorator(binary);
    decorator.xor(mock(DRes.class), mock(DRes.class));
    decorator.and(mock(DRes.class), mock(DRes.class));
    Assert.assertEquals(decorator.getLoggedValues().get("XOR_COUNT").intValue(), 1);
    Assert.assertEquals(decorator.getLoggedValues().get("AND_COUNT").intValue(), 1);
    decorator.reset();
    Assert.assertEquals(decorator.getLoggedValues().get("XOR_COUNT").intValue(), 0);
    Assert.assertEquals(decorator.getLoggedValues().get("AND_COUNT").intValue(), 0);
  }
}

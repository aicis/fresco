package dk.alexandra.fresco.suite.dummy.arithmetic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DummyArithmeticBuilderFactoryTest {

  private ProtocolBuilderNumeric builder;
  private BasicNumericContext context;
  private DummyArithmeticNativeProtocol<SInt> result;
  private BigInteger rolledBit;
  private FieldElement fieldElement;

  @Before
  public void setup() {
    builder = mock(ProtocolBuilderNumeric.class);
    FieldDefinition fieldDefinition = mock(FieldDefinition.class);
    when(fieldDefinition.createElement(any(BigInteger.class)))
        .thenAnswer(
            invocationOnMock -> {
              rolledBit = invocationOnMock.getArgument(0);
              fieldElement = mock(FieldElement.class);
              return fieldElement;
            });
    context = mock(BasicNumericContext.class);
    when(context.getFieldDefinition()).thenReturn(fieldDefinition);
    when(builder.append(any(DummyArithmeticNativeProtocol.class)))
        .thenAnswer(
            (invocationOnMock -> {
              result = invocationOnMock.getArgument(0);
              return null;
            }));
  }

  @Test
  public void createNumericRandomElement() {
    BuilderFactoryNumeric dummy = new DummyArithmeticBuilderFactory(context);
    Numeric numeric = dummy.createNumeric(builder);
    numeric.randomBit();
    Assert.assertNotNull(result);
    Assert.assertEquals(
        result.evaluate(0, mock(DummyArithmeticResourcePool.class), mock(Network.class)),
        EvaluationStatus.IS_DONE);
    Assert.assertTrue(rolledBit.intValue() >= 0);
    Assert.assertTrue(rolledBit.intValue() < 2);

    Assert.assertTrue(result.out().toString().contains("Mock for FieldElement"));
  }
}

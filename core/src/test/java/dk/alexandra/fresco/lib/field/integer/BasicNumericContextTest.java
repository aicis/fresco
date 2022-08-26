package dk.alexandra.fresco.lib.field.integer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BasicNumericContextTest {

  private BasicNumericContext context;
  private int maxBitLength = 16;
  private int myId = 2;
  private int noOfParties = 2;
  private FieldDefinition fieldDefinition = mock(FieldDefinition.class);
  private int precision = 32;
  private int statisticalSecurityParameter = 8;

  @Before
  public void setup() {
    when(fieldDefinition.getModulus()).thenReturn(BigInteger.ONE);
    context = new BasicNumericContext(maxBitLength, myId, noOfParties, fieldDefinition, precision,
        statisticalSecurityParameter);
  }

  @Test
  public void getPrecision() {
    Assert.assertEquals(context.getDefaultFixedPointPrecision(), precision);
  }

  @Test
  public void getMaxBitLength() {
    Assert.assertEquals(context.getMaxBitLength(), maxBitLength);
  }

  @Test
  public void getFieldDefinition() {
    Assert.assertEquals(context.getFieldDefinition(), fieldDefinition);
  }

  @Test
  public void getModulus() {
    Assert.assertEquals(context.getModulus(), BigInteger.ONE);
  }

  @Test
  public void getMyId() {
    Assert.assertEquals(context.getMyId(), myId);
  }

  @Test
  public void getNoOfParties() {
    Assert.assertEquals(context.getNoOfParties(), noOfParties);
  }

  @Test
  public void getStatisticalSecurityParameter() {
    Assert.assertEquals(context.getStatisticalSecurityParam(), statisticalSecurityParameter);
  }

}

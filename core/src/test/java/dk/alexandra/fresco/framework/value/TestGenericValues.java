package dk.alexandra.fresco.framework.value;

import java.math.BigInteger;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.MPCException;

public class TestGenericValues {

  
  
  @Test
  public void testGetAndSetOInt() {
    GenericOInt value = new GenericOInt();
    Assert.assertNull(value.getValue());
    value.setValue(BigInteger.TEN);
    Assert.assertThat(value.getValue(), Is.is(BigInteger.TEN));
  }
  
  @Test
  public void testIsReadyOInt() {
    GenericOInt value = new GenericOInt(null);
    Assert.assertFalse(value.isReady());
    value.setValue(BigInteger.ONE);
    Assert.assertTrue(value.isReady());
  }
  
  @Test
  public void testSerializationOInt() {
    GenericOInt value = new GenericOInt(BigInteger.ONE);
    Assert.assertThat(value.getSerializableContent(), Is.is(BigInteger.ONE.toByteArray()));
    value.setSerializableContent(BigInteger.TEN.toByteArray());
    Assert.assertThat(value.getSerializableContent(), Is.is(BigInteger.TEN.toByteArray()));
  }

  @Test
  public void testIsReadyOBool() {
    GenericOBool value = new GenericOBool();
    Assert.assertFalse(value.isReady());
    value.setValue(true);
    Assert.assertTrue(value.isReady());
  }
  
  @Test
  public void testSerializationOBool() {
    GenericOBool value = new GenericOBool(true);
    Assert.assertThat(value.getSerializableContent(), Is.is(new byte[]{1}));
    value.setValue(false);
    Assert.assertThat(value.getSerializableContent(), Is.is(new byte[]{0}));
    
    value.setSerializableContent(new byte[] {0});
    Assert.assertFalse(value.getValue());

    value.setSerializableContent(new byte[] {1});
    Assert.assertTrue(value.getValue());
    
    try{
      value.setSerializableContent(new byte[] {3});
    } catch (MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Cannot set a boolean value from a byte array containing 3"));
    }
    try{
      value.setSerializableContent(new byte[] {1, 1});
    } catch (MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Cannot set a boolean value from a byte array of length 2"));
    }
    //value.setSerializableContent(BigInteger.TEN.toByteArray());
    //Assert.assertThat(value.getSerializableContent(), Is.is(BigInteger.TEN.toByteArray()));
  }
  
}

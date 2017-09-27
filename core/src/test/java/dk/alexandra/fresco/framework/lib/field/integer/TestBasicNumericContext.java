package dk.alexandra.fresco.framework.lib.field.integer;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;

public class TestBasicNumericContext {

  @Test
  public void testGetters() {
  
    Random random = new Random();
    SecureRandom secureRandom = new SecureRandom();
    Network network = new KryoNetNetwork();
    
    ResourcePool resourcePool = new ResourcePoolImpl(0, 2, network, random, secureRandom);
    
    BasicNumericContext basicNumericContext = new BasicNumericContext(10, BigInteger.ONE, resourcePool);
    
    Assert.assertThat(basicNumericContext.getMaxBitLength(), Is.is(10));
    Assert.assertThat(basicNumericContext.getModulus(), Is.is(BigInteger.ONE));
    Assert.assertThat(basicNumericContext.getMyId(), Is.is(0));
    Assert.assertThat(basicNumericContext.getNoOfParties(), Is.is(2));
    
  }
}

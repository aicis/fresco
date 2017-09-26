package dk.alexandra.fresco.framework.sce.resources;

import java.security.SecureRandom;
import java.util.Random;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;

public class TestResourcePool {

  @Test
  public void testGetters() {
  
    Random random = new Random();
    SecureRandom secureRandom = new SecureRandom();
    Network network = new KryoNetNetwork();
    
    ResourcePool resourcePool = new ResourcePoolImpl(0, 2, network, random, secureRandom);
    
    Assert.assertThat(resourcePool.getRandom(), Is.is(random));
    Assert.assertThat(resourcePool.getSecureRandom(), Is.is(secureRandom));
    Assert.assertThat(resourcePool.getNetwork(), Is.is(network));
    Assert.assertThat(resourcePool.getMyId(), Is.is(0));
    Assert.assertThat(resourcePool.getNoOfParties(), Is.is(2));
  }
  
  
      
      
}

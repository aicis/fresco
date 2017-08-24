package dk.alexandra.fresco.lib.collections.sort;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;

public class TestSort {

  @Test
  public void testProtocolLayer() {
    ProtocolLayer protocolLayer = new ProtocolLayer();
    Assert.assertThat(protocolLayer.size(), Is.is(0));
    protocolLayer = new ProtocolLayer(2);
    protocolLayer.add(new ProtocolProducer() {
      
      @Override
      public boolean hasNextProtocols() {
        // TODO Auto-generated method stub
        return false;
      }
      
      @Override
      public void getNextProtocols(ProtocolCollection protocolCollection) {
        // TODO Auto-generated method stub
        
      }
    });
    Assert.assertThat(protocolLayer.size(), Is.is(1));
  }

}

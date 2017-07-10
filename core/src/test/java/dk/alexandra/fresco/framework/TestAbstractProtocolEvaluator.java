package dk.alexandra.fresco.framework;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.suite.ProtocolSuite;

public class TestAbstractProtocolEvaluator {
/* TODO
  private AbstractProtocolEvaluator evaluator;
  
  @Before
  public void setup() {
    
    evaluator = new AbstractProtocolEvaluator() {
      
      @Override
      public void setResourcePool(SCEResourcePool resourcePool) {
      }
      
      @Override
      public void setProtocolInvocation(ProtocolSuite pii) {
      }
      
      @Override
      public void processBatch(int batchId, NativeProtocol[] gates, int numOfGates) {
      }
    };
  }

  
  @Test
  public void testMaxBatchSize() {
    Assert.assertThat(evaluator.getMaxBatchSize(), Is.is(2048));
    evaluator.setMaxBatchSize(200);
    Assert.assertThat(evaluator.getMaxBatchSize(), Is.is(200));
  }
  
  @Test
  public void testEval() {
    ProtocolProducer producer = new ProtocolProducer() {

      int iterations = 0;
      @Override
      public int getNextProtocols(NativeProtocol[] protocols, int pos) {
        iterations++;
        return 0;
      }

      @Override
      public boolean hasNextProtocols() {
        if(iterations > 1) {
          return false;
        }
        return true;
      }
    };
    
    evaluator.eval(producer);
  }
  */
}

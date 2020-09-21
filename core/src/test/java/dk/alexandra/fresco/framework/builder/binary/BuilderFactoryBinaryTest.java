package dk.alexandra.fresco.framework.builder.binary;

import static org.mockito.Mockito.mock;

import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import org.junit.Assert;
import org.junit.Test;

public class BuilderFactoryBinaryTest {

  @Test
  public void createParallel() {
    Binary mockBinary = mock(Binary.class);
    BuilderFactoryBinary factoryBinary = builder -> mockBinary;
    ProtocolBuilderBinary builderBinary = factoryBinary.createParallel();

    Assert.assertEquals(mockBinary, builderBinary.binary());
    // verify result is ParallelProtocolProducer
    Assert.assertTrue(builderBinary.build() instanceof ParallelProtocolProducer);
  }
}

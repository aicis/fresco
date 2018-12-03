package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Interface for the Dummy Arithmetic suites ResourcePool.
 */
public interface DummyArithmeticResourcePool extends NumericResourcePool {

  SInt createSInt(BigIntegerI add);

  /**
   * Gets a serializer for big integer that is aligned with the current system settings in this
   * invocation - hence byte length of big integer.
   *
   * @return the serializer
   */
  ByteSerializer<BigIntegerI> getSerializer();
}

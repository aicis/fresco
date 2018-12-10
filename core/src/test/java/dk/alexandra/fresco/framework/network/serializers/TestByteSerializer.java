package dk.alexandra.fresco.framework.network.serializers;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionMersennePrime;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.FieldElementMersennePrime;
import dk.alexandra.fresco.framework.builder.numeric.ModulusMersennePrime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestByteSerializer {

  @Test
  public void testDeserializeList() {
    ModulusMersennePrime modulus = new ModulusMersennePrime(1234);
    ByteSerializer<FieldElement> serializer = new BigIntegerWithFixedLengthSerializer(2,
        new FieldDefinitionMersennePrime(modulus));
    List<byte[]> bytes = new ArrayList<>();
    bytes.add(new byte[]{1, 1});
    bytes.add(new byte[]{2, 2});
    bytes.add(new byte[]{3, 3});
    List<FieldElement> actual = serializer.deserializeList(bytes);
    List<FieldElement> expected = new ArrayList<>();
    expected.add(new FieldElementMersennePrime(257, modulus));
    expected.add(new FieldElementMersennePrime(514, modulus));
    expected.add(new FieldElementMersennePrime(771, modulus));
    Assert.assertEquals(expected, actual);
  }
}

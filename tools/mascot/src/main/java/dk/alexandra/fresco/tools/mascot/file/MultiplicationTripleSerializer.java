package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.network.serializers.StaticSizeByteSerializer;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import java.math.BigInteger;
import java.util.Arrays;

public class MultiplicationTripleSerializer extends StaticSizeByteSerializer<MultiplicationTriple> {

  private final AuthenticatedElementSerializer authElemSerializer;

  public MultiplicationTripleSerializer(BigInteger modulus) {
    this.authElemSerializer = new AuthenticatedElementSerializer(modulus);
  }

  @Override
  public int getElementSize() {
    return 3 * authElemSerializer.getElementSize();
  }

  @Override
  public byte[] serialize(MultiplicationTriple triple) {
    byte[] left = authElemSerializer.serialize(triple.getLeft());
    byte[] right = authElemSerializer.serialize(triple.getRight());
    byte[] product = authElemSerializer.serialize(triple.getProduct());
    byte[] arr = new byte[getElementSize()];
    System.arraycopy(left, 0, arr, 0, authElemSerializer.getElementSize());
    System.arraycopy(right, 0, arr, authElemSerializer.getElementSize(),
        2 * authElemSerializer.getElementSize());
    System.arraycopy(product, 0, arr, 2 * authElemSerializer.getElementSize(),
        3 * authElemSerializer.getElementSize());
    return arr;
  }

  @Override
  public MultiplicationTriple deserialize(byte[] bytes) {
    byte[] leftBytes = Arrays.copyOfRange(bytes, 0, authElemSerializer.getElementSize());
    byte[] rightBytes = Arrays.copyOfRange(bytes, authElemSerializer.getElementSize(),
        2 * authElemSerializer.getElementSize());
    byte[] productBytes = Arrays.copyOfRange(bytes, 2 * authElemSerializer.getElementSize(),
        3 * authElemSerializer.getElementSize());
    AuthenticatedElement left = authElemSerializer.deserialize(leftBytes);
    AuthenticatedElement right = authElemSerializer.deserialize(rightBytes);
    AuthenticatedElement product = authElemSerializer.deserialize(productBytes);
    MultiplicationTriple res = new MultiplicationTriple(left, right, product);
    return res;
  }

}

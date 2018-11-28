package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.network.serializers.StaticSizeByteSerializer;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.InputMask;
import java.math.BigInteger;
import java.util.Arrays;

public class InputMaskSerializer extends StaticSizeByteSerializer<InputMask> {

  private final FieldElementSerializer fieldSerializer;
  private final AuthenticatedElementSerializer authElemSerializer;

  public InputMaskSerializer(BigInteger modulus) {
    this.fieldSerializer = new FieldElementSerializer(modulus);
    this.authElemSerializer = new AuthenticatedElementSerializer(modulus);
  }

  @Override
  public int getElementSize() {
    return fieldSerializer.getElementSize() + authElemSerializer.getElementSize();
  }

  @Override
  public byte[] serialize(InputMask object) {
    byte[] openVal = fieldSerializer.serialize(object.getOpenValue());
    byte[] maskShare = authElemSerializer.serialize(object.getMaskShare());
    byte[] arr = new byte[getElementSize()];
    System.arraycopy(openVal, 0, arr, 0, fieldSerializer.getElementSize());
    System.arraycopy(maskShare, 0, arr, fieldSerializer.getElementSize(), getElementSize());
    return arr;
  }

  @Override
  public InputMask deserialize(byte[] bytes) {
    byte[] byteOpen = Arrays.copyOfRange(bytes, 0, fieldSerializer.getElementSize());
    byte[] byteMask = Arrays.copyOfRange(bytes, fieldSerializer.getElementSize(), getElementSize());
    FieldElement openVal = fieldSerializer.deserialize(byteOpen);
    AuthenticatedElement maskShare = authElemSerializer.deserialize(byteMask);
    return new InputMask(openVal, maskShare);
  }
}

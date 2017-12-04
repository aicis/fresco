package dk.alexandra.fresco.tools.mascot.field;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;

// TODO security implications
public class FieldElementSerializer {

  public static byte[] serialize(FieldElement element) {
    try {
      return ByteArrayHelper.serialize(element);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return new byte[] {};
    }
  }

  public static FieldElement deserialize(byte[] data) {
    try {
      return (FieldElement) ByteArrayHelper.deserialize(data);
    } catch (ClassNotFoundException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public static byte[] serialize(List<FieldElement> elements) {
    try {
      return ByteArrayHelper.serialize((Serializable) elements);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return new byte[] {};
    }
  }

  public static List<FieldElement> deserializeList(byte[] data) {
    try {
      return (List<FieldElement>) ByteArrayHelper.deserialize(data);
    } catch (ClassNotFoundException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

}

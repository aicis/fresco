package dk.alexandra.fresco.framework.network.serializers;

import java.util.List;

public interface SecureSerializer<T> {

  byte[] serialize(T obj);
  
  T deserialize(byte[] data);
  
  byte[] serialize(List<T> objs);

  List<T> deserializeList(byte[] data);
  
}

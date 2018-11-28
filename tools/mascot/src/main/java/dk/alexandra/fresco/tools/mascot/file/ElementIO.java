package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.network.serializers.StaticSizeByteSerializer;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class ElementIO<U> {

  private static final int ELEMENTS_IN_BUFFER = 128;
  private final ByteBuffer buffer;
  private final StaticSizeByteSerializer serializer;
  private final FileChannel file;
  private int readElemPos = 0;

  /**
   * Process based on a given input data
   */
  public <T extends StaticSizeByteSerializer<U>> ElementIO(String fileDir, T serializer) {
    this.serializer = serializer;
    this.buffer = ByteBuffer.allocateDirect(ELEMENTS_IN_BUFFER * serializer.getElementSize());
    try {
      file = FileChannel
          .open(Paths.get(fileDir), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
              StandardOpenOption.READ);
    } catch (IOException e) {
      throw new RuntimeException("Could not open content file: " + e.getMessage());
    }
  }

  public void close() {
    ExceptionConverter.safe(() ->
    {
      file.close();
      return null;
    }, "Could not close file");
  }


  public void writeData(List<U> elements) {
    try {
      file.position(file.size());
      buffer.clear();
      // Amount of times to fill buffer, rounded down
      int loops = elements.size() / ELEMENTS_IN_BUFFER;
      for (int i = 0; i < loops; i++) {
        buffer.put(serializer
            .serialize(elements.subList(i * ELEMENTS_IN_BUFFER, (i + 1) * ELEMENTS_IN_BUFFER)));
        buffer.flip();
        file.write(buffer);
        buffer.flip();
      }
      // Add any remaining elements to buffer
      buffer
          .put(serializer.serialize(elements.subList(loops * ELEMENTS_IN_BUFFER, elements.size())));
      buffer.flip();
      file.write(buffer);
      buffer.flip();
    } catch (Exception e) {
      throw new RuntimeException("Could not write data to file: " + e.toString());
    }
  }

  public List<U> readData(int amount) {
    List<U> res = new LinkedList<U>();
    byte[] tempArray = new byte[ELEMENTS_IN_BUFFER * serializer.getElementSize()];
    try {
      file.position(readElemPos * serializer.getElementSize());
      buffer.clear();
      // Amount of times to fill buffer, rounded down
      int loops = amount / ELEMENTS_IN_BUFFER;
      for (int i = 0; i < loops; i++) {
        int read = file.read(buffer);
        buffer.flip();
        if (read != ELEMENTS_IN_BUFFER * serializer.getElementSize()) {
          throw new RuntimeException("Could not fill buffer");
        }
        buffer.get(tempArray);
        buffer.flip();
        List<U> currentElements = serializer.deserializeList(tempArray);
        res.addAll(currentElements);
      }
      readElemPos += amount;
    } catch (Exception e) {
      throw new RuntimeException("Could not read all elements from content file: " + e.toString());
    }
    return res;
  }
}
package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.network.serializers.StaticSizeByteSerializer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class ElementIO<T extends StaticSizeByteSerializer<U>, U> {

  private static final int ELEMENTS_IN_BUFFER = 128;
  private final ByteBuffer buffer;
  private final T serializer;
  private int readElemPos = 0;

  /**
   * Process based on a given input data
   */
  public ElementIO(T serializer) {
    this.serializer = serializer;
    this.buffer = ByteBuffer.allocateDirect(ELEMENTS_IN_BUFFER * serializer.getElementSize());
  }

  public FileChannel getFile(String fileDir) {
    Path path = Paths.get(fileDir);
    FileChannel file;
    try {
      file = FileChannel
          .open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
    } catch (IOException e) {
      throw new RuntimeException("Could not open content file " + e.toString());
    }
    return file;
  }

  public void writeData(FileChannel channel, List<U> elements) {
    try {
      channel.position(channel.size());
      buffer.clear();
      // Amount of times to fill buffer, rounded down
      int loops = elements.size() / ELEMENTS_IN_BUFFER;
      for (int i = 0; i < loops; i++) {
        buffer.put(serializer
            .serialize(elements.subList(i * ELEMENTS_IN_BUFFER, (i + 1) * ELEMENTS_IN_BUFFER)));
        buffer.flip();
        channel.write(buffer);
        buffer.flip();
      }
      // Add any remaining elements to buffer
      buffer
          .put(serializer.serialize(elements.subList(loops * ELEMENTS_IN_BUFFER, elements.size())));
      buffer.flip();
      channel.write(buffer);
      buffer.flip();
    } catch (Exception e) {
      throw new RuntimeException("Could not write data to file: " + e.toString());
    }
  }

  public List<U> readData(FileChannel channel, int amount) {
    List<U> res = new LinkedList<U>();
    byte[] tempArray = new byte[ELEMENTS_IN_BUFFER * serializer.getElementSize()];
    try {
      channel.position(readElemPos * serializer.getElementSize());
      buffer.clear();
      // Amount of times to fill buffer, rounded down
      int loops = amount / ELEMENTS_IN_BUFFER;
      for (int i = 0; i < loops; i++) {
        int read = channel.read(buffer);
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
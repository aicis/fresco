package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ElementWriter {

  /**
   * Process based on a given input data
   */
  public ElementWriter() {
  }

  public void processMetaFile(String fileDir, MascotMetaContent settings) {
    ExceptionConverter.safe(() -> {
      writeMetaFile(fileDir, settings);
      return null;
    }, "Could not write meta file");
  }

  public void processContentFile(String fileDir, List<AuthenticatedElement> elements,
      ByteSerializer serializer) {
    ExceptionConverter.safe(() -> {
      writeContentFile(fileDir, elements, serializer);
      return null;
    }, "Could not write content file");
  }

  public MascotMetaContent loadMetaFile(String fileDir) {
    return ExceptionConverter.safe(() -> readMetaFile(fileDir), "Could not read file");
  }

  public List<AuthenticatedElement> loadContentFile(String fileDir, ByteSerializer serializer) {
    return ExceptionConverter
        .safe(() -> readContentFile(fileDir, serializer), "Could not read content file");
  }

  private FileChannel getFile(String fileDir) throws Exception {
    Path path = Paths.get(fileDir);
    FileChannel file = null;
    try {
      file = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    } catch (IOException e) {
      // The file might already exist, if that is the case instead read the content
    }
    return file;
  }

  private void writeMetaFile(String fileDir, MascotMetaContent settings) throws Exception {
    Path path = Paths.get(fileDir);
    FileChannel indexFile;
    try {
      indexFile = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      ObjectOutputStream outputStream = new ObjectOutputStream(Channels.newOutputStream(indexFile));
      outputStream.writeObject(settings);
      outputStream.close();
    } catch (IOException e) {
      // The file might already exist, if that is the case instead read the content
      indexFile = FileChannel.open(path, StandardOpenOption.READ);
      ObjectInputStream inputStream = new ObjectInputStream(Channels.newInputStream(indexFile));
      MascotMetaContent loadedSettings = (MascotMetaContent) inputStream.readObject();
      inputStream.close();
      if (!settings.isCompatible(loadedSettings)) {
        throw new RuntimeException(
            "Preprocessed material in file is generated using different parameters");
      }
    }
    indexFile.close();
  }

  private void writeContentFile(String fileDir, List<AuthenticatedElement> elements,
      ByteSerializer serializer) throws Exception {
    FileChannel contentFile;
    try {
      contentFile = FileChannel.open(Paths.get(fileDir), StandardOpenOption.CREATE_NEW,
          StandardOpenOption.WRITE);
    } catch (IOException e) {
      contentFile = FileChannel.open(Paths.get(fileDir), StandardOpenOption.WRITE);
      contentFile.position(contentFile.size());
    }
    ByteBuffer buffer = ByteBuffer.wrap(serializer.serialize(elements));
    contentFile.write(buffer);
    contentFile.close();
  }

  private MascotMetaContent readMetaFile(String fileDir) throws Exception {
    Path path = Paths.get(fileDir);
    FileChannel indexFile = FileChannel.open(path, StandardOpenOption.READ);
    ObjectInputStream inputStream = new ObjectInputStream(Channels.newInputStream(indexFile));
    // File with settings, without content yet
    MascotMetaContent settings = (MascotMetaContent) inputStream.readObject();
    indexFile.close();
    return settings;
  }

  private List<AuthenticatedElement> readContentFile(String fileDir, ByteSerializer serializer)
      throws Exception {
    // Load the content
    FileChannel contentFile = FileChannel
        .open(Paths.get(fileDir), StandardOpenOption.READ);
    ByteBuffer buffer = ByteBuffer.allocate((int) contentFile.size());
    if (contentFile.read(buffer) != contentFile.size()) {
      throw new RuntimeException("Could not read whole file");
    }
    buffer.flip();
    List<AuthenticatedElement> elements = serializer.deserializeList(buffer.array());
    contentFile.close();
    return elements;
//    int amountOfElements = (int) contentFile.size() / settings.getElementSize();
//    List<AuthenticatedElement> elements = new ArrayList<>(amountOfElements);
//    ByteBuffer buffer = ByteBuffer.allocate(settings.getElementSize());
//    for (int i = 0; i < amountOfElements; i++) {
//      contentFile.read(buffer);
//      buffer.flip();
//      AuthenticatedElement currentElement = settings.deserialize(buffer.array());
//      elements.add(currentElement);
//    }
//    settings.appendElements(elements);
  }
}

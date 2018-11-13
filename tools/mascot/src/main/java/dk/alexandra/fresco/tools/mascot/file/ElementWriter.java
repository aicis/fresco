package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ElementWriter {
  /**
   * Process based on a given input data
   *
   * @param resources
   */
  public ElementWriter() {
  }

  public void process(String fileDir, ElementPreprocessingFile settings) {
    ExceptionConverter.safe(() -> {
      writeFile(fileDir, settings);
      return null;
    }, "Could not write file");
  }

  public ElementPreprocessingFile load(String fileDir) {
    return ExceptionConverter.safe(() -> readFile(fileDir), "Could not read file");
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

  private void writeFile(String fileDir, ElementPreprocessingFile settings) throws Exception {
    Path path = Paths.get(fileDir);
    FileChannel indexFile, contentFile;
    try {
      indexFile = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      ObjectOutputStream outputStream = new ObjectOutputStream(Channels.newOutputStream(indexFile));
      outputStream.writeObject(settings);
      outputStream.close();
    } catch (IOException e) {
      // The file might already exist, if that is the case instead read the content
      indexFile = FileChannel.open(path, StandardOpenOption.READ);
      ObjectInputStream inputStream = new ObjectInputStream(Channels.newInputStream(indexFile));
      ElementPreprocessingFile loadedSettings = (ElementPreprocessingFile) inputStream.readObject();
      inputStream.close();
      if (!settings.isCompatible(loadedSettings)) {
        throw new RuntimeException(
            "Preprocessed material in file is generated using different parameters");
      }
    }
    try {
      contentFile = FileChannel.open(Paths.get(fileDir + ".cont"), StandardOpenOption.CREATE_NEW,
          StandardOpenOption.WRITE);
    } catch (IOException e) {
      contentFile = FileChannel.open(Paths.get(fileDir + ".cont"), StandardOpenOption.WRITE);
      contentFile.position(contentFile.size());
    }
    ByteBuffer buffer = ByteBuffer.allocate(settings.getElementSize());
    for (AuthenticatedElement element : settings.getElements()) {
      buffer.put(settings.serialize(element));
      buffer.flip();
      contentFile.write(buffer);
      buffer.flip();
    }
    contentFile.close();
  }

  private ElementPreprocessingFile readFile(String fileDir) throws Exception {
    Path path = Paths.get(fileDir);
    FileChannel indexFile = FileChannel.open(path, StandardOpenOption.READ);
    ObjectInputStream inputStream = new ObjectInputStream(Channels.newInputStream(indexFile));
    // File with settings, without content yet
    ElementPreprocessingFile settings = (ElementPreprocessingFile) inputStream.readObject();
    // Load the content
    FileChannel contentFile = FileChannel.open(Paths.get(fileDir + ".cont"), StandardOpenOption.READ);
    int amountOfElements = (int) contentFile.size() / settings.getElementSize();
    List<AuthenticatedElement> elements = new ArrayList<>(amountOfElements);
    ByteBuffer buffer = ByteBuffer.allocate(settings.getElementSize());
    for (int i = 0; i < amountOfElements; i++) {
      contentFile.read(buffer);
      buffer.flip();
      AuthenticatedElement currentElement = settings.deserialize(buffer.array());
      elements.add(currentElement);
    }
    settings.appendElements(elements);
    return settings;
  }
}

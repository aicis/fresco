package dk.alexandra.fresco.tools.mascot.file;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SettingsIO<T extends Settings> {

  public void writeFile(String fileDir, T settings) {
    Path path = Paths.get(fileDir);
    FileChannel settingsFile;
    try {
      settingsFile = FileChannel
          .open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      ObjectOutputStream outputStream = new ObjectOutputStream(
          Channels.newOutputStream(settingsFile));
      outputStream.writeObject(settings);
      outputStream.close();
      settingsFile.close();
    } catch (IOException e) {
      throw new RuntimeException(
          "Could not create settings file: " + e.getMessage());
    }
  }

  public T readFile(String fileDir) {
    Path path = Paths.get(fileDir);
    try {
      FileChannel indexFile = FileChannel.open(path, StandardOpenOption.READ);
      ObjectInputStream inputStream = new ObjectInputStream(Channels.newInputStream(indexFile));
      T settings = (T) inputStream.readObject();
      indexFile.close();
      return settings;
    } catch (IOException|ClassNotFoundException e) {
      throw new RuntimeException("Could not read settings file: " + e.getMessage());
    }
  }
}

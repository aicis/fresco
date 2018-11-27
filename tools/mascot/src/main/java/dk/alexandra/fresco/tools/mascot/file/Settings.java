package dk.alexandra.fresco.tools.mascot.file;

import java.io.Serializable;

public interface Settings extends Serializable {
  int getNoPlayers();

  boolean isCompatible(Settings otherSettings);
}

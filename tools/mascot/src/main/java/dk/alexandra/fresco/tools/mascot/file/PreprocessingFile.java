package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

import java.io.Serializable;
import java.util.List;

public interface PreprocessingFile extends Serializable {
  int getNoPlayers();

  List<?> getElements();

  int getFirstUnusedIndex();
}

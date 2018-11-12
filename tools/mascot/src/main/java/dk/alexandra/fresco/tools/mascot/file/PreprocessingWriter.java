package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.network.Network;

import java.util.List;

public interface PreprocessingWriter {

  /**
   * Initialize the underlying preprocessing generation functionality.
   *
   * @param network The network to use
   */
  void init(Network network);

  /**
   * Execute the preprocessing and store generated material in the a certain file.
   * Creates a new file if it does not exist and appends if it already exists.
   *
   * @param fileDir The name of the file to store the preprocessed material.
   * @param amount  The amount of elements to preprocess.
   */
  void process(String fileDir, List<?> elements);
}

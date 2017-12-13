package dk.alexandra.fresco.tools.mascot;

public interface Initializable {

  void initialize();

  boolean isInitialized();

  default void throwIfInitialized() {
    if (isInitialized()) {
      throw new IllegalArgumentException("Already initialized");
    }
  }

  default void initializeIfNeeded() {
    if (!isInitialized()) {
      initialize();
    }
  }

}

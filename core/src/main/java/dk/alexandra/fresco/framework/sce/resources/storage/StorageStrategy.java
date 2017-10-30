package dk.alexandra.fresco.framework.sce.resources.storage;

public enum StorageStrategy {

  IN_MEMORY,
  STREAMED_STORAGE;

  public static Storage fromString(String storageString) {
    final String ss = storageString.toUpperCase();
    switch (ss) {
      case "IN_MEMORY":
      case "INMEMORY":
        return new InMemoryStorage();
      case "STREAMED_STORAGE":
      case "FILE_BASED_STORAGE":
        return new FilebasedStreamedStorageImpl(new InMemoryStorage());
      default:
        return null;
    }
  }
}
